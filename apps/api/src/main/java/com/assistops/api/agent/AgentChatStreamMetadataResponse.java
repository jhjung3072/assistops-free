package com.assistops.api.agent;

import java.util.UUID;

public record AgentChatStreamMetadataResponse(
	UUID sessionId,
	UUID userMessageId,
	String model
) {
}
