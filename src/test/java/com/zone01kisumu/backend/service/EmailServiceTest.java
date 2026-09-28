package com.zone01kisumu.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private String testEmail;
    private String testSubject;
    private String testBody;

    @BeforeEach
    void setUp() {
        testEmail = "user@example.com";
        testSubject = "Test Subject";
        testBody = "Test email body content";
    }

    // ================ sendOtpEmail Success Tests ================

    @Test
    void sendOtpEmail_ShouldSendEmail_WhenValidParametersProvided() {
        emailService.sendOtpEmail(testEmail, testSubject, testBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertEquals(testEmail, sentMessage.getTo()[0]);
        assertEquals(testSubject, sentMessage.getSubject());
        assertEquals(testBody, sentMessage.getText());
        assertEquals("noreply@yourdomain.com", sentMessage.getFrom());
    }

    @Test
    void sendOtpEmail_ShouldSendEmail_WithOtpContent() {
        String otpSubject = "Your OTP for Login";
        String otpBody = "Your OTP is: 123456\\n\\nThis OTP will expire in 5 minutes.";

        emailService.sendOtpEmail(testEmail, otpSubject, otpBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(testEmail, sentMessage.getTo()[0]);
        assertEquals(otpSubject, sentMessage.getSubject());
        assertEquals(otpBody, sentMessage.getText());
        assertTrue(sentMessage.getText().contains("123456"));
        assertTrue(sentMessage.getText().contains("5 minutes"));
    }

    @Test
    void sendOtpEmail_ShouldSendEmail_WithMultipleRecipientsFormat() {
        String multiEmail = "user1@example.com";

        emailService.sendOtpEmail(multiEmail, testSubject, testBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(multiEmail, sentMessage.getTo()[0]);
    }

    @Test
    void sendOtpEmail_ShouldSetCorrectFromAddress() {
        emailService.sendOtpEmail(testEmail, testSubject, testBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("noreply@yourdomain.com", sentMessage.getFrom());
    }

    @Test
    void sendOtpEmail_ShouldHandleSpecialCharactersInSubject() {
        String specialSubject = "Your OTP: åäö 测试 🔐";

        emailService.sendOtpEmail(testEmail, specialSubject, testBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(specialSubject, sentMessage.getSubject());
    }

    @Test
    void sendOtpEmail_ShouldHandleSpecialCharactersInBody() {
        String specialBody = "Your OTP is: 123456\\n\\nSpecial chars: åäö 测试 🔐\\nEmojis: ✅ ❌ 🎉";

        emailService.sendOtpEmail(testEmail, testSubject, specialBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(specialBody, sentMessage.getText());
    }

    @Test
    void sendOtpEmail_ShouldHandleLongEmailBody() {
        StringBuilder longBody = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longBody.append("This is line ").append(i).append(" of a very long email body.\\n");
        }

        emailService.sendOtpEmail(testEmail, testSubject, longBody.toString());

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(longBody.toString(), sentMessage.getText());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // ================ sendOtpEmail Error Handling Tests ================

    @Test
    void sendOtpEmail_ShouldHandleMailException_Gracefully() {
        MailException mailException = new MailException("SMTP server unavailable") {};
        doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

        // Should not throw exception, should handle gracefully
        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(testEmail, testSubject, testBody)
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_ShouldHandleRuntimeException_Gracefully() {
        RuntimeException runtimeException = new RuntimeException("Unexpected error");
        doThrow(runtimeException).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(testEmail, testSubject, testBody)
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_ShouldHandleGenericException_Gracefully() {
        RuntimeException genericException = new RuntimeException("Generic error");
        doThrow(genericException).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(testEmail, testSubject, testBody)
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // ================ sendOtpEmail Null/Empty Parameter Tests ================

    @Test
    void sendOtpEmail_ShouldHandleNullEmail() {
        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(null, testSubject, testBody)
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(1, sentMessage.getTo().length);
        assertNull(sentMessage.getTo()[0]);
    }

    @Test
    void sendOtpEmail_ShouldHandleEmptyEmail() {
        assertDoesNotThrow(() ->
            emailService.sendOtpEmail("", testSubject, testBody)
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("", sentMessage.getTo()[0]);
    }

    @Test
    void sendOtpEmail_ShouldHandleNullSubject() {
        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(testEmail, null, testBody)
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNull(sentMessage.getSubject());
    }

    @Test
    void sendOtpEmail_ShouldHandleEmptySubject() {
        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(testEmail, "", testBody)
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("", sentMessage.getSubject());
    }

    @Test
    void sendOtpEmail_ShouldHandleNullBody() {
        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(testEmail, testSubject, null)
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNull(sentMessage.getText());
    }

    @Test
    void sendOtpEmail_ShouldHandleEmptyBody() {
        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(testEmail, testSubject, "")
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("", sentMessage.getText());
    }

    @Test
    void sendOtpEmail_ShouldHandleAllNullParameters() {
        assertDoesNotThrow(() ->
            emailService.sendOtpEmail(null, null, null)
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(1, sentMessage.getTo().length);
        assertNull(sentMessage.getTo()[0]);
        assertNull(sentMessage.getSubject());
        assertNull(sentMessage.getText());
        assertEquals("noreply@yourdomain.com", sentMessage.getFrom()); // From should still be set
    }

    // ================ sendOtpEmail Email Format Validation Tests ================

    @Test
    void sendOtpEmail_ShouldHandleValidEmailFormats() {
        String[] validEmails = {
            "user@example.com",
            "test.email@domain.co.uk",
            "user+tag@example.org",
            "123@456.com",
            "a@b.co",
            "user_name@example-domain.com",
            "firstname.lastname@company.travel"
        };

        for (String email : validEmails) {
            reset(mailSender); // Reset mock for each iteration

            emailService.sendOtpEmail(email, testSubject, testBody);

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(messageCaptor.capture());

            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertEquals(email, sentMessage.getTo()[0]);
        }
    }

    @Test
    void sendOtpEmail_ShouldHandleInvalidEmailFormats() {
        String[] invalidEmails = {
            "invalid-email",
            "@domain.com",
            "user@",
            "user@@domain.com",
            "user@domain",
            "user space@domain.com"
        };

        for (String email : invalidEmails) {
            reset(mailSender);

            // Should not throw exception even with invalid emails
            assertDoesNotThrow(() ->
                emailService.sendOtpEmail(email, testSubject, testBody)
            );

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(messageCaptor.capture());

            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertEquals(email, sentMessage.getTo()[0]);
        }
    }

    // ================ Concurrent Access Tests ================

    @Test
    void sendOtpEmail_ShouldHandleConcurrentCalls() throws InterruptedException {
        int numberOfThreads = 10;
        Thread[] threads = new Thread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                emailService.sendOtpEmail(
                    "user" + threadNum + "@example.com",
                    "Subject " + threadNum,
                    "Body " + threadNum
                );
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all emails were sent
        verify(mailSender, times(numberOfThreads)).send(any(SimpleMailMessage.class));
    }

    // ================ Integration-like Tests ================

    @Test
    void sendOtpEmail_ShouldCreateCompleteMessage() {
        String to = "recipient@example.com";
        String subject = "Your OTP Code";
        String body = "Your verification code is: 123456\\n\\nThis code expires in 5 minutes.";

        emailService.sendOtpEmail(to, subject, body);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        // Verify all fields are set correctly
        assertNotNull(message);
        assertEquals(to, message.getTo()[0]);
        assertEquals(subject, message.getSubject());
        assertEquals(body, message.getText());
        assertEquals("noreply@yourdomain.com", message.getFrom());

        // Verify the message is ready to send
        assertNotNull(message.getTo());
        assertTrue(message.getTo().length > 0);
    }

    @Test
    void sendOtpEmail_ShouldHandleMultilineBody() {
        String multilineBody = "Line 1\\nLine 2\\nLine 3\\n\\nLine 5 with empty line above";

        emailService.sendOtpEmail(testEmail, testSubject, multilineBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(multilineBody, sentMessage.getText());
        assertTrue(sentMessage.getText().contains("\\n"));
    }

    // ================ Mail Sender Interaction Tests ================

    @Test
    void sendOtpEmail_ShouldCallMailSenderExactlyOnce() {
        emailService.sendOtpEmail(testEmail, testSubject, testBody);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_ShouldNotCallOtherMailSenderMethods() {
        emailService.sendOtpEmail(testEmail, testSubject, testBody);

        verify(mailSender, only()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_ShouldPassCorrectMessageType() {
        emailService.sendOtpEmail(testEmail, testSubject, testBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        assertTrue(messageCaptor.getValue() instanceof SimpleMailMessage);
    }
}