package com.assistops.api.document;

import com.assistops.api.document.processing.DocumentChunker;
import com.assistops.api.document.processing.DocumentProcessingException;
import com.assistops.api.document.processing.DocumentTextExtractor;
import com.assistops.api.global.exception.BadRequestException;
import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.user.User;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentProcessingService {

	private final DocumentRepository documentRepository;
	private final DocumentChunkRepository documentChunkRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final DocumentStorageService documentStorageService;
	private final DocumentTextExtractor documentTextExtractor;
	private final DocumentChunker documentChunker;

	public DocumentProcessingService(
		DocumentRepository documentRepository,
		DocumentChunkRepository documentChunkRepository,
		WorkspaceMemberRepository workspaceMemberRepository,
		DocumentStorageService documentStorageService,
		DocumentTextExtractor documentTextExtractor,
		DocumentChunker documentChunker
	) {
		this.documentRepository = documentRepository;
		this.documentChunkRepository = documentChunkRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.documentStorageService = documentStorageService;
		this.documentTextExtractor = documentTextExtractor;
		this.documentChunker = documentChunker;
	}

	@Transactional(noRollbackFor = {BadRequestException.class, DocumentProcessingException.class})
	public DocumentProcessingResponse process(User user, UUID documentId) {
		Document document = findAccessibleDocument(user.getId(), documentId);

		if (document.getStatus() == DocumentStatus.DELETED) {
			throw new BadRequestException("Deleted documents cannot be processed.");
		}

		document.markProcessing();
		documentChunkRepository.deleteByDocumentId(document.getId());

		try (InputStream inputStream = documentStorageService.download(document.getStoredObjectKey())) {
			String text = documentTextExtractor.extract(
				inputStream,
				document.getContentType(),
				document.getOriginalFilename()
			);
			List<DocumentChunker.Chunk> chunks = documentChunker.chunk(text);
			List<DocumentChunk> entities = chunks.stream()
				.map(chunk -> new DocumentChunk(
					document.getId(),
					document.getWorkspaceId(),
					chunk.chunkIndex(),
					chunk.content(),
					chunk.tokenCount()
				))
				.toList();

			documentChunkRepository.saveAll(entities);
			document.markProcessed(entities.size());

			return new DocumentProcessingResponse(DocumentResponse.from(document));
		}
		catch (DocumentProcessingException | BadRequestException exception) {
			document.markFailed(exception.getMessage());
			throw exception;
		}
		catch (Exception exception) {
			String message = "Document processing failed.";
			document.markFailed(message);
			throw new DocumentProcessingException(message, exception);
		}
	}

	@Transactional(readOnly = true)
	public DocumentChunkListResponse getChunks(User user, UUID documentId) {
		Document document = findAccessibleDocument(user.getId(), documentId);

		if (document.getStatus() == DocumentStatus.DELETED) {
			throw new NotFoundException("Document not found.");
		}

		List<DocumentChunkResponse> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId)
			.stream()
			.map(DocumentChunkResponse::from)
			.toList();

		return new DocumentChunkListResponse(chunks);
	}

	private Document findAccessibleDocument(UUID userId, UUID documentId) {
		List<UUID> workspaceIds = workspaceMemberRepository.findByUserId(userId)
			.stream()
			.map(WorkspaceMember::getWorkspaceId)
			.toList();

		if (workspaceIds.isEmpty()) {
			throw new NotFoundException("Workspace membership not found.");
		}

		return documentRepository.findByIdAndWorkspaceIdIn(documentId, workspaceIds)
			.orElseThrow(() -> new NotFoundException("Document not found."));
	}
}
