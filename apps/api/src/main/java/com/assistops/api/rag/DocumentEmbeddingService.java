package com.assistops.api.rag;

import com.assistops.api.document.Document;
import com.assistops.api.document.DocumentChunk;
import com.assistops.api.document.DocumentChunkRepository;
import com.assistops.api.document.DocumentResponse;
import com.assistops.api.document.DocumentRepository;
import com.assistops.api.document.DocumentStatus;
import com.assistops.api.global.exception.BadRequestException;
import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.rag.embedding.EmbeddingException;
import com.assistops.api.rag.embedding.EmbeddingProperties;
import com.assistops.api.rag.embedding.EmbeddingService;
import com.assistops.api.user.User;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentEmbeddingService {

	private final DocumentRepository documentRepository;
	private final DocumentChunkRepository documentChunkRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final DocumentChunkVectorRepository documentChunkVectorRepository;
	private final EmbeddingService embeddingService;
	private final EmbeddingProperties embeddingProperties;

	public DocumentEmbeddingService(
		DocumentRepository documentRepository,
		DocumentChunkRepository documentChunkRepository,
		WorkspaceMemberRepository workspaceMemberRepository,
		DocumentChunkVectorRepository documentChunkVectorRepository,
		EmbeddingService embeddingService,
		EmbeddingProperties embeddingProperties
	) {
		this.documentRepository = documentRepository;
		this.documentChunkRepository = documentChunkRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.documentChunkVectorRepository = documentChunkVectorRepository;
		this.embeddingService = embeddingService;
		this.embeddingProperties = embeddingProperties;
	}

	@Transactional(noRollbackFor = EmbeddingException.class)
	public DocumentEmbeddingResponse embed(User user, UUID documentId) {
		Document document = findAccessibleDocument(user.getId(), documentId);

		if (document.getStatus() != DocumentStatus.PROCESSED) {
			throw new BadRequestException("Document must be processed before embedding.");
		}

		List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(document.getId());
		if (chunks.isEmpty()) {
			throw new BadRequestException("Document chunks are required before embedding.");
		}

		document.markEmbedding();

		try {
			int embeddedCount = 0;

			for (DocumentChunk chunk : chunks) {
				float[] embedding = embeddingService.embedDocument(chunk.getContent());
				validateDimension(embedding);

				Instant embeddedAt = Instant.now();
				documentChunkVectorRepository.updateEmbedding(
					chunk.getId(),
					embedding,
					embeddingService.modelName(),
					embeddedAt
				);
				chunk.markEmbedded(embeddingService.modelName(), embeddedAt);
				embeddedCount++;
			}

			document.markEmbedded(embeddedCount);

			return new DocumentEmbeddingResponse(DocumentResponse.from(document));
		}
		catch (EmbeddingException exception) {
			document.markEmbeddingFailed(exception.getMessage());
			throw exception;
		}
		catch (RuntimeException exception) {
			String message = "Document embedding failed.";
			document.markEmbeddingFailed(message);
			throw new EmbeddingException(message, exception);
		}
	}

	private Document findAccessibleDocument(UUID userId, UUID documentId) {
		List<UUID> workspaceIds = workspaceMemberRepository.findByUserId(userId)
			.stream()
			.map(WorkspaceMember::getWorkspaceId)
			.toList();

		if (workspaceIds.isEmpty()) {
			throw new NotFoundException("Workspace membership not found.");
		}

		Document document = documentRepository.findByIdAndWorkspaceIdIn(documentId, workspaceIds)
			.orElseThrow(() -> new NotFoundException("Document not found."));

		if (document.getStatus() == DocumentStatus.DELETED) {
			throw new NotFoundException("Document not found.");
		}

		return document;
	}

	private void validateDimension(float[] embedding) {
		if (embedding == null || embedding.length != embeddingProperties.dimension()) {
			throw new EmbeddingException(
				"Embedding dimension mismatch. Expected "
					+ embeddingProperties.dimension()
					+ " but got "
					+ (embedding == null ? 0 : embedding.length)
					+ "."
			);
		}
	}
}
