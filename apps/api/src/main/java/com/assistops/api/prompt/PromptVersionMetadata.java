package com.assistops.api.prompt;

import java.util.UUID;

public record PromptVersionMetadata(
	UUID promptVersionId,
	String promptTemplateName,
	Integer promptVersion
) {

	public static PromptVersionMetadata empty(UUID promptVersionId) {
		return new PromptVersionMetadata(promptVersionId, null, null);
	}
}
