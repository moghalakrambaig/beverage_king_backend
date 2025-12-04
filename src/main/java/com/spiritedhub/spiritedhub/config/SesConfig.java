package com.spiritedhub.spiritedhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SesConfig {

        @Value("${AWS_ACCESS_KEY_ID}")
        private String accessKey;

        @Value("${AWS_SECRET_ACCESS_KEY}")
        private String secretKey;

        @Value("${AWS_REGION}")
        private String region;

        @PostConstruct
        public void debugEnv() {
                System.out.println("=== ENVIRONMENT VARIABLES DEBUG ===");
                System.out.println("AWS_ACCESS_KEY_ID from System.getenv(): " + System.getenv("AWS_ACCESS_KEY_ID"));
                System.out.println("AWS_SECRET_ACCESS_KEY from System.getenv(): " +
                                (System.getenv("AWS_SECRET_ACCESS_KEY") != null ? "[SET]" : "NULL"));
                System.out.println("AWS_REGION from System.getenv(): " + System.getenv("AWS_REGION"));

                System.out.println("\n=== Checking all AWS/AMS prefixed vars ===");
                System.getenv().forEach((key, value) -> {
                        if (key.toUpperCase().contains("AWS") || key.toUpperCase().contains("AMS")) {
                                System.out.println(key + " = " +
                                                (key.contains("SECRET") || key.contains("KEY") ? "[REDACTED]" : value));
                        }
                });
        }

        @Bean
        public SesClient sesClient() {

                System.out.println("SES KEY FROM ENV = " + accessKey); // TEMP — remove after testing

                AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

                return SesClient.builder()
                                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                                .region(Region.of(region))
                                .build();
        }
}
