// package com.spiritedhub.spiritedhub.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

// @Service
// public class EmailService {

//     @Autowired
//     private JavaMailSender mailSender;

//     public void sendEmail(String to, String subject, String text) {
//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setTo(to);
//         message.setSubject(subject);
//         message.setText(text);
//         mailSender.send(message);
//     }
// }

package com.spiritedhub.spiritedhub.service;

import com.resend.Resend;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String message) {
        try {
            Resend resend = new Resend(apiKey);

            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html("<p>" + message + "</p>")
                    .build();

            resend.emails().send(request);

        } catch (Exception e) {
            throw new RuntimeException("Email failed: " + e.getMessage());
        }
    }
}
