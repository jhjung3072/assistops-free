package com.assistops.api.prompt;

import jakarta.validation.constraints.NotBlank;

public record PromptVersionCreateRequest(
	@NotBlank(message = "systemPrompt is required.")
	String systemPrompt,
	@NotBlank(message = "userPromptTemplate is required.")
	String userPromptTemplate,
	String contextTemplate,
	String model
) {
}
