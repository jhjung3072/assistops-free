package com.assistops.api.prompt;

public record PromptResolvedVersion(
	PromptTemplate template,
	PromptVersion version
) {

	public PromptVersionMetadata metadata() {
		return new PromptVersionMetadata(
			version.getId(),
			template.getName(),
			version.getVersion()
		);
	}
}
