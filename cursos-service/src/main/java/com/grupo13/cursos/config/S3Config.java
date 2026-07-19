package com.grupo13.cursos.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Cliente S3 (AWS SDK v2). Sin credenciales hardcodeadas: usa variables de entorno
 * (con session token en AWS Academy) o el rol IAM de la EC2 (DefaultCredentialsProvider).
 */
@Slf4j
@Configuration
public class S3Config {

    @Value("${aws.region:us-east-1}")
    private String region;
    @Value("${aws.accessKeyId:}")
    private String accessKeyId;
    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;
    @Value("${aws.sessionToken:}")
    private String sessionToken;

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider provider;
        if (accessKeyId != null && !accessKeyId.isBlank()
                && secretAccessKey != null && !secretAccessKey.isBlank()) {
            if (sessionToken != null && !sessionToken.isBlank()) {
                log.info("S3Client con credenciales temporales (session token) en {}", region);
                provider = StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken));
            } else {
                log.info("S3Client con credenciales estaticas en {}", region);
                provider = StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey));
            }
        } else {
            log.info("S3Client con DefaultCredentialsProvider (rol IAM) en {}", region);
            provider = DefaultCredentialsProvider.create();
        }
        return S3Client.builder().region(Region.of(region)).credentialsProvider(provider).build();
    }
}
