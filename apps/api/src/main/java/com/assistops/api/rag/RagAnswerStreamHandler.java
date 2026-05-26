package com.assistops.api.rag;

import com.assistops.api.rag.search.ChunkSearchResult;

public interface RagAnswerStreamHandler {

	default void onSource(ChunkSearchResult source) {
	}

	default void onToken(String token) {
	}
}
