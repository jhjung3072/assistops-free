package com.assistops.api.rag.generation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.generation")
public record RagProperties(
	String chatModel,
	int defaultTopK,
	int minTopK,
	int maxTopK,
	int contextChunkMaxChars,
	int contextTotalMaxChars,
	int numPredict,
	double temperature,
	double topP,
	String keepAlive
) {
}
