package com.assistops.api.prompt;

import java.util.List;

public record PromptVersionListResponse(
	List<PromptVersionResponse> versions
) {
}
