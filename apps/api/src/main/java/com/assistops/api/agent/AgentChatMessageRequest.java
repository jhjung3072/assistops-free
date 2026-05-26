package com.assistops.api.agent;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AgentChatMessageRequest(
	@NotBlank(message = "Message content is required.")
	String content,

	@Min(value = 1, message = "topK must be at least 1.")
	@Max(value = 8, message = "topK must be 8 or smaller.")
	Integer topK
) {
}
