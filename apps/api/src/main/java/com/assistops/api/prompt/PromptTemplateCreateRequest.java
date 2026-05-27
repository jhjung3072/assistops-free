package com.assistops.api.prompt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PromptTemplateCreateRequest(
	UUID workspaceId,
	@NotBlank(message = "name is required.")
	String name,
	String description,
	@NotNull(message = "type is required.")
	PromptType type,
	String systemPrompt,
	String userPromptTemplate,
	String contextTemplate,
	String model
) {
}
