package ru.tokarev.cloudstorage.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    @Bean
    public MinioClient minioClient(@Value("${minio.root.endpoint}") String endpoint,
                                   @Value("${minio.root.username}") String username,
                                   @Value("${minio.root.password}") String password) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username, password)
                .build();
    }
}

