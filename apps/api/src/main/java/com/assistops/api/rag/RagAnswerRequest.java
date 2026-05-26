package com.assistops.api.rag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RagAnswerRequest(
	@NotBlank(message = "Question is required.")
	String question,

	@Min(value = 1, message = "topK must be at least 1.")
	@Max(value = 8, message = "topK must be 8 or smaller.")
	Integer topK,

	UUID workspaceId
) {
}
