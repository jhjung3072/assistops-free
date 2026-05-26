package com.assistops.api.rag.search;

import com.assistops.api.rag.DocumentChunkVectorRepository;
import java.util.UUID;

public record ChunkSearchResult(
	UUID documentId,
	String documentName,
	UUID chunkId,
	int chunkIndex,
	String content,
	double score,
	double distance,
	String embeddingModel
) {

	public static ChunkSearchResult from(DocumentChunkVectorRepository.ChunkSearchRow row) {
		return new ChunkSearchResult(
			row.documentId(),
			row.documentName(),
			row.chunkId(),
			row.chunkIndex(),
			row.content(),
			row.score(),
			row.distance(),
			row.embeddingModel()
		);
	}
}
