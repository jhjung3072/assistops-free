package com.assistops.api.rag;

import java.time.Instant;
import java.util.UUID;

public record RagAnswerSummary(
	UUID answerId,
	UUID workspaceId,
	String question,
	String answerPreview,
	String model,
	int topK,
	long sourceCount,
	Long totalMs,
	Instant createdAt
) {

	private static final int ANSWER_PREVIEW_LIMIT = 240;

	public static RagAnswerSummary from(RagAnswer answer, long sourceCount) {
		String preview = answer.getAnswer();
		if (preview.length() > ANSWER_PREVIEW_LIMIT) {
			preview = preview.substring(0, ANSWER_PREVIEW_LIMIT) + "...";
		}

		return new RagAnswerSummary(
			answer.getId(),
			answer.getWorkspaceId(),
			answer.getQuestion(),
			preview,
			answer.getModel(),
			answer.getTopK(),
			sourceCount,
			answer.getTotalMs(),
			answer.getCreatedAt()
		);
	}
}
