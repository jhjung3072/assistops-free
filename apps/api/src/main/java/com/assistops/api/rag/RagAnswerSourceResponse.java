package com.assistops.api.rag;

import java.util.UUID;

public record RagAnswerSourceResponse(
	UUID id,
	UUID documentId,
	String documentName,
	UUID chunkId,
	int chunkIndex,
	String content,
	Double score
) {

	public static RagAnswerSourceResponse from(RagAnswerSource source) {
		return new RagAnswerSourceResponse(
			source.getId(),
			source.getDocumentId(),
			source.getDocumentName(),
			source.getChunkId(),
			source.getChunkIndex(),
			source.getContent(),
			source.getScore()
		);
	}
}
