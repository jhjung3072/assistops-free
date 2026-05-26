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
	}

	public void markDeleted() {
		this.status = DocumentStatus.DELETED;
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
}
