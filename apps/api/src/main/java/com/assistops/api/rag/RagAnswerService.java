package com.assistops.api.rag;

import com.assistops.api.global.exception.BadRequestException;
import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.rag.generation.RagGenerationResult;
import com.assistops.api.rag.generation.RagGenerationService;
import com.assistops.api.rag.generation.RagProperties;
import com.assistops.api.rag.search.ChunkSearchRequest;
import com.assistops.api.rag.search.ChunkSearchResult;
import com.assistops.api.rag.search.ChunkSearchService;
import com.assistops.api.user.User;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RagAnswerService {

	private static final Logger log = LoggerFactory.getLogger(RagAnswerService.class);
	private static final String INSUFFICIENT_CONTEXT_ANSWER = "제공된 문서만으로는 답변하기 어렵습니다.";

	private final ChunkSearchService chunkSearchService;
	private final RagGenerationService ragGenerationService;
	private final RagProperties ragProperties;
	private final RagAnswerRepository ragAnswerRepository;
	private final RagAnswerSourceRepository ragAnswerSourceRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;

	public RagAnswerService(
		ChunkSearchService chunkSearchService,
		RagGenerationService ragGenerationService,
		RagProperties ragProperties,
		RagAnswerRepository ragAnswerRepository,
		RagAnswerSourceRepository ragAnswerSourceRepository,
		WorkspaceMemberRepository workspaceMemberRepository
	) {
		this.chunkSearchService = chunkSearchService;
		this.ragGenerationService = ragGenerationService;
		this.ragProperties = ragProperties;
		this.ragAnswerRepository = ragAnswerRepository;
		this.ragAnswerSourceRepository = ragAnswerSourceRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
	}

	@Transactional
	public RagAnswerResponse answer(User user, RagAnswerRequest request) {
		long totalStart = System.nanoTime();
		String stage = "resolveTopK";

		try {
			int topK = resolveTopK(request.topK());

			stage = "semanticSearch";
			ChunkSearchService.ChunkSearchResultWithMetrics searchResult = chunkSearchService.searchWithMetrics(
				user,
				new ChunkSearchRequest(request.question(), topK, request.workspaceId())
			);
			List<ChunkSearchResult> results = searchResult.response().results();
			UUID workspaceId = resolveAnswerWorkspaceId(user.getId(), request.workspaceId(), results);

			stage = "generation";
			RagGenerationResult generationResult = results.isEmpty()
				? new RagGenerationResult(INSUFFICIENT_CONTEXT_ANSWER, 0, 0, 0)
				: ragGenerationService.generateAnswer(request.question(), results);

			stage = "persist";
			long persistStart = System.nanoTime();
			RagAnswer ragAnswer = ragAnswerRepository.save(new RagAnswer(
				workspaceId,
				user.getId(),
				request.question(),
				generationResult.answer(),
				ragGenerationService.modelName(),
				topK
			));

			List<RagAnswerSource> sources = results.stream()
				.map(result -> RagAnswerSource.from(ragAnswer, result))
				.toList();
			ragAnswerSourceRepository.saveAll(sources);
			long answerPersistMs = elapsedMs(persistStart);
			long totalMs = elapsedMs(totalStart);

			RagLatencyMetrics metrics = new RagLatencyMetrics(
				totalMs,
				searchResult.queryEmbeddingMs(),
				searchResult.vectorSearchMs(),
				generationResult.promptBuildMs(),
				generationResult.chatGenerationMs(),
				answerPersistMs,
				sources.size(),
				generationResult.promptContextCharCount(),
				generationResult.answer().length()
			);
			ragAnswer.updateLatencyMetrics(metrics);

			log.info(
				"RAG answer latency: totalMs={}, queryEmbeddingMs={}, vectorSearchMs={}, promptBuildMs={}, chatGenerationMs={}, answerPersistMs={}, sourceCount={}, promptContextCharCount={}, answerCharCount={}, model={}, questionLength={}",
				metrics.totalMs(),
				metrics.queryEmbeddingMs(),
				metrics.vectorSearchMs(),
				metrics.promptBuildMs(),
				metrics.chatGenerationMs(),
				metrics.answerPersistMs(),
				metrics.sourceCount(),
				metrics.promptContextCharCount(),
				metrics.answerCharCount(),
				ragGenerationService.modelName(),
				request.question().length()
			);

			return RagAnswerResponse.from(ragAnswer, sources, metrics);
		}
		catch (RuntimeException exception) {
			log.warn(
				"RAG answer failed: stage={}, elapsedMs={}, model={}, questionLength={}",
				stage,
				elapsedMs(totalStart),
				ragGenerationService.modelName(),
				request.question() == null ? 0 : request.question().length()
			);
			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public RagAnswerListResponse getAnswers(User user) {
		List<UUID> workspaceIds = accessibleWorkspaceIds(user.getId());
		List<RagAnswerSummary> answers = ragAnswerRepository
			.findByUserIdAndWorkspaceIdInOrderByCreatedAtDesc(user.getId(), workspaceIds)
			.stream()
			.map(answer -> RagAnswerSummary.from(
				answer,
				ragAnswerSourceRepository.countByRagAnswerId(answer.getId())
			))
			.toList();

		return new RagAnswerListResponse(answers);
	}

	@Transactional(readOnly = true)
	public RagAnswerResponse getAnswer(User user, UUID answerId) {
		RagAnswer answer = findAccessibleAnswer(user.getId(), answerId);
		List<RagAnswerSource> sources = ragAnswerSourceRepository.findByRagAnswerIdOrderByCreatedAtAsc(answerId);

		return RagAnswerResponse.from(answer, sources);
	}

	@Transactional
	public void deleteAnswer(User user, UUID answerId) {
		RagAnswer answer = findAccessibleAnswer(user.getId(), answerId);

		ragAnswerSourceRepository.deleteByRagAnswerId(answer.getId());
		ragAnswerRepository.delete(answer);
	}

	private RagAnswer findAccessibleAnswer(UUID userId, UUID answerId) {
		List<UUID> workspaceIds = accessibleWorkspaceIds(userId);

		return ragAnswerRepository.findByIdAndWorkspaceIdIn(answerId, workspaceIds)
			.orElseThrow(() -> new NotFoundException("RAG answer not found."));
	}

	private UUID resolveAnswerWorkspaceId(
		UUID userId,
		UUID requestedWorkspaceId,
		List<ChunkSearchResult> results
	) {
		if (!results.isEmpty()) {
			return results.getFirst().workspaceId();
		}

		if (requestedWorkspaceId != null) {
			if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(requestedWorkspaceId, userId)) {
				throw new NotFoundException("Workspace not found.");
			}

			return requestedWorkspaceId;
		}

		return workspaceMemberRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
			.map(WorkspaceMember::getWorkspaceId)
			.orElseThrow(() -> new NotFoundException("Workspace membership not found."));
	}

	private List<UUID> accessibleWorkspaceIds(UUID userId) {
		List<UUID> workspaceIds = workspaceMemberRepository.findByUserId(userId)
			.stream()
			.map(WorkspaceMember::getWorkspaceId)
			.toList();

		if (workspaceIds.isEmpty()) {
			throw new NotFoundException("Workspace membership not found.");
		}

		return workspaceIds;
	}

	private int resolveTopK(Integer requestedTopK) {
		int topK = requestedTopK == null ? ragProperties.defaultTopK() : requestedTopK;

		if (topK < ragProperties.minTopK() || topK > ragProperties.maxTopK()) {
			throw new BadRequestException(
				"topK must be between " + ragProperties.minTopK() + " and " + ragProperties.maxTopK() + "."
			);
		}

		return topK;
	}

	private long elapsedMs(long startedAt) {
		return (System.nanoTime() - startedAt) / 1_000_000L;
	}
}
