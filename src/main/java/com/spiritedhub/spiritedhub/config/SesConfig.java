package com.spiritedhub.spiritedhub.config;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SesConfig {

        @Value("${AWS_ACCESS_KEY_ID}")
        private String accessKey;

        @Value("${AWS_SECRET_ACCESS_KEY}")
        private String secretKey;

        @Value("${AWS_REGION}")
        private String region;

        @Bean
        public SesClient sesClient() {
                AwsBasicCredentials creds = AwsBasicCredentials.create(
                                System.getenv("AWS_ACCESS_KEY_ID"),
                                System.getenv("AWS_SECRET_ACCESS_KEY"));
                return SesClient.builder()
                                .credentialsProvider(StaticCredentialsProvider.create(creds))
                                .region(Region.of(System.getenv("AWS_REGION")))
                                .build();
        }
}
