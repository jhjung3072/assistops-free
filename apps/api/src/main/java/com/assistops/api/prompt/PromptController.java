package com.assistops.api.prompt;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Prompts")
@RestController
@RequestMapping("/api/prompts")
public class PromptController {

	private final PromptService promptService;

	public PromptController(PromptService promptService) {
		this.promptService = promptService;
	}

	@Operation(summary = "Create prompt template")
	@PostMapping
	public PromptTemplateResponse createTemplate(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody PromptTemplateCreateRequest request
	) {
		return promptService.createTemplate(currentUser(userDetails), request);
	}

	@Operation(summary = "List prompt templates")
	@GetMapping
	public PromptTemplateListResponse templates(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) PromptType type
	) {
		return promptService.getTemplates(currentUser(userDetails), type);
	}

	@Operation(summary = "Get active prompt template")
	@GetMapping("/active")
	public PromptTemplateResponse activeTemplate(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam PromptType type,
		@RequestParam(required = false) UUID workspaceId
	) {
		return promptService.getActiveTemplate(currentUser(userDetails), type, workspaceId);
	}

	@Operation(summary = "Get prompt template")
	@GetMapping("/{id}")
	public PromptTemplateResponse template(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		return promptService.getTemplate(currentUser(userDetails), id);
	}

	@Operation(summary = "Delete prompt template")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTemplate(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		promptService.deleteTemplate(currentUser(userDetails), id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Create prompt version")
	@PostMapping("/{id}/versions")
	public PromptVersionResponse createVersion(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id,
		@Valid @RequestBody PromptVersionCreateRequest request
	) {
		return promptService.createVersion(currentUser(userDetails), id, request);
	}

	@Operation(summary = "List prompt versions")
	@GetMapping("/{id}/versions")
	public PromptVersionListResponse versions(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		return promptService.getVersions(currentUser(userDetails), id);
	}

	@Operation(summary = "Activate prompt version")
	@PostMapping("/{id}/versions/{versionId}/activate")
	public PromptVersionResponse activateVersion(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id,
		@PathVariable UUID versionId
	) {
		return promptService.activateVersion(currentUser(userDetails), id, versionId);
	}

	private User currentUser(CustomUserDetails userDetails) {
		if (userDetails == null) {
			throw new UnauthorizedException("Authentication is required.");
		}

		return userDetails.getUser();
	}
}
