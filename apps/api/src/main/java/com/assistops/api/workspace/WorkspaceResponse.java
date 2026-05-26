package com.assistops.api.workspace;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceResponse(
	UUID id,
	String name,
	String slug,
	Instant createdAt,
	Instant updatedAt
) {

	public static WorkspaceResponse from(Workspace workspace) {
		return new WorkspaceResponse(
			workspace.getId(),
			workspace.getName(),
			workspace.getSlug(),
			workspace.getCreatedAt(),
			workspace.getUpdatedAt()
		);
	}
}
