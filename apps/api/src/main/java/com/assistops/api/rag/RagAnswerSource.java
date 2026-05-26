package com.assistops.api.rag;

import com.assistops.api.rag.search.ChunkSearchResult;
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
@Table(name = "rag_answer_sources")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagAnswerSource {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "rag_answer_id", nullable = false)
	private UUID ragAnswerId;

	@Column(name = "document_id", nullable = false)
	private UUID documentId;

	@Column(name = "document_name", nullable = false, length = 255)
	private String documentName;

	@Column(name = "chunk_id", nullable = false)
	private UUID chunkId;

	@Column(name = "chunk_index", nullable = false)
	private int chunkIndex;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column
	private Double score;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public RagAnswerSource(
		UUID ragAnswerId,
		UUID documentId,
		String documentName,
		UUID chunkId,
		int chunkIndex,
		String content,
		Double score
	) {
		this.id = UUID.randomUUID();
		this.ragAnswerId = ragAnswerId;
		this.documentId = documentId;
		this.documentName = documentName;
		this.chunkId = chunkId;
		this.chunkIndex = chunkIndex;
		this.content = content;
		this.score = score;
	}

	public static RagAnswerSource from(RagAnswer answer, ChunkSearchResult result) {
		return new RagAnswerSource(
			answer.getId(),
			result.documentId(),
			result.documentName(),
			result.chunkId(),
			result.chunkIndex(),
			result.content(),
			result.score()
		);
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
