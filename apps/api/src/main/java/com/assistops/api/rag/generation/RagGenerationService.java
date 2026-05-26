package com.assistops.api.rag.generation;

import com.assistops.api.rag.search.ChunkSearchResult;
import java.util.List;

public interface RagGenerationService {

	RagGenerationResult generateAnswer(String question, List<ChunkSearchResult> sources);

	String modelName();
}
