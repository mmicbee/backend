package com.zone01kisumu.backend.paymentTests.mpesaTests;

import com.zone01kisumu.backend.config.MpesaConfigProperties;
import com.zone01kisumu.backend.dto.STKPushRequestDto;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.PaymentRepository;
import com.zone01kisumu.backend.service.MpesaService;
import com.zone01kisumu.backend.service.SendSTKPush;

import okhttp3.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendSTKPushTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private MpesaConfigProperties config;

    @Mock
    private MpesaService mpesaService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    private SendSTKPush sendSTKPush;
    private STKPushRequestDto stkPushRequestDto;
    private Course course;

    @BeforeEach
    void setUp() {
        sendSTKPush = new SendSTKPush(paymentRepository, courseRepository, config, mpesaService);

        stkPushRequestDto = new STKPushRequestDto();
        stkPushRequestDto.setCourseId(1L);
        stkPushRequestDto.setStudentId(123L);
        stkPushRequestDto.setAmount(BigDecimal.valueOf(100.00));
        stkPushRequestDto.setPhoneNumber("254712345678");
        stkPushRequestDto.setTransactionType("CustomerPayBillOnline");

        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setPaymentAccount("174379");
        course.setMpesaPaymentType(Course.MpesaPaymentType.PAYBILL);
    }

    @Test
    void initiateSTKPush_Success() throws IOException {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(config.getEnv()).thenReturn("sandbox");
        when(mpesaService.authenticate()).thenReturn("test-access-token");

        // Create fresh mocks for this test only
        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);

        JSONObject responseJson = new JSONObject();
        responseJson.put("CheckoutRequestID", "ws_CO_123456789");
        responseJson.put("ResponseCode", "0");
        responseJson.put("ResponseDescription", "Success");

        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(responseJson.toString());
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        SendSTKPush sendSTKPushSpy = spy(sendSTKPush);
        doReturn(client).when(sendSTKPushSpy).createOkHttpClient();

        // Act
        String result = sendSTKPushSpy.initiateSTKPush("test-passkey", "https://callback.com", "https://timeout.com",
                stkPushRequestDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CheckoutRequestID"));

        // Verify payment was saved
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(123L, savedPayment.getStudentId());
        assertEquals(1L, savedPayment.getCourseId());
        assertEquals(BigDecimal.valueOf(100.00), savedPayment.getAmount());
        assertEquals(Payment.PaymentMethod.MPESA, savedPayment.getPaymentMethod());
        assertEquals("ws_CO_123456789", savedPayment.getPaystackReference());
        assertEquals(Payment.PaymentStatus.PENDING, savedPayment.getStatus());
        assertNotNull(savedPayment.getPaymentDate());
    }

    @Test
    void initiateSTKPush_CourseNotFound() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> sendSTKPush.initiateSTKPush("test-passkey", "https://callback.com", "https://timeout.com",
                        stkPushRequestDto));

        assertEquals("Course not found with ID: 1", exception.getMessage());
    }

    @Test
    void initiateSTKPush_NoPaymentAccount() {
        // Arrange
        course.setPaymentAccount(null);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> sendSTKPush.initiateSTKPush("test-passkey", "https://callback.com", "https://timeout.com",
                        stkPushRequestDto));

        assertEquals("Course does not have a valid payment account (BusinessShortCode)", exception.getMessage());
    }

    @Test
    void initiateSTKPush_EmptyPaymentAccount() {
        // Arrange
        course.setPaymentAccount("   ");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> sendSTKPush.initiateSTKPush("test-passkey", "https://callback.com", "https://timeout.com",
                        stkPushRequestDto));

        assertEquals("Course does not have a valid payment account (BusinessShortCode)", exception.getMessage());
    }

    @Test
    void initiateSTKPush_HttpResponseBodyNull() throws IOException {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(config.getEnv()).thenReturn("sandbox");
        when(mpesaService.authenticate()).thenReturn("test-access-token");

        // Create fresh mocks for this test only
        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response response = mock(Response.class);

        when(response.body()).thenReturn(null);
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        SendSTKPush sendSTKPushSpy = spy(sendSTKPush);
        doReturn(client).when(sendSTKPushSpy).createOkHttpClient();

        // Act & Assert
        IOException exception = assertThrows(IOException.class,
                () -> sendSTKPushSpy.initiateSTKPush("test-passkey", "https://callback.com", "https://timeout.com",
                        stkPushRequestDto));

        assertEquals("Response body is null", exception.getMessage());
    }

    @Test
    void initiateSTKPush_ProductionEnvironment() throws IOException {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(config.getEnv()).thenReturn("production");
        when(mpesaService.authenticate()).thenReturn("test-access-token");

        // Create fresh mocks for this test only
        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);

        JSONObject responseJson = new JSONObject();
        responseJson.put("CheckoutRequestID", "ws_CO_987654321");
        responseJson.put("ResponseCode", "0");

        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(responseJson.toString());
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        SendSTKPush sendSTKPushSpy = spy(sendSTKPush);
        doReturn(client).when(sendSTKPushSpy).createOkHttpClient();

        // Act
        String result = sendSTKPushSpy.initiateSTKPush("test-passkey", "https://callback.com", "https://timeout.com",
                stkPushRequestDto);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void sTKPushTransactionStatus_Success() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("sandbox");
        when(mpesaService.authenticate()).thenReturn("test-access-token");

        // Create fresh mocks for this test only
        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);

        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("{\"ResultCode\":\"0\",\"ResultDesc\":\"Success\"}");
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        SendSTKPush sendSTKPushSpy = spy(sendSTKPush);
        doReturn(client).when(sendSTKPushSpy).createOkHttpClient();

        // Act
        String result = sendSTKPushSpy.sTKPushTransactionStatus("174379", "test-password", "20231201120000",
                "ws_CO_123456789");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("ResultCode"));
    }

    @Test
    void sTKPushTransactionStatus_ResponseBodyNull() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("sandbox");
        when(mpesaService.authenticate()).thenReturn("test-access-token");

        // Create fresh mocks for this test only
        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response response = mock(Response.class);

        when(response.body()).thenReturn(null);
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        SendSTKPush sendSTKPushSpy = spy(sendSTKPush);
        doReturn(client).when(sendSTKPushSpy).createOkHttpClient();

        // Act & Assert
        IOException exception = assertThrows(IOException.class,
                () -> sendSTKPushSpy.sTKPushTransactionStatus("174379", "test-password", "20231201120000",
                        "ws_CO_123456789"));

        assertEquals("Response body is null", exception.getMessage());
    }

    @Test
    void sTKPushTransactionStatus_ProductionEnvironment() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("production");
        when(mpesaService.authenticate()).thenReturn("test-access-token");

        // Create fresh mocks for this test only
        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);

        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("{\"ResultCode\":\"0\",\"ResultDesc\":\"Success\"}");
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        SendSTKPush sendSTKPushSpy = spy(sendSTKPush);
        doReturn(client).when(sendSTKPushSpy).createOkHttpClient();

        // Act
        String result = sendSTKPushSpy.sTKPushTransactionStatus("174379", "test-password", "20231201120000",
                "ws_CO_123456789");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testIsSandboxMethod() throws Exception {
        // Use reflection to test private isSandbox method
        java.lang.reflect.Method isSandboxMethod = SendSTKPush.class.getDeclaredMethod("isSandbox");
        isSandboxMethod.setAccessible(true);

        when(config.getEnv()).thenReturn("sandbox");
        Boolean result = (Boolean) isSandboxMethod.invoke(sendSTKPush);
        assertTrue(result);

        when(config.getEnv()).thenReturn("production");
        result = (Boolean) isSandboxMethod.invoke(sendSTKPush);
        assertFalse(result);

        when(config.getEnv()).thenReturn("SANDBOX");
        result = (Boolean) isSandboxMethod.invoke(sendSTKPush);
        assertTrue(result);
    }

    @Test
    void initiateSTKPush_IOException() throws IOException {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(config.getEnv()).thenReturn("sandbox");
        when(mpesaService.authenticate()).thenReturn("test-access-token");

        // Create fresh mocks for this test only
        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);

        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenThrow(new IOException("Network error"));

        SendSTKPush sendSTKPushSpy = spy(sendSTKPush);
        doReturn(client).when(sendSTKPushSpy).createOkHttpClient();

        // Act & Assert
        IOException exception = assertThrows(IOException.class,
                () -> sendSTKPushSpy.initiateSTKPush("test-passkey", "https://callback.com", "https://timeout.com",
                        stkPushRequestDto));

        assertEquals("Network error", exception.getMessage());
    }

}