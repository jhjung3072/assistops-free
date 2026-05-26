package com.assistops.api.rag.generation;

public record RagGenerationResult(
	String answer,
	long promptBuildMs,
	long chatGenerationMs,
	int promptContextCharCount
) {
}
