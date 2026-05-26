package com.assistops.api.rag.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OllamaEmbeddingService implements EmbeddingService {

	private static final String DOCUMENT_PREFIX = "search_document: ";
	private static final String QUERY_PREFIX = "search_query: ";

	private final EmbeddingModel embeddingModel;
	private final EmbeddingProperties properties;

	public OllamaEmbeddingService(EmbeddingModel embeddingModel, EmbeddingProperties properties) {
		this.embeddingModel = embeddingModel;
		this.properties = properties;
	}

	@Override
	public float[] embedDocument(String text) {
		return embed(DOCUMENT_PREFIX + normalize(text));
	}

	@Override
	public float[] embedQuery(String query) {
		return embed(QUERY_PREFIX + normalize(query));
	}

	@Override
	public String modelName() {
		return properties.model();
	}

	private float[] embed(String text) {
		try {
			float[] embedding = embeddingModel.embed(text);
			if (embedding == null || embedding.length == 0) {
				throw new EmbeddingException("Embedding result is empty.");
			}
			if (embedding.length != properties.dimension()) {
				throw new EmbeddingException(
					"Embedding dimension mismatch. Expected "
						+ properties.dimension()
						+ " but got "
						+ embedding.length
						+ "."
				);
			}

			return embedding;
		}
		catch (EmbeddingException exception) {
			throw exception;
		}
		catch (RuntimeException exception) {
			throw new EmbeddingException(
				"Failed to create embedding with Ollama. Check that Ollama is running and the embedding model is pulled.",
				exception
			);
		}
	}

	private String normalize(String text) {
		if (!StringUtils.hasText(text)) {
			throw new EmbeddingException("Text for embedding must not be blank.");
		}

		return text.trim();
	}
}
