package com.assistops.api.prompt;

import java.util.List;

public record PromptTemplateListResponse(
	List<PromptTemplateResponse> prompts
) {
}
