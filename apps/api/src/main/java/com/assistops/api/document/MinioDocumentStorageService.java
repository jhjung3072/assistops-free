package com.assistops.api.document;

import com.assistops.api.document.storage.DocumentStorageException;
import com.assistops.api.document.storage.MinioStorageProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true")
public class MinioDocumentStorageService implements DocumentStorageService {

	private final MinioClient minioClient;
	private final MinioStorageProperties properties;

	public MinioDocumentStorageService(MinioClient minioClient, MinioStorageProperties properties) {
		this.minioClient = minioClient;
		this.properties = properties;
	}

	@Override
	public void upload(String objectKey, MultipartFile file) {
		try (InputStream inputStream = file.getInputStream()) {
			minioClient.putObject(PutObjectArgs.builder()
				.bucket(properties.bucket())
				.object(objectKey)
				.stream(inputStream, file.getSize(), -1)
				.contentType(file.getContentType())
				.build());
		}
		catch (Exception exception) {
			throw new DocumentStorageException("Failed to upload document to object storage.", exception);
		}
	}

	@Override
	public InputStream download(String objectKey) {
		try {
			return minioClient.getObject(GetObjectArgs.builder()
				.bucket(properties.bucket())
				.object(objectKey)
				.build());
		}
		catch (Exception exception) {
			throw new DocumentStorageException("Failed to download document from object storage.", exception);
		}
	}

	@Override
	public void delete(String objectKey) {
		try {
			minioClient.removeObject(RemoveObjectArgs.builder()
				.bucket(properties.bucket())
				.object(objectKey)
				.build());
		}
		catch (Exception exception) {
			throw new DocumentStorageException("Failed to delete document from object storage.", exception);
		}
	}
}
