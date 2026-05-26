package com.assistops.api.agent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "agent_chat_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentChatSession {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public AgentChatSession(UUID workspaceId, UUID userId, String title) {
		this.id = UUID.randomUUID();
		this.workspaceId = workspaceId;
		this.userId = userId;
		this.title = title;
	}

	public void rename(String title) {
		this.title = title;
		touch();
	}

	public void touch() {
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
