package com.assistops.api.agent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AgentChatMessageResponse(
	UUID id,
	UUID sessionId,
	AgentChatRole role,
	String content,
	UUID ragAnswerId,
	String model,
	Long totalMs,
	Long chatGenerationMs,
	Integer sourceCount,
	Instant createdAt,
	List<AgentChatMessageSourceResponse> sources
) {

	public static AgentChatMessageResponse from(
		AgentChatMessage message,
		List<AgentChatMessageSource> sources
	) {
		return new AgentChatMessageResponse(
			message.getId(),
			message.getSessionId(),
			message.getRole(),
			message.getContent(),
			message.getRagAnswerId(),
			message.getModel(),
			message.getTotalMs(),
			message.getChatGenerationMs(),
			message.getSourceCount(),
			message.getCreatedAt(),
			sources.stream()
				.map(AgentChatMessageSourceResponse::from)
				.toList()
		);
	}
}
