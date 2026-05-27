package com.assistops.api.rag;

import com.assistops.api.global.exception.BadRequestException;
import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.prompt.PromptResolvedVersion;
import com.assistops.api.prompt.PromptService;
import com.assistops.api.prompt.PromptType;
import com.assistops.api.prompt.PromptVersion;
import com.assistops.api.prompt.PromptVersionMetadata;
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
	private final RagAnswerQueryRepository ragAnswerQueryRepository;
	private final RagAnswerSourceRepository ragAnswerSourceRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final PromptService promptService;

	public RagAnswerService(
		ChunkSearchService chunkSearchService,
		RagGenerationService ragGenerationService,
		RagProperties ragProperties,
		RagAnswerRepository ragAnswerRepository,
		RagAnswerQueryRepository ragAnswerQueryRepository,
		RagAnswerSourceRepository ragAnswerSourceRepository,
		WorkspaceMemberRepository workspaceMemberRepository,
		PromptService promptService
	) {
		this.chunkSearchService = chunkSearchService;
		this.ragGenerationService = ragGenerationService;
		this.ragProperties = ragProperties;
		this.ragAnswerRepository = ragAnswerRepository;
		this.ragAnswerQueryRepository = ragAnswerQueryRepository;
		this.ragAnswerSourceRepository = ragAnswerSourceRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.promptService = promptService;
	}

	@Transactional
	public RagAnswerResponse answer(User user, RagAnswerRequest request) {
		return answerInternal(user, request, PromptType.RAG_ANSWER, null);
	}

	@Transactional
	public RagAnswerResponse answer(User user, RagAnswerRequest request, PromptType promptType) {
		return answerInternal(user, request, promptType, null);
	}

	@Transactional
	public RagAnswerResponse streamAnswer(
		User user,
		RagAnswerRequest request,
		RagAnswerStreamHandler streamHandler
	) {
		return answerInternal(user, request, PromptType.RAG_ANSWER, streamHandler);
	}

	@Transactional
	public RagAnswerResponse streamAnswer(
		User user,
		RagAnswerRequest request,
		PromptType promptType,
		RagAnswerStreamHandler streamHandler
	) {
		return answerInternal(user, request, promptType, streamHandler);
	}

	private RagAnswerResponse answerInternal(
		User user,
		RagAnswerRequest request,
		PromptType promptType,
		RagAnswerStreamHandler streamHandler
	) {
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
			if (streamHandler != null) {
				results.forEach(streamHandler::onSource);
			}
			PromptResolvedVersion prompt = promptService.resolveActivePromptVersion(user, workspaceId, promptType);

			stage = "generation";
			RagGenerationResult generationResult = generateAnswer(
				request,
				results,
				prompt.version(),
				streamHandler
			);

			stage = "persist";
			long persistStart = System.nanoTime();
			RagAnswer ragAnswer = ragAnswerRepository.save(new RagAnswer(
				workspaceId,
				user.getId(),
				request.question(),
				generationResult.answer(),
				modelName(generationResult),
				topK,
				prompt.version().getId()
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

			return RagAnswerResponse.from(ragAnswer, sources, metrics, prompt.metadata());
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

	private RagGenerationResult generateAnswer(
		RagAnswerRequest request,
		List<ChunkSearchResult> results,
		PromptVersion promptVersion,
		RagAnswerStreamHandler streamHandler
	) {
		if (results.isEmpty()) {
			if (streamHandler != null) {
				streamHandler.onToken(INSUFFICIENT_CONTEXT_ANSWER);
			}

			return new RagGenerationResult(
				INSUFFICIENT_CONTEXT_ANSWER,
				0,
				0,
				0,
				modelName(promptVersion)
			);
		}

		if (streamHandler != null) {
			return ragGenerationService.generateAnswerStream(
				request.question(),
				results,
				promptVersion,
				streamHandler::onToken
			);
		}

		return ragGenerationService.generateAnswer(request.question(), results, promptVersion);
	}

	@Transactional(readOnly = true)
	public RagAnswerListResponse getAnswers(User user, RagAnswerSearchCondition condition) {
		List<UUID> workspaceIds = accessibleWorkspaceIds(user.getId());

		return RagAnswerListResponse.from(
			ragAnswerQueryRepository.search(user.getId(), workspaceIds, condition)
				.map(answer -> RagAnswerSummary.from(
					answer,
					ragAnswerSourceRepository.countByRagAnswerId(answer.getId())
				))
		);
	}

	public String modelName() {
		return ragGenerationService.modelName();
	}

	@Transactional(readOnly = true)
	public RagAnswerResponse getAnswer(User user, UUID answerId) {
		RagAnswer answer = findAccessibleAnswer(user.getId(), answerId);
		List<RagAnswerSource> sources = ragAnswerSourceRepository.findByRagAnswerIdOrderByCreatedAtAsc(answerId);
		PromptVersionMetadata metadata = promptService.getMetadata(answer.getPromptVersionId());

		return RagAnswerResponse.from(answer, sources, RagLatencyMetrics.from(answer), metadata);
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

	private String modelName(RagGenerationResult generationResult) {
		return generationResult.model() == null ? ragGenerationService.modelName() : generationResult.model();
	}

	private String modelName(PromptVersion promptVersion) {
		return org.springframework.util.StringUtils.hasText(promptVersion.getModel())
			? promptVersion.getModel()
			: ragGenerationService.modelName();
	}
}
