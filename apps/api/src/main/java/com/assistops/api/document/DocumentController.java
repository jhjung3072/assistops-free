package com.assistops.api.document;

import com.assistops.api.global.exception.UnauthorizedException;
import com.assistops.api.global.security.CustomUserDetails;
import com.assistops.api.rag.DocumentEmbeddingResponse;
import com.assistops.api.rag.DocumentEmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Documents")
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	private final DocumentService documentService;
	private final DocumentProcessingService documentProcessingService;
	private final DocumentEmbeddingService documentEmbeddingService;

	public DocumentController(
		DocumentService documentService,
		DocumentProcessingService documentProcessingService,
		DocumentEmbeddingService documentEmbeddingService
	) {
		this.documentService = documentService;
		this.documentProcessingService = documentProcessingService;
		this.documentEmbeddingService = documentEmbeddingService;
	}

	@Operation(summary = "Upload document")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DocumentUploadResponse upload(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestPart("file") MultipartFile file,
		@RequestParam(required = false) UUID workspaceId
	) {
		return documentService.upload(currentUser(userDetails), file, workspaceId);
	}

	@Operation(summary = "List documents")
	@GetMapping
	public DocumentListResponse list(@AuthenticationPrincipal CustomUserDetails userDetails) {
		// Workspace switcher가 추가되면 workspaceId query filter를 도입할 예정이다.
		return documentService.getDocuments(currentUser(userDetails));
	}

	@Operation(summary = "Get document")
	@GetMapping("/{id}")
	public DocumentResponse get(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		return documentService.getDocument(currentUser(userDetails), id);
	}

	@Operation(summary = "Download document")
	@GetMapping("/{id}/download")
	public ResponseEntity<InputStreamResource> download(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		DocumentDownload download = documentService.download(currentUser(userDetails), id);
		Document document = download.document();
		MediaType mediaType = StringUtils.hasText(document.getContentType())
			? MediaType.parseMediaType(document.getContentType())
			: MediaType.APPLICATION_OCTET_STREAM;

		return ResponseEntity.ok()
			.contentType(mediaType)
			.contentLength(document.getSizeBytes())
			.header(
				HttpHeaders.CONTENT_DISPOSITION,
				ContentDisposition.attachment()
					.filename(document.getOriginalFilename(), StandardCharsets.UTF_8)
					.build()
					.toString()
			)
			.body(new InputStreamResource(download.inputStream()));
	}

	@Operation(summary = "Process document")
	@PostMapping("/{id}/process")
	public DocumentProcessingResponse process(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		return documentProcessingService.process(currentUser(userDetails), id);
	}

	@Operation(summary = "List document chunks")
	@GetMapping("/{id}/chunks")
	public DocumentChunkListResponse chunks(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		return documentProcessingService.getChunks(currentUser(userDetails), id);
	}

	@Operation(summary = "Embed document chunks")
	@PostMapping("/{id}/embed")
	public DocumentEmbeddingResponse embed(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		return documentEmbeddingService.embed(currentUser(userDetails), id);
	}

	@Operation(summary = "Delete document")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable UUID id
	) {
		documentService.delete(currentUser(userDetails), id);
		return ResponseEntity.noContent().build();
	}

	private com.assistops.api.user.User currentUser(CustomUserDetails userDetails) {
		if (userDetails == null) {
			throw new UnauthorizedException("Authentication is required.");
		}

		return userDetails.getUser();
	}
}
