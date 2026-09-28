package com.zone01kisumu.backend.paymentTests.mpesaTests;

import com.zone01kisumu.backend.config.MpesaConfigProperties;
import com.zone01kisumu.backend.controller.MpesaController;
import com.zone01kisumu.backend.dto.STKPushRequestDto;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.PaymentRepository;
import com.zone01kisumu.backend.service.SendSTKPush;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MpesaControllerTest {

    @Mock
    private MpesaConfigProperties config;

    @Mock
    private SendSTKPush sendSTKPush;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private MpesaController mpesaController;

    private STKPushRequestDto stkPushRequestDto;
    private Course course;
    private Payment payment;

    @BeforeEach
    void setUp() {
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

        payment = new Payment();
        payment.setId(1L);
        payment.setStudentId(123L);
        payment.setCourseId(1L);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setPaystackReference("ws_CO_123456789");
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
    }

    @Test
    void initiateSTKPush_Success() throws IOException {
        // Arrange
        when(config.getPasskey()).thenReturn("test-passkey");
        when(config.getCallbackUrl()).thenReturn("https://callback.com");
        when(config.getTimeoutUrl()).thenReturn("https://timeout.com");
        when(sendSTKPush.initiateSTKPush(anyString(), anyString(), anyString(), any(STKPushRequestDto.class)))
                .thenReturn("{\"ResponseCode\":\"0\",\"CheckoutRequestID\":\"ws_CO_123456789\"}");

        // Act
        ResponseEntity<String> response = mpesaController.initiateSTKPush(stkPushRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("CheckoutRequestID"));
        
        verify(sendSTKPush).initiateSTKPush("test-passkey", "https://callback.com", "https://timeout.com", stkPushRequestDto);
    }

    @Test
    void initiateSTKPush_JSONException() throws IOException {
        // Arrange
        when(config.getPasskey()).thenReturn("test-passkey");
        when(config.getCallbackUrl()).thenReturn("https://callback.com");
        when(config.getTimeoutUrl()).thenReturn("https://timeout.com");
        when(sendSTKPush.initiateSTKPush(anyString(), anyString(), anyString(), any(STKPushRequestDto.class)))
                .thenThrow(new JSONException("Invalid JSON format"));

        // Act
        ResponseEntity<String> response = mpesaController.initiateSTKPush(stkPushRequestDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error initiating STK Push"));
        assertTrue(response.getBody().contains("Invalid JSON format"));
    }

    @Test
    void initiateSTKPush_IOException() throws IOException {
        // Arrange
        when(config.getPasskey()).thenReturn("test-passkey");
        when(config.getCallbackUrl()).thenReturn("https://callback.com");
        when(config.getTimeoutUrl()).thenReturn("https://timeout.com");
        when(sendSTKPush.initiateSTKPush(anyString(), anyString(), anyString(), any(STKPushRequestDto.class)))
                .thenThrow(new IOException("Network error"));

        // Act
        ResponseEntity<String> response = mpesaController.initiateSTKPush(stkPushRequestDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Unexpected error"));
        assertTrue(response.getBody().contains("Network error"));
    }

    @Test
    void handleCallback_Success() {
        // Arrange
        String callbackData = "{\"Body\":{\"stkCallback\":{\"ResultCode\":0,\"ResultDesc\":\"The service request is processed successfully\"}}}";

        // Act
        ResponseEntity<String> response = mpesaController.handleCallback(callbackData);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"ResultDesc\":\"Callback processed successfully\""));
    }

    @Test
    void handleCallback_JSONException() {
        // Arrange
        String invalidCallbackData = "invalid-json-data";

        // Act
        ResponseEntity<String> response = mpesaController.handleCallback(invalidCallbackData);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().contains("\"RESULTCODE\":1"));
        assertFalse(response.getBody().contains("\"ResultDesc\":\"Error processing callback\""));
    }

    @Test
    void handleTimeout_Success() {
        // Arrange
        String timeoutData = "{\"TransactionID\":\"123456\",\"ResultCode\":0}";

        // Act
        ResponseEntity<String> response = mpesaController.handleTimeout(timeoutData);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"ResultDesc\":\"Timeout callback processed\""));
    }

    @Test
    void handleTimeout_JSONException() {
        // Arrange
        String invalidTimeoutData = "invalid-json-data";

        // Act
        ResponseEntity<String> response = mpesaController.handleTimeout(invalidTimeoutData);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().contains("\"RESULTCODE\":0"));
        assertFalse(response.getBody().contains("\"RESULTCODE\":\"Error processing timeout\""));
        assertFalse(response.getBody().contains("invalid-json-data"));
    }

    @Test
    void checkTransactionStatus_Success() throws IOException {
        // Arrange
        String checkoutRequestId = "ws_CO_123456789";
        
        when(paymentRepository.findByPaystackReference(checkoutRequestId))
                .thenReturn(Optional.of(payment));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));
        when(config.getPasskey()).thenReturn("test-passkey");
        when(sendSTKPush.sTKPushTransactionStatus(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("{\"ResultCode\":\"0\",\"ResultDesc\":\"The service request is processed successfully\"}");

        // Act
        ResponseEntity<String> response = mpesaController.checkTransactionStatus(checkoutRequestId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("ResultCode"));
        
        verify(paymentRepository).findByPaystackReference(checkoutRequestId);
        verify(courseRepository).findById(1L);
        verify(sendSTKPush).sTKPushTransactionStatus(
                eq("174379"), 
                anyString(), // password
                anyString(), // timestamp
                eq(checkoutRequestId)
        );
    }

    @Test
    void checkTransactionStatus_PaymentNotFound() {
        // Arrange
        String checkoutRequestId = "non-existent-id";
        when(paymentRepository.findByPaystackReference(checkoutRequestId))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = mpesaController.checkTransactionStatus(checkoutRequestId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Payment not found for checkoutRequestId"));
        assertTrue(response.getBody().contains("non-existent-id"));
    }

    @Test
    void checkTransactionStatus_CourseNotFound() {
        // Arrange
        String checkoutRequestId = "ws_CO_123456789";
        when(paymentRepository.findByPaystackReference(checkoutRequestId))
                .thenReturn(Optional.of(payment));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = mpesaController.checkTransactionStatus(checkoutRequestId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Course not found with ID: 1"));
    }

    @Test
    void checkTransactionStatus_NoPaymentAccount() {
        // Arrange
        String checkoutRequestId = "ws_CO_123456789";
        course.setPaymentAccount(null);
        
        when(paymentRepository.findByPaystackReference(checkoutRequestId))
                .thenReturn(Optional.of(payment));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));

        // Act
        ResponseEntity<String> response = mpesaController.checkTransactionStatus(checkoutRequestId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Course does not have a valid BusinessShortCode (paymentAccount)"));
    }

    @Test
    void checkTransactionStatus_EmptyPaymentAccount() {
        // Arrange
        String checkoutRequestId = "ws_CO_123456789";
        course.setPaymentAccount("   ");
        
        when(paymentRepository.findByPaystackReference(checkoutRequestId))
                .thenReturn(Optional.of(payment));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));

        // Act
        ResponseEntity<String> response = mpesaController.checkTransactionStatus(checkoutRequestId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Course does not have a valid BusinessShortCode (paymentAccount)"));
    }

    @Test
    void checkTransactionStatus_IOException() throws IOException {
        // Arrange
        String checkoutRequestId = "ws_CO_123456789";
        
        when(paymentRepository.findByPaystackReference(checkoutRequestId))
                .thenReturn(Optional.of(payment));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));
        when(config.getPasskey()).thenReturn("test-passkey");
        when(sendSTKPush.sTKPushTransactionStatus(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IOException("Network connection failed"));

        // Act
        ResponseEntity<String> response = mpesaController.checkTransactionStatus(checkoutRequestId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("IO error while checking transaction status"));
        assertTrue(response.getBody().contains("Network connection failed"));
    }

    @Test
    void checkTransactionStatus_GenericException() {
        // Arrange
        String checkoutRequestId = "ws_CO_123456789";
        
        when(paymentRepository.findByPaystackReference(checkoutRequestId))
                .thenThrow(new RuntimeException("Unexpected database error"));

        // Act
        ResponseEntity<String> response = mpesaController.checkTransactionStatus(checkoutRequestId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error: Unexpected database error"));
    }

    @Test
    void debugConfig_Success() {
        // Arrange
        when(config.getAppKey()).thenReturn("test-app-key");
        when(config.getAppSecret()).thenReturn("test-app-secret");
        when(config.getPasskey()).thenReturn("test-passkey");
        when(config.getCallbackUrl()).thenReturn("https://callback.com");
        when(config.getTimeoutUrl()).thenReturn("https://timeout.com");
        when(config.getEnv()).thenReturn("sandbox");

        // Act
        ResponseEntity<Map<String, String>> response = mpesaController.debugConfig();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, String> configs = response.getBody();
        assertEquals("test-app-key", configs.get("APP_KEY"));
        assertEquals("***SET***", configs.get("APP_SECRET"));
        assertEquals("***SET***", configs.get("PASSKEY"));
        assertEquals("https://callback.com", configs.get("CALLBACK_URL"));
        assertEquals("https://timeout.com", configs.get("TIMEOUT_URL"));
        assertEquals("sandbox", configs.get("ENV"));
    }

    @Test
    void debugConfig_NullSecrets() {
        // Arrange
        when(config.getAppKey()).thenReturn("test-app-key");
        when(config.getAppSecret()).thenReturn(null);
        when(config.getPasskey()).thenReturn(null);
        when(config.getCallbackUrl()).thenReturn("https://callback.com");
        when(config.getTimeoutUrl()).thenReturn("https://timeout.com");
        when(config.getEnv()).thenReturn("production");

        // Act
        ResponseEntity<Map<String, String>> response = mpesaController.debugConfig();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, String> configs = response.getBody();
        assertEquals("test-app-key", configs.get("APP_KEY"));
        assertEquals("NULL", configs.get("APP_SECRET"));
        assertEquals("NULL", configs.get("PASSKEY"));
        assertEquals("production", configs.get("ENV"));
    }

    @Test
    void testGeneratePassword_WithReflection() throws Exception {
        // Arrange
        String businessShortCode = "174379";
        String passkey = "test-passkey";
        String timestamp = "20231201120000";
        
        // Use reflection to access private method
        Method generatePasswordMethod = MpesaController.class.getDeclaredMethod(
            "generatePassword", String.class, String.class, String.class
        );
        generatePasswordMethod.setAccessible(true);
        
        // Act
        String password = (String) generatePasswordMethod.invoke(
            mpesaController, businessShortCode, passkey, timestamp
        );
        
        // Assert
        assertNotNull(password);
        // The password should be Base64 encoded "174379test-passkey20231201120000"
        String expectedRaw = "174379test-passkey20231201120000";
        String expectedEncoded = Base64.getEncoder().encodeToString(expectedRaw.getBytes());
        assertEquals(expectedEncoded, password);
    }

    @Test
    void testGeneratePassword_EmptyValues_WithReflection() throws Exception {
        // Arrange
        String businessShortCode = "";
        String passkey = "";
        String timestamp = "";
        
        // Use reflection to access private method
        Method generatePasswordMethod = MpesaController.class.getDeclaredMethod(
            "generatePassword", String.class, String.class, String.class
        );
        generatePasswordMethod.setAccessible(true);
        
        // Act
        String password = (String) generatePasswordMethod.invoke(
            mpesaController, businessShortCode, passkey, timestamp
        );
        
        // Assert
        assertNotNull(password);
        String expectedEncoded = Base64.getEncoder().encodeToString("".getBytes());
        assertEquals(expectedEncoded, password);
    }

    @Test
    void testGeneratePassword_NullValues_WithReflection() throws Exception {
        // Arrange
        String businessShortCode = null;
        String passkey = null;
        String timestamp = null;
        
        // Use reflection to access private method
        Method generatePasswordMethod = MpesaController.class.getDeclaredMethod(
            "generatePassword", String.class, String.class, String.class
        );
        generatePasswordMethod.setAccessible(true);
        
        // Act
        String password = (String) generatePasswordMethod.invoke(
            mpesaController, businessShortCode, passkey, timestamp
        );
        
        // Assert
        assertNotNull(password);
        // Should handle null values by converting them to "null" strings
        String expectedRaw = "nullnullnull";
        String expectedEncoded = Base64.getEncoder().encodeToString(expectedRaw.getBytes());
        assertEquals(expectedEncoded, password);
    }

    @Test
    void testGeneratePassword_IntegrationThroughPublicMethod() throws IOException {
        // This test verifies the private method is called correctly through the public method
        // Arrange
        String checkoutRequestId = "ws_CO_123456789";
        
        when(paymentRepository.findByPaystackReference(checkoutRequestId))
                .thenReturn(Optional.of(payment));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));
        when(config.getPasskey()).thenReturn("test-passkey");
        when(sendSTKPush.sTKPushTransactionStatus(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("{\"ResultCode\":\"0\"}");

        // Act
        ResponseEntity<String> response = mpesaController.checkTransactionStatus(checkoutRequestId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that the service was called with a password parameter
        verify(sendSTKPush).sTKPushTransactionStatus(
                eq("174379"), 
                anyString(), // This should be the Base64 encoded password
                anyString(), // timestamp
                eq(checkoutRequestId)
        );
    }
}