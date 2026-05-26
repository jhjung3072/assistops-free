package com.assistops.api.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "documents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(name = "uploaded_by", nullable = false)
	private UUID uploadedBy;

	@Column(name = "original_filename", nullable = false, length = 255)
	private String originalFilename;

	@Column(name = "stored_object_key", nullable = false, unique = true, length = 512)
	private String storedObjectKey;

	@Column(name = "content_type", length = 255)
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private DocumentStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "processed_at")
	private Instant processedAt;

	@Column(name = "processing_error", columnDefinition = "text")
	private String processingError;

	@Column(name = "chunk_count", nullable = false)
	private int chunkCount;

	@Column(name = "embedded_at")
	private Instant embeddedAt;

	@Column(name = "embedded_chunk_count", nullable = false)
	private int embeddedChunkCount;

	@Enumerated(EnumType.STRING)
	@Column(name = "embedding_status", nullable = false, length = 40)
	private DocumentEmbeddingStatus embeddingStatus;

	@Column(name = "embedding_error", columnDefinition = "text")
	private String embeddingError;

	public Document(
		UUID workspaceId,
		UUID uploadedBy,
		String originalFilename,
		String storedObjectKey,
		String contentType,
		long sizeBytes
	) {
		this.id = UUID.randomUUID();
		this.workspaceId = workspaceId;
		this.uploadedBy = uploadedBy;
		this.originalFilename = originalFilename;
		this.storedObjectKey = storedObjectKey;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.status = DocumentStatus.UPLOADED;
		this.embeddingStatus = DocumentEmbeddingStatus.NOT_EMBEDDED;
	}

	public void markDeleted() {
		this.status = DocumentStatus.DELETED;
		this.updatedAt = Instant.now();
	}

	public void markProcessing() {
		this.status = DocumentStatus.PROCESSING;
		this.processedAt = null;
		this.processingError = null;
		this.chunkCount = 0;
		resetEmbedding();
		this.updatedAt = Instant.now();
	}

	public void markProcessed(int chunkCount) {
		this.status = DocumentStatus.PROCESSED;
		this.processedAt = Instant.now();
		this.processingError = null;
		this.chunkCount = chunkCount;
		resetEmbedding();
		this.updatedAt = Instant.now();
	}

	public void markFailed(String processingError) {
		this.status = DocumentStatus.FAILED;
		this.processedAt = null;
		this.processingError = processingError;
		this.chunkCount = 0;
		resetEmbedding();
		this.updatedAt = Instant.now();
	}

	public void markEmbedding() {
		this.embeddingStatus = DocumentEmbeddingStatus.EMBEDDING;
		this.embeddedAt = null;
		this.embeddedChunkCount = 0;
		this.embeddingError = null;
		this.updatedAt = Instant.now();
	}

	public void markEmbedded(int embeddedChunkCount) {
		this.embeddingStatus = DocumentEmbeddingStatus.EMBEDDED;
		this.embeddedAt = Instant.now();
		this.embeddedChunkCount = embeddedChunkCount;
		this.embeddingError = null;
		this.updatedAt = Instant.now();
	}

	public void markEmbeddingFailed(String embeddingError) {
		this.embeddingStatus = DocumentEmbeddingStatus.EMBEDDING_FAILED;
		this.embeddedAt = null;
		this.embeddedChunkCount = 0;
		this.embeddingError = embeddingError;
		this.updatedAt = Instant.now();
	}

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();

		if (id == null) {
			id = UUID.randomUUID();
		}
		if (status == null) {
			status = DocumentStatus.UPLOADED;
		}
		if (embeddingStatus == null) {
			embeddingStatus = DocumentEmbeddingStatus.NOT_EMBEDDED;
		}
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	private void resetEmbedding() {
		embeddedAt = null;
		embeddedChunkCount = 0;
		embeddingStatus = DocumentEmbeddingStatus.NOT_EMBEDDED;
		embeddingError = null;
	}
}
