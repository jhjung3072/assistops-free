package com.assistops.api.rag;

import com.assistops.api.prompt.PromptVersionMetadata;
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
	UUID promptVersionId,
	String promptTemplateName,
	Integer promptVersion,
	Instant createdAt,
	List<RagAnswerSourceResponse> sources,
	RagLatencyMetrics latencyMetrics
) {

	public static RagAnswerResponse from(RagAnswer answer, List<RagAnswerSource> sources) {
		return from(answer, sources, RagLatencyMetrics.from(answer), PromptVersionMetadata.empty(answer.getPromptVersionId()));
	}

	public static RagAnswerResponse from(
		RagAnswer answer,
		List<RagAnswerSource> sources,
		RagLatencyMetrics latencyMetrics
	) {
		return from(answer, sources, latencyMetrics, PromptVersionMetadata.empty(answer.getPromptVersionId()));
	}

	public static RagAnswerResponse from(
		RagAnswer answer,
		List<RagAnswerSource> sources,
		RagLatencyMetrics latencyMetrics,
		PromptVersionMetadata promptVersionMetadata
	) {
		return new RagAnswerResponse(
			answer.getId(),
			answer.getWorkspaceId(),
			answer.getUserId(),
			answer.getQuestion(),
			answer.getAnswer(),
			answer.getModel(),
			answer.getTopK(),
			answer.getPromptVersionId(),
			promptVersionMetadata == null ? null : promptVersionMetadata.promptTemplateName(),
			promptVersionMetadata == null ? null : promptVersionMetadata.promptVersion(),
			answer.getCreatedAt(),
			sources.stream()
				.map(RagAnswerSourceResponse::from)
				.toList(),
			latencyMetrics
		);
	}
}
