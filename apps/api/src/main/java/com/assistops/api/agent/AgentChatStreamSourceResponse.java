package com.assistops.api.agent;

import com.assistops.api.rag.search.ChunkSearchResult;
import java.util.UUID;

public record AgentChatStreamSourceResponse(
	UUID documentId,
	String documentName,
	UUID chunkId,
	int chunkIndex,
	String content,
	Double score
) {

	public static AgentChatStreamSourceResponse from(ChunkSearchResult source) {
		return new AgentChatStreamSourceResponse(
			source.documentId(),
			source.documentName(),
			source.chunkId(),
			source.chunkIndex(),
			source.content(),
			source.score()
		);
	}
}
