package com.assistops.api.agent;

import java.util.List;

public record AgentChatSessionListResponse(
	List<AgentChatSessionSummaryResponse> sessions
) {
}
