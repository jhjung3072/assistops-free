package com.assistops.api.document.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.minio")
public record MinioStorageProperties(
	boolean enabled,
	String endpoint,
	String accessKey,
	String secretKey,
	String bucket,
	String region
) {
}
