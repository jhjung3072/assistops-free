package com.assistops.api.rag.search;

import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.rag.DocumentChunkVectorRepository;
import com.assistops.api.rag.embedding.EmbeddingException;
import com.assistops.api.rag.embedding.EmbeddingProperties;
import com.assistops.api.rag.embedding.EmbeddingService;
import com.assistops.api.user.User;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChunkSearchService {

	private static final int DEFAULT_TOP_K = 5;

	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final EmbeddingService embeddingService;
	private final EmbeddingProperties embeddingProperties;
	private final DocumentChunkVectorRepository documentChunkVectorRepository;

	public ChunkSearchService(
		WorkspaceMemberRepository workspaceMemberRepository,
		EmbeddingService embeddingService,
		EmbeddingProperties embeddingProperties,
		DocumentChunkVectorRepository documentChunkVectorRepository
	) {
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.embeddingService = embeddingService;
		this.embeddingProperties = embeddingProperties;
		this.documentChunkVectorRepository = documentChunkVectorRepository;
	}

	@Transactional(readOnly = true)
	public ChunkSearchResponse search(User user, ChunkSearchRequest request) {
		List<UUID> workspaceIds = resolveWorkspaceIds(user.getId(), request.workspaceId());
		int topK = request.topK() == null ? DEFAULT_TOP_K : request.topK();
		float[] queryEmbedding = embeddingService.embedQuery(request.query());
		validateDimension(queryEmbedding);

		List<ChunkSearchResult> results = documentChunkVectorRepository
			.searchSimilarChunks(workspaceIds, queryEmbedding, topK)
			.stream()
			.map(ChunkSearchResult::from)
			.toList();

		return new ChunkSearchResponse(request.query(), topK, results);
	}

	private List<UUID> resolveWorkspaceIds(UUID userId, UUID requestedWorkspaceId) {
		if (requestedWorkspaceId != null) {
			if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(requestedWorkspaceId, userId)) {
				throw new NotFoundException("Workspace not found.");
			}

			return List.of(requestedWorkspaceId);
		}

		List<UUID> workspaceIds = workspaceMemberRepository.findByUserId(userId)
			.stream()
			.map(WorkspaceMember::getWorkspaceId)
			.toList();

		if (workspaceIds.isEmpty()) {
			throw new NotFoundException("Workspace membership not found.");
		}

		return workspaceIds;
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
