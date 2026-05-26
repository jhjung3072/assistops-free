package com.assistops.api.document.storage;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioStorageProperties.class)
public class MinioConfig {

	@Bean
	@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true")
	public MinioClient minioClient(MinioStorageProperties properties) {
		return MinioClient.builder()
			.endpoint(properties.endpoint())
			.credentials(properties.accessKey(), properties.secretKey())
			.build();
	}
}
