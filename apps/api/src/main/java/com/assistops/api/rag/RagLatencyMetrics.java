package com.assistops.api.rag;

public record RagLatencyMetrics(
	Long totalMs,
	Long queryEmbeddingMs,
	Long vectorSearchMs,
	Long promptBuildMs,
	Long chatGenerationMs,
	Long answerPersistMs,
	Integer sourceCount,
	Integer promptContextCharCount,
	Integer answerCharCount
) {

	public static RagLatencyMetrics from(RagAnswer answer) {
		return new RagLatencyMetrics(
			answer.getTotalMs(),
			answer.getQueryEmbeddingMs(),
			answer.getVectorSearchMs(),
			answer.getPromptBuildMs(),
			answer.getChatGenerationMs(),
			answer.getAnswerPersistMs(),
			answer.getSourceCount(),
			answer.getPromptContextCharCount(),
			answer.getAnswerCharCount()
		);
	}
}
