package com.assistops.api.agent;

import com.assistops.api.rag.RagAnswerResponse;
import com.assistops.api.rag.RagLatencyMetrics;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "agent_chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentChatMessage {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private AgentChatRole role;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "rag_answer_id")
	private UUID ragAnswerId;

	@Column(length = 255)
	private String model;

	@Column(name = "total_ms")
	private Long totalMs;

	@Column(name = "chat_generation_ms")
	private Long chatGenerationMs;

	@Column(name = "source_count")
	private Integer sourceCount;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	private AgentChatMessage(UUID sessionId, AgentChatRole role, String content) {
		this.id = UUID.randomUUID();
		this.sessionId = sessionId;
		this.role = role;
		this.content = content;
	}

	public static AgentChatMessage user(UUID sessionId, String content) {
		return new AgentChatMessage(sessionId, AgentChatRole.USER, content);
	}

	public static AgentChatMessage assistant(UUID sessionId, RagAnswerResponse answer) {
		AgentChatMessage message = new AgentChatMessage(sessionId, AgentChatRole.ASSISTANT, answer.answer());
		RagLatencyMetrics metrics = answer.latencyMetrics();

		message.ragAnswerId = answer.answerId();
		message.model = answer.model();
		message.totalMs = metrics == null ? null : metrics.totalMs();
		message.chatGenerationMs = metrics == null ? null : metrics.chatGenerationMs();
		message.sourceCount = metrics == null ? answer.sources().size() : metrics.sourceCount();

		return message;
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
