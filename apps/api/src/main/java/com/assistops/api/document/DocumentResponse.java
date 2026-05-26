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
			document.getCreatedAt(),
			document.getUpdatedAt()
		);
	}
}
