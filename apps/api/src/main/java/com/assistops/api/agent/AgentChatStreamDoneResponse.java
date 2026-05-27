package com.assistops.api.agent;

import java.util.UUID;

public record AgentChatStreamDoneResponse(
	UUID assistantMessageId,
	UUID ragAnswerId,
	UUID promptVersionId,
	String promptTemplateName,
	Integer promptVersion
) {
}
