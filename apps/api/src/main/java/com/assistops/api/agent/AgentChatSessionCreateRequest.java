package com.assistops.api.agent;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AgentChatSessionCreateRequest(
	@Size(max = 255, message = "Title must be 255 characters or fewer.")
	String title,

	UUID workspaceId
) {
}
