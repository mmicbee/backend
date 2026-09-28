package com.zone01kisumu.backend.paymentTests.mpesaTests;

import com.zone01kisumu.backend.config.MpesaConfigProperties;
import com.zone01kisumu.backend.service.MpesaService;

import okhttp3.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MpesaServiceTest {

    @Mock
    private MpesaConfigProperties config;

    @Mock
    private OkHttpClient client;

    @Mock
    private Call call;

    private MpesaService mpesaService;

    @BeforeEach
    void setUp() {
        mpesaService = new MpesaService(config);
    }

    @Test
    void authenticate_SuccessWithNewToken_SandboxEnvironment() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("sandbox");
        when(config.getAppKey()).thenReturn("testAppKey");
        when(config.getAppSecret()).thenReturn("testAppSecret");

        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        
        JSONObject authResponse = new JSONObject();
        authResponse.put("access_token", "test-access-token-123");
        authResponse.put("expires_in", "3600");
        
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(authResponse.toString());
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        MpesaService mpesaServiceSpy = spy(mpesaService);
        doReturn(client).when(mpesaServiceSpy).createOkHttpClient();

        // Act
        String result = mpesaServiceSpy.authenticate();

        // Assert
        assertNotNull(result);
        assertEquals("test-access-token-123", result);
        assertEquals("test-access-token-123", mpesaServiceSpy.getAccessToken());
        assertTrue(mpesaServiceSpy.getTokenExpiryTime() > System.currentTimeMillis());
        
        // Verify the request was made with correct parameters
        verify(client).newCall(argThat(request -> {
            assertTrue(request.url().toString().contains("https://sandbox.safaricom.co.ke/oauth/v1/generate"));
            assertTrue(request.url().toString().contains("grant_type=client_credentials"));
            assertEquals("GET", request.method());
            
            String expectedAuth = "Basic " + Base64.getEncoder().encodeToString(
                "testAppKey:testAppSecret".getBytes(StandardCharsets.ISO_8859_1)
            );
            assertEquals(expectedAuth, request.header("authorization"));
            return true;
        }));
    }

    @Test
    void authenticate_SuccessWithNewToken_ProductionEnvironment() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("production");
        when(config.getAppKey()).thenReturn("prodAppKey");
        when(config.getAppSecret()).thenReturn("prodAppSecret");

        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        
        JSONObject authResponse = new JSONObject();
        authResponse.put("access_token", "prod-access-token-456");
        
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(authResponse.toString());
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        MpesaService mpesaServiceSpy = spy(mpesaService);
        doReturn(client).when(mpesaServiceSpy).createOkHttpClient();

        // Act
        String result = mpesaServiceSpy.authenticate();

        // Assert
        assertNotNull(result);
        assertEquals("prod-access-token-456", result);
        
        // Verify production URL was used
        verify(client).newCall(argThat(request -> 
            request.url().toString().contains("https://api.safaricom.co.ke/oauth/v1/generate")
        ));
    }

    @Test
    void authenticate_ReturnsCachedToken_WhenNotExpired() throws IOException {
        // Arrange - Set up a valid token that hasn't expired
        mpesaService.setAccessToken("cached-token-789");
        mpesaService.setTokenExpiryTime(System.currentTimeMillis() + 300000); // 5 minutes from now

        // Act
        String result = mpesaService.authenticate();

        // Assert
        assertEquals("cached-token-789", result);
        
        // Verify no HTTP call was made
        verifyNoInteractions(client, call, config);
    }

    @Test
    void authenticate_FetchesNewToken_WhenTokenExpired() throws IOException {
        // Arrange - Set up an expired token
        mpesaService.setAccessToken("expired-token");
        mpesaService.setTokenExpiryTime(System.currentTimeMillis() - 1000); // 1 second ago

        when(config.getEnv()).thenReturn("sandbox");
        when(config.getAppKey()).thenReturn("testAppKey");
        when(config.getAppSecret()).thenReturn("testAppSecret");

        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        
        JSONObject authResponse = new JSONObject();
        authResponse.put("access_token", "new-token-999");
        
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(authResponse.toString());
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        MpesaService mpesaServiceSpy = spy(mpesaService);
        doReturn(client).when(mpesaServiceSpy).createOkHttpClient();

        // Act
        String result = mpesaServiceSpy.authenticate();

        // Assert
        assertEquals("new-token-999", result);
        assertNotEquals("expired-token", result);
        
        // Verify HTTP call was made to get new token
        verify(client).newCall(any(Request.class));
    }

    @Test
    void authenticate_HttpCallUnsuccessful() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("sandbox");
        when(config.getAppKey()).thenReturn("testAppKey");
        when(config.getAppSecret()).thenReturn("testAppSecret");

        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        
        when(response.isSuccessful()).thenReturn(false);
        when(response.code()).thenReturn(401);
        when(response.message()).thenReturn("Unauthorized");
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("Invalid credentials");
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        MpesaService mpesaServiceSpy = spy(mpesaService);
        doReturn(client).when(mpesaServiceSpy).createOkHttpClient();

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> 
            mpesaServiceSpy.authenticate()
        );
        
        assertTrue(exception.getMessage().contains("Authentication failed"));
        assertTrue(exception.getMessage().contains("Code: 401"));
        assertTrue(exception.getMessage().contains("Message: Unauthorized"));
        assertTrue(exception.getMessage().contains("Body: Invalid credentials"));
    }

    @Test
    void authenticate_ResponseBodyNull() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("sandbox");
        when(config.getAppKey()).thenReturn("testAppKey");
        when(config.getAppSecret()).thenReturn("testAppSecret");

        Response response = mock(Response.class);
        
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(null);
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        MpesaService mpesaServiceSpy = spy(mpesaService);
        doReturn(client).when(mpesaServiceSpy).createOkHttpClient();

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> 
            mpesaServiceSpy.authenticate()
        );
        
        assertEquals("Response body is null", exception.getMessage());
    }

    @Test
    void authenticate_NoAccessTokenInResponse() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("sandbox");
        when(config.getAppKey()).thenReturn("testAppKey");
        when(config.getAppSecret()).thenReturn("testAppSecret");

        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        
        JSONObject authResponse = new JSONObject();
        authResponse.put("error", "invalid_client");
        authResponse.put("error_description", "Invalid client credentials");
        
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(authResponse.toString());
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        MpesaService mpesaServiceSpy = spy(mpesaService);
        doReturn(client).when(mpesaServiceSpy).createOkHttpClient();

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> 
            mpesaServiceSpy.authenticate()
        );
        
        assertTrue(exception.getMessage().contains("Authentication failed"));
        assertTrue(exception.getMessage().contains("invalid_client"));
    }

    @Test
    void authenticate_IOExceptionDuringHttpCall() throws IOException {
        // Arrange
        when(config.getEnv()).thenReturn("sandbox");
        when(config.getAppKey()).thenReturn("testAppKey");
        when(config.getAppSecret()).thenReturn("testAppSecret");

        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenThrow(new IOException("Network connection failed"));

        MpesaService mpesaServiceSpy = spy(mpesaService);
        doReturn(client).when(mpesaServiceSpy).createOkHttpClient();

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> 
            mpesaServiceSpy.authenticate()
        );
        
        assertEquals("Network connection failed", exception.getMessage());
    }

    @Test
    void authenticate_SynchronizedMethod_ThreadSafety() throws Exception {
        // Arrange
        when(config.getEnv()).thenReturn("sandbox");
        when(config.getAppKey()).thenReturn("testAppKey");
        when(config.getAppSecret()).thenReturn("testAppSecret");

        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        
        JSONObject authResponse = new JSONObject();
        authResponse.put("access_token", "thread-safe-token");
        
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(authResponse.toString());
        when(client.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        MpesaService mpesaServiceSpy = spy(mpesaService);
        doReturn(client).when(mpesaServiceSpy).createOkHttpClient();

        // Act - Test from multiple threads
        Runnable authTask = () -> {
            try {
                mpesaServiceSpy.authenticate();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Thread thread1 = new Thread(authTask);
        Thread thread2 = new Thread(authTask);
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();

        // Assert - Should complete without concurrency issues
        assertEquals("thread-safe-token", mpesaServiceSpy.getAccessToken());
        
        // Verify HTTP call was made only once due to synchronization
        verify(client, times(1)).newCall(any(Request.class));
    }

    @Test
    void testIsSandboxMethod() throws Exception {
        // Test sandbox environment
        when(config.getEnv()).thenReturn("sandbox");
        java.lang.reflect.Method isSandboxMethod = MpesaService.class.getDeclaredMethod("isSandbox");
        isSandboxMethod.setAccessible(true);
        Boolean result = (Boolean) isSandboxMethod.invoke(mpesaService);
        assertTrue(result);

        // Test production environment
        when(config.getEnv()).thenReturn("production");
        result = (Boolean) isSandboxMethod.invoke(mpesaService);
        assertFalse(result);

        // Test case insensitive
        when(config.getEnv()).thenReturn("SANDBOX");
        result = (Boolean) isSandboxMethod.invoke(mpesaService);
        assertTrue(result);
    }

    @Test
    void testGettersAndSetters() {
        // Test Lombok @Data annotation generated methods
        mpesaService.setAccessToken("test-token");
        mpesaService.setTokenExpiryTime(123456789L);
        
        assertEquals("test-token", mpesaService.getAccessToken());
        assertEquals(123456789L, mpesaService.getTokenExpiryTime());
    }

 
}