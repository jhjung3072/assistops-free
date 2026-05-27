package com.assistops.api.prompt;

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
@Table(name = "prompt_templates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptTemplate {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private PromptType type;

	@Column(name = "active_version_id")
	private UUID activeVersionId;

	@Column(name = "created_by", nullable = false)
	private UUID createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	public PromptTemplate(
		UUID workspaceId,
		String name,
		String description,
		PromptType type,
		UUID createdBy
	) {
		this.id = UUID.randomUUID();
		this.workspaceId = workspaceId;
		this.name = name;
		this.description = description;
		this.type = type;
		this.createdBy = createdBy;
	}

	public void activate(UUID activeVersionId) {
		this.activeVersionId = activeVersionId;
		this.updatedAt = Instant.now();
	}

	public void markDeleted() {
		this.deletedAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();

		if (id == null) {
			id = UUID.randomUUID();
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
