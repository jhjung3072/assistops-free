package com.assistops.api.prompt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromptTemplateResponse(
	UUID id,
	UUID workspaceId,
	String name,
	String description,
	PromptType type,
	UUID activeVersionId,
	PromptVersionResponse activeVersion,
	UUID createdBy,
	Instant createdAt,
	Instant updatedAt
) {

	public static PromptTemplateResponse from(PromptTemplate template, PromptVersion activeVersion) {
		return new PromptTemplateResponse(
			template.getId(),
			template.getWorkspaceId(),
			template.getName(),
			template.getDescription(),
			template.getType(),
			template.getActiveVersionId(),
			activeVersion == null ? null : PromptVersionResponse.from(activeVersion, template.getActiveVersionId()),
			template.getCreatedBy(),
			template.getCreatedAt(),
			template.getUpdatedAt()
		);
	}

	public static List<PromptTemplateResponse> from(
		List<PromptTemplate> templates,
		List<PromptVersion> activeVersions
	) {
		return templates.stream()
			.map(template -> from(
				template,
				activeVersions.stream()
					.filter(version -> version.getId().equals(template.getActiveVersionId()))
					.findFirst()
					.orElse(null)
			))
			.toList();
	}
}
