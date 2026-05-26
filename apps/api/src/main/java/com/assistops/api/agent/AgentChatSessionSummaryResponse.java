package com.assistops.api.agent;

import java.time.Instant;
import java.util.UUID;

public record AgentChatSessionSummaryResponse(
	UUID id,
	UUID workspaceId,
	UUID userId,
	String title,
	Instant createdAt,
	Instant updatedAt
) {

	public static AgentChatSessionSummaryResponse from(AgentChatSession session) {
		return new AgentChatSessionSummaryResponse(
			session.getId(),
			session.getWorkspaceId(),
			session.getUserId(),
			session.getTitle(),
			session.getCreatedAt(),
			session.getUpdatedAt()
		);
	}
}
