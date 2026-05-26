package com.assistops.api.agent;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AgentChatSessionDetailResponse(
	UUID id,
	UUID workspaceId,
	UUID userId,
	String title,
	Instant createdAt,
	Instant updatedAt,
	List<AgentChatMessageResponse> messages
) {

	public static AgentChatSessionDetailResponse from(
		AgentChatSession session,
		List<AgentChatMessage> messages,
		Map<UUID, List<AgentChatMessageSource>> sourcesByMessageId
	) {
		return new AgentChatSessionDetailResponse(
			session.getId(),
			session.getWorkspaceId(),
			session.getUserId(),
			session.getTitle(),
			session.getCreatedAt(),
			session.getUpdatedAt(),
			messages.stream()
				.map(message -> AgentChatMessageResponse.from(
					message,
					sourcesByMessageId.getOrDefault(message.getId(), List.of())
				))
				.toList()
		);
	}
}
