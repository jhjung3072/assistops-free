package com.assistops.api.rag;

import com.assistops.api.global.exception.UnauthorizedException;
import com.assistops.api.global.security.CustomUserDetails;
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

@Tag(name = "RAG")
@RestController
@RequestMapping("/api/rag")
public class RagAnswerController {

	private final RagAnswerService ragAnswerService;

	public RagAnswerController(RagAnswerService ragAnswerService) {
		this.ragAnswerService = ragAnswerService;
	}

	@Operation(summary = "Generate RAG answer")
	@PostMapping("/answer")
	public RagAnswerResponse answer(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody RagAnswerRequest request
	) {
		return ragAnswerService.answer(currentUser(userDetails), request);
	}

	@Operation(summary = "List RAG answers")
	@GetMapping("/answers")
	public RagAnswerListResponse answers(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ragAnswerService.getAnswers(currentUser(userDetails));
	}

	@Operation(summary = "Get RAG answer")
	@GetMapping("/answers/{id}")
	public RagAnswerResponse answerDetail(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		return ragAnswerService.getAnswer(currentUser(userDetails), id);
	}

	@Operation(summary = "Delete RAG answer")
	@DeleteMapping("/answers/{id}")
	public ResponseEntity<Void> deleteAnswer(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		ragAnswerService.deleteAnswer(currentUser(userDetails), id);
		return ResponseEntity.noContent().build();
	}

	private com.assistops.api.user.User currentUser(CustomUserDetails userDetails) {
		if (userDetails == null) {
			throw new UnauthorizedException("Authentication is required.");
		}

		return userDetails.getUser();
	}
}
