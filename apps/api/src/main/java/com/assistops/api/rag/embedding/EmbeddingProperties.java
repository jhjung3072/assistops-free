package com.assistops.api.rag.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.embedding")
public record EmbeddingProperties(
	String model,
	int dimension
) {
}
