package com.assistops.api.agent;

import java.util.UUID;

public record AgentChatMessageSourceResponse(
	UUID id,
	UUID documentId,
	String documentName,
	UUID chunkId,
	int chunkIndex,
	String content,
	Double score
) {

	public static AgentChatMessageSourceResponse from(AgentChatMessageSource source) {
		return new AgentChatMessageSourceResponse(
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
