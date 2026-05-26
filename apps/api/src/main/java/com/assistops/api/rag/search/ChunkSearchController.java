package com.assistops.api.rag.search;

import com.assistops.api.global.exception.UnauthorizedException;
import com.assistops.api.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search")
@RestController
@RequestMapping("/api/search")
public class ChunkSearchController {

	private final ChunkSearchService chunkSearchService;

	public ChunkSearchController(ChunkSearchService chunkSearchService) {
		this.chunkSearchService = chunkSearchService;
	}

	@Operation(summary = "Search similar document chunks")
	@PostMapping("/chunks")
	public ChunkSearchResponse searchChunks(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody ChunkSearchRequest request
	) {
		if (userDetails == null) {
			throw new UnauthorizedException("Authentication is required.");
		}

		return chunkSearchService.search(userDetails.getUser(), request);
	}
}
