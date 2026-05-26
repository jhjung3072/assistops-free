package com.assistops.api.rag;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RagAnswerResponse(
	UUID answerId,
	UUID workspaceId,
	UUID userId,
	String question,
	String answer,
	String model,
	int topK,
	Instant createdAt,
	List<RagAnswerSourceResponse> sources,
	RagLatencyMetrics latencyMetrics
) {

	public static RagAnswerResponse from(RagAnswer answer, List<RagAnswerSource> sources) {
		return from(answer, sources, RagLatencyMetrics.from(answer));
	}

	public static RagAnswerResponse from(
		RagAnswer answer,
		List<RagAnswerSource> sources,
		RagLatencyMetrics latencyMetrics
	) {
		return new RagAnswerResponse(
			answer.getId(),
			answer.getWorkspaceId(),
			answer.getUserId(),
			answer.getQuestion(),
			answer.getAnswer(),
			answer.getModel(),
			answer.getTopK(),
			answer.getCreatedAt(),
			sources.stream()
				.map(RagAnswerSourceResponse::from)
				.toList(),
			latencyMetrics
		);
	}
}
