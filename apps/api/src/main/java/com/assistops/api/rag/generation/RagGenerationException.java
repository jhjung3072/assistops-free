package com.assistops.api.rag.generation;

public class RagGenerationException extends RuntimeException {

	public RagGenerationException(String message) {
		super(message);
	}

	public RagGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
