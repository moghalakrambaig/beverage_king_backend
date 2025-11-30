package com.spiritedhub.spiritedhub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
public class EmailService {

        @Autowired
        private SesClient sesClient;

        @Value("${AWS_SES_FROM_EMAIL}")
        private String fromEmail;

        // Send password reset email
        public void sendPasswordResetEmail(String toEmail, String resetLink) {
                String subject = "Reset Your Password";
                String htmlBody = "<h2>Password Reset</h2>"
                                + "<p>Click the link below to reset your password:</p>"
                                + "<a href=\"" + resetLink + "\">Reset Password</a><br>"
                                + "<p>If you didn’t request this, ignore this email.</p>";

                sendHtmlEmail(toEmail, subject, htmlBody);
        }

        // Generic HTML email sender
        public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
                // Trim and validate email
                if (!StringUtils.hasText(toEmail)) {
                        throw new IllegalArgumentException("Recipient email must not be empty");
                }
                toEmail = toEmail.trim();

                if (!toEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                        throw new IllegalArgumentException("Invalid email format: " + toEmail);
                }

                if (!StringUtils.hasText(fromEmail)) {
                        throw new IllegalStateException("Sender email (fromEmail) is not configured");
                }

                SendEmailRequest request = SendEmailRequest.builder()
                                .destination(Destination.builder()
                                                .toAddresses(toEmail)
                                                .build())
                                .message(Message.builder()
                                                .subject(Content.builder().data(subject).build())
                                                .body(Body.builder()
                                                                .html(Content.builder().data(htmlBody).build())
                                                                .build())
                                                .build())
                                .source(fromEmail)
                                .build();

                sesClient.sendEmail(request);
        }
}
