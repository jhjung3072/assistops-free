package com.assistops.api.agent;

import java.time.LocalDateTime;

public record AgentChatSessionSearchCondition(
	String keyword,
	LocalDateTime createdFrom,
	LocalDateTime createdTo,
	Integer page,
	Integer size
) {
}
