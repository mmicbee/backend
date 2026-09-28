package com.zone01kisumu.backend.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

import com.zone01kisumu.backend.config.MpesaConfigProperties;
import com.zone01kisumu.backend.dto.STKPushRequestDto;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.PaymentRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendSTKPush {

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;

    private final MpesaConfigProperties config;
    private final MpesaService mpesaService; // Service to handle M-Pesa authentication
    private static final String SANDURL = "https://sandbox.safaricom.co.ke";
    private static final String PRODURL = "https://api.safaricom.co.ke";
    private static final String MEDIA_TYPE_JSON = "application/json";
    private static final String AUTH = "authorization";

    public String initiateSTKPush(String passkey,
            String callbackUrl, String timeoutUrl,
            STKPushRequestDto dto) throws IOException {

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + dto.getCourseId()));

        String paymentMethod = course.getMpesaPaymentType().toString(); // e.g., "paybill" or "till"
        String paybillNumber = course.getPaybillNumber(); // Till number if applicable
        String businessShortCode = course.getPaymentAccount();
        if (businessShortCode == null || businessShortCode.trim().isEmpty()) {
            throw new IllegalStateException("Course does not have a valid payment account (BusinessShortCode)");
        }

        String timestamp = getCurrentTimestamp();
        String password = generateSTKPassword(businessShortCode, passkey, timestamp);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BusinessShortCode", businessShortCode);
        jsonObject.put("Password", password);
        jsonObject.put("Timestamp", timestamp);
        jsonObject.put("Amount", dto.getAmount());
        jsonObject.put("PhoneNumber", dto.getPhoneNumber());
        jsonObject.put("PartyA", dto.getPhoneNumber());

        String transactionType;
        if ("paybill".equalsIgnoreCase(paymentMethod)) {
            transactionType = "CustomerPayBillOnline";
            jsonObject.put("PartyB", businessShortCode); // Paybill number
            jsonObject.put("AccountReference", paybillNumber); // Must provide a valid account
        } else if ("till".equalsIgnoreCase(paymentMethod)) {
            transactionType = "CustomerBuyGoodsOnline";
            jsonObject.put("PartyB", businessShortCode); // Till number
            jsonObject.put("AccountReference", accountReference());
        } else {
            throw new IllegalStateException("Unsupported MpesaPaymentType: " + paymentMethod);
        }

        jsonObject.put("TransactionType", transactionType);
        jsonObject.put("CallBackURL", callbackUrl);
        jsonObject.put("QueueTimeOutURL", timeoutUrl);
        jsonObject.put("TransactionDesc", "Payment for: " + course.getTitle());

        // Differentiate Paybill vs Till Number

        String requestJson = jsonObject.toString();
        String baseUrl = isSandbox() ? SANDURL : PRODURL;
        OkHttpClient client = createOkHttpClient();
        MediaType mediaType = MediaType.parse(MEDIA_TYPE_JSON);
        RequestBody body = RequestBody.create(mediaType, requestJson);

        Request request = new Request.Builder()
                .url(baseUrl + "/mpesa/stkpush/v1/processrequest")
                .post(body)
                .addHeader("content-type", MEDIA_TYPE_JSON)
                .addHeader(AUTH, "Bearer " + mpesaService.authenticate())
                .addHeader("cache-control", "no-cache")
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody finalBody = response.body();
            if (finalBody == null) {
                throw new IOException("Response body is null");
            }

            String responseBody = finalBody.string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // 🔐 Save the payment in DB
            String checkoutRequestID = jsonResponse.optString("CheckoutRequestID");

            Payment payment = new Payment(
                    dto.getStudentId(),
                    dto.getCourseId(),
                    dto.getAmount(),
                    Payment.PaymentMethod.MPESA,
                    checkoutRequestID);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            return responseBody;
        }
    }

    public String sTKPushTransactionStatus(String businessShortCode, String password,
            String timestamp, String checkoutRequestID) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BusinessShortCode", businessShortCode);
        jsonObject.put("Password", password);
        jsonObject.put("Timestamp", timestamp);
        jsonObject.put("CheckoutRequestID", checkoutRequestID);

        String requestJson = jsonObject.toString();

        String baseUrl = isSandbox() ? SANDURL : PRODURL;
        OkHttpClient client = createOkHttpClient();
        MediaType mediaType = MediaType.parse(MEDIA_TYPE_JSON);
        RequestBody body = RequestBody.create(mediaType, requestJson);

        Request request = new Request.Builder()
                .url(baseUrl + "/mpesa/stkpushquery/v1/query")
                .post(body)
                .addHeader(AUTH, "Bearer " + mpesaService.authenticate())
                .addHeader("content-type", MEDIA_TYPE_JSON)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody finalBody = response.body(); // Read response body
            if (finalBody == null) {
                throw new IOException("Response body is null");
            }
            return finalBody.string();
        }
    }

    private boolean isSandbox() {
        return "sandbox".equalsIgnoreCase(config.getEnv());
    }

    private String generateSTKPassword(String businessShortCode, String passkey, String timestamp) {
        String rawPassword = businessShortCode + passkey + timestamp;
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String accountReference() {
        // random alphanumeric string of length 8
        return "LMS-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public OkHttpClient createOkHttpClient() {
        return new OkHttpClient();
    }

}
