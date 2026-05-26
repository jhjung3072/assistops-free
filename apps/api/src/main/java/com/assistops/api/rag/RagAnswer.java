package com.assistops.api.rag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "rag_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagAnswer {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(nullable = false, columnDefinition = "text")
	private String question;

	@Column(nullable = false, columnDefinition = "text")
	private String answer;

	@Column(nullable = false, length = 255)
	private String model;

	@Column(name = "top_k", nullable = false)
	private int topK;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "total_ms")
	private Long totalMs;

	@Column(name = "query_embedding_ms")
	private Long queryEmbeddingMs;

	@Column(name = "vector_search_ms")
	private Long vectorSearchMs;

	@Column(name = "prompt_build_ms")
	private Long promptBuildMs;

	@Column(name = "chat_generation_ms")
	private Long chatGenerationMs;

	@Column(name = "answer_persist_ms")
	private Long answerPersistMs;

	@Column(name = "source_count")
	private Integer sourceCount;

	@Column(name = "prompt_context_char_count")
	private Integer promptContextCharCount;

	@Column(name = "answer_char_count")
	private Integer answerCharCount;

	public RagAnswer(UUID workspaceId, UUID userId, String question, String answer, String model, int topK) {
		this.id = UUID.randomUUID();
		this.workspaceId = workspaceId;
		this.userId = userId;
		this.question = question;
		this.answer = answer;
		this.model = model;
		this.topK = topK;
	}

	public void updateLatencyMetrics(RagLatencyMetrics metrics) {
		this.totalMs = metrics.totalMs();
		this.queryEmbeddingMs = metrics.queryEmbeddingMs();
		this.vectorSearchMs = metrics.vectorSearchMs();
		this.promptBuildMs = metrics.promptBuildMs();
		this.chatGenerationMs = metrics.chatGenerationMs();
		this.answerPersistMs = metrics.answerPersistMs();
		this.sourceCount = metrics.sourceCount();
		this.promptContextCharCount = metrics.promptContextCharCount();
		this.answerCharCount = metrics.answerCharCount();
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
