package com.assistops.api.document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
	UUID id,
	UUID workspaceId,
	UUID uploadedBy,
	String originalFilename,
	String contentType,
	long sizeBytes,
	DocumentStatus status,
	int chunkCount,
	Instant processedAt,
	String processingError,
	DocumentEmbeddingStatus embeddingStatus,
	int embeddedChunkCount,
	Instant embeddedAt,
	String embeddingError,
	Instant createdAt,
	Instant updatedAt
) {

	public static DocumentResponse from(Document document) {
		return new DocumentResponse(
			document.getId(),
			document.getWorkspaceId(),
			document.getUploadedBy(),
			document.getOriginalFilename(),
			document.getContentType(),
			document.getSizeBytes(),
			document.getStatus(),
			document.getChunkCount(),
			document.getProcessedAt(),
			document.getProcessingError(),
			document.getEmbeddingStatus(),
			document.getEmbeddedChunkCount(),
			document.getEmbeddedAt(),
			document.getEmbeddingError(),
			document.getCreatedAt(),
			document.getUpdatedAt()
		);
	}
}
