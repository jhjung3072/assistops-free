package com.assistops.api.rag.generation;

import com.assistops.api.rag.search.ChunkSearchResult;
import java.util.List;
import java.util.function.Consumer;

public interface RagGenerationService {

	RagGenerationResult generateAnswer(String question, List<ChunkSearchResult> sources);

	RagGenerationResult generateAnswerStream(
		String question,
		List<ChunkSearchResult> sources,
		Consumer<String> tokenConsumer
	);

	String modelName();
}
