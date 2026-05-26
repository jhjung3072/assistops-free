package com.assistops.api.document;

import java.time.Instant;
import java.util.UUID;

public record DocumentChunkResponse(
	UUID id,
	UUID documentId,
	UUID workspaceId,
	int chunkIndex,
	String content,
	Integer tokenCount,
	int charCount,
	Instant embeddedAt,
	String embeddingModel,
	Instant createdAt
) {

	public static DocumentChunkResponse from(DocumentChunk chunk) {
		return new DocumentChunkResponse(
			chunk.getId(),
			chunk.getDocumentId(),
			chunk.getWorkspaceId(),
			chunk.getChunkIndex(),
			chunk.getContent(),
			chunk.getTokenCount(),
			chunk.getCharCount(),
			chunk.getEmbeddedAt(),
			chunk.getEmbeddingModel(),
			chunk.getCreatedAt()
		);
	}
}
