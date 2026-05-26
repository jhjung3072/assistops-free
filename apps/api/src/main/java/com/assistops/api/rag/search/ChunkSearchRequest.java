package com.assistops.api.rag.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ChunkSearchRequest(
	@NotBlank(message = "Query is required.")
	String query,

	@Min(value = 1, message = "topK must be at least 1.")
	@Max(value = 20, message = "topK must be 20 or smaller.")
	Integer topK,

	UUID workspaceId
) {
}
