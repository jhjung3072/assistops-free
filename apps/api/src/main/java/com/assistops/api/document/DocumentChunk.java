package com.assistops.api.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "document_chunks",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_document_chunks_document_chunk_index",
		columnNames = {"document_id", "chunk_index"}
	)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentChunk {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "document_id", nullable = false)
	private UUID documentId;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(name = "chunk_index", nullable = false)
	private int chunkIndex;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "token_count")
	private Integer tokenCount;

	@Column(name = "char_count", nullable = false)
	private int charCount;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "embedded_at")
	private Instant embeddedAt;

	@Column(name = "embedding_model", length = 255)
	private String embeddingModel;

	public DocumentChunk(
		UUID documentId,
		UUID workspaceId,
		int chunkIndex,
		String content,
		Integer tokenCount
	) {
		this.id = UUID.randomUUID();
		this.documentId = documentId;
		this.workspaceId = workspaceId;
		this.chunkIndex = chunkIndex;
		this.content = content;
		this.tokenCount = tokenCount;
		this.charCount = content.length();
	}

	public void markEmbedded(String embeddingModel, Instant embeddedAt) {
		this.embeddingModel = embeddingModel;
		this.embeddedAt = embeddedAt;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
