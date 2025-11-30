package com.spiritedhub.spiritedhub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

        @Value("${aws.ses.fromEmail}")
        private String fromEmail;

        public void sendPasswordResetEmail(String toEmail, String resetLink) {
                String subject = "Reset Your Password";
                String htmlBody = "<h2>Password Reset</h2>"
                                + "<p>Click the link below to reset your password:</p>"
                                + "<a href=\"" + resetLink + "\">Reset Password</a><br>"
                                + "<p>If you didn’t request this, ignore this email.</p>";

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

        public void sendHtmlEmail(String to, String subject, String htmlBody) {

                SendEmailRequest request = SendEmailRequest.builder()
                                .destination(Destination.builder()
                                                .toAddresses(to)
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
