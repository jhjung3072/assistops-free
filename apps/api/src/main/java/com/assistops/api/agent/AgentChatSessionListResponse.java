package com.assistops.api.agent;

import com.assistops.api.global.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public record AgentChatSessionListResponse(
	List<AgentChatSessionSummaryResponse> sessions,
	PageResponse<AgentChatSessionSummaryResponse> page
) {

	public static AgentChatSessionListResponse from(Page<AgentChatSessionSummaryResponse> page) {
		return new AgentChatSessionListResponse(page.getContent(), PageResponse.from(page));
	}
}
