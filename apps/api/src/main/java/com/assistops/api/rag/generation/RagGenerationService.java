package com.assistops.api.rag.generation;

import com.assistops.api.prompt.PromptVersion;
import com.assistops.api.rag.search.ChunkSearchResult;
import java.util.List;
import java.util.function.Consumer;

public interface RagGenerationService {

	RagGenerationResult generateAnswer(String question, List<ChunkSearchResult> sources, PromptVersion promptVersion);

	RagGenerationResult generateAnswerStream(
		String question,
		List<ChunkSearchResult> sources,
		PromptVersion promptVersion,
		Consumer<String> tokenConsumer
	);

	String modelName();
}
