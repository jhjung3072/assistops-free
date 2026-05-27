package com.assistops.api.rag.generation;

public record RagGenerationResult(
	String answer,
	long promptBuildMs,
	long chatGenerationMs,
	int promptContextCharCount,
	String model
) {

	public RagGenerationResult(
		String answer,
		long promptBuildMs,
		long chatGenerationMs,
		int promptContextCharCount
	) {
		this(answer, promptBuildMs, chatGenerationMs, promptContextCharCount, null);
	}
}
