package com.assistops.api.prompt;

import java.time.Instant;
import java.util.UUID;

public record PromptVersionResponse(
	UUID id,
	UUID promptTemplateId,
	int version,
	String systemPrompt,
	String userPromptTemplate,
	String contextTemplate,
	String model,
	UUID createdBy,
	Instant createdAt,
	boolean active
) {

	public static PromptVersionResponse from(PromptVersion version, UUID activeVersionId) {
		return new PromptVersionResponse(
			version.getId(),
			version.getPromptTemplateId(),
			version.getVersion(),
			version.getSystemPrompt(),
			version.getUserPromptTemplate(),
			version.getContextTemplate(),
			version.getModel(),
			version.getCreatedBy(),
			version.getCreatedAt(),
			version.getId().equals(activeVersionId)
		);
	}
}
