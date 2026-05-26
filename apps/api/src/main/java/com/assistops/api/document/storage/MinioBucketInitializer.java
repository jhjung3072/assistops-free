package com.assistops.api.document.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true")
public class MinioBucketInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(MinioBucketInitializer.class);

	private final MinioClient minioClient;
	private final MinioStorageProperties properties;

	public MinioBucketInitializer(MinioClient minioClient, MinioStorageProperties properties) {
		this.minioClient = minioClient;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
				.bucket(properties.bucket())
				.build());

			if (exists) {
				log.info("MinIO bucket '{}' already exists.", properties.bucket());
				return;
			}

			minioClient.makeBucket(MakeBucketArgs.builder()
				.bucket(properties.bucket())
				.region(properties.region())
				.build());
			log.info("Created MinIO bucket '{}'.", properties.bucket());
		}
		catch (Exception exception) {
			log.error("Failed to initialize MinIO bucket '{}'.", properties.bucket(), exception);
		}
	}
}
