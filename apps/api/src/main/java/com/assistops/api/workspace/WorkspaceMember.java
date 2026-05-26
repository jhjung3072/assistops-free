package com.assistops.api.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
	name = "workspace_members",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_workspace_members_workspace_user",
		columnNames = {"workspace_id", "user_id"}
	)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMember {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private WorkspaceMemberRole role;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public WorkspaceMember(UUID workspaceId, UUID userId, WorkspaceMemberRole role) {
		this.id = UUID.randomUUID();
		this.workspaceId = workspaceId;
		this.userId = userId;
		this.role = role;
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
