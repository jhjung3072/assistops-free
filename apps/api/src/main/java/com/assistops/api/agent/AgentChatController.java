package com.assistops.api.agent;

import com.assistops.api.global.exception.UnauthorizedException;
import com.assistops.api.global.security.CustomUserDetails;
import com.assistops.api.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Agent Chat")
@RestController
@RequestMapping("/api/agent/sessions")
public class AgentChatController {

	private final AgentChatService agentChatService;

	public AgentChatController(AgentChatService agentChatService) {
		this.agentChatService = agentChatService;
	}

	@Operation(summary = "Create agent chat session")
	@PostMapping
	public AgentChatSessionDetailResponse createSession(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody AgentChatSessionCreateRequest request
	) {
		return agentChatService.createSession(currentUser(userDetails), request);
	}

	@Operation(summary = "List agent chat sessions")
	@GetMapping
	public AgentChatSessionListResponse sessions(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return agentChatService.getSessions(currentUser(userDetails));
	}

	@Operation(summary = "Get agent chat session")
	@GetMapping("/{id}")
	public AgentChatSessionDetailResponse session(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		return agentChatService.getSession(currentUser(userDetails), id);
	}

	@Operation(summary = "Send agent chat message")
	@PostMapping("/{id}/messages")
	public AgentChatSessionDetailResponse sendMessage(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id,
		@Valid @RequestBody AgentChatMessageRequest request
	) {
		return agentChatService.sendMessage(currentUser(userDetails), id, request);
	}

	@Operation(summary = "Delete agent chat session")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSession(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		agentChatService.deleteSession(currentUser(userDetails), id);
		return ResponseEntity.noContent().build();
	}

	private User currentUser(CustomUserDetails userDetails) {
		if (userDetails == null) {
			throw new UnauthorizedException("Authentication is required.");
		}

		return userDetails.getUser();
	}
}
