package com.assistops.api.rag.embedding;

public interface EmbeddingService {

	float[] embedDocument(String text);

	float[] embedQuery(String query);

	String modelName();
}
