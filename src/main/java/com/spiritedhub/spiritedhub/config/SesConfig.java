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

        @Value("${aws.accessKey}")
        private String accessKey;

        @Value("${aws.secretKey}")
        private String secretKey;

        @Value("${aws.region}")
        private String region;

        @Bean
        public SesClient sesClient() {
                return SesClient.builder()
                                .credentialsProvider(
                                                StaticCredentialsProvider.create(
                                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .region(Region.of(region))
                                .build();
        }
}
