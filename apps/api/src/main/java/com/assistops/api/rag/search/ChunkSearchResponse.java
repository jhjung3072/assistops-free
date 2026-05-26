package com.assistops.api.rag.search;

import java.util.List;

public record ChunkSearchResponse(
	String query,
	int topK,
	List<ChunkSearchResult> results
) {
}
