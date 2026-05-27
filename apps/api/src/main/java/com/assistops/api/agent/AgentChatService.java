package com.assistops.api.agent;

import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.prompt.PromptService;
import com.assistops.api.prompt.PromptType;
import com.assistops.api.prompt.PromptVersionMetadata;
import com.assistops.api.rag.RagAnswerRequest;
import com.assistops.api.rag.RagAnswerResponse;
import com.assistops.api.rag.RagAnswerService;
import com.assistops.api.rag.RagAnswerStreamHandler;
import com.assistops.api.user.User;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AgentChatService {

	private static final Logger log = LoggerFactory.getLogger(AgentChatService.class);
	private static final String DEFAULT_TITLE = "새 채팅";
	private static final int GENERATED_TITLE_LENGTH = 30;
	private static final int TITLE_MAX_LENGTH = 255;

	private final AgentChatSessionRepository sessionRepository;
	private final AgentChatSessionQueryRepository sessionQueryRepository;
	private final AgentChatMessageRepository messageRepository;
	private final AgentChatMessageSourceRepository sourceRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final RagAnswerService ragAnswerService;
	private final PromptService promptService;
	private final TaskExecutor taskExecutor;
	private final TransactionTemplate transactionTemplate;

	public AgentChatService(
		AgentChatSessionRepository sessionRepository,
		AgentChatSessionQueryRepository sessionQueryRepository,
		AgentChatMessageRepository messageRepository,
		AgentChatMessageSourceRepository sourceRepository,
		WorkspaceMemberRepository workspaceMemberRepository,
		RagAnswerService ragAnswerService,
		PromptService promptService,
		TaskExecutor taskExecutor,
		TransactionTemplate transactionTemplate
	) {
		this.sessionRepository = sessionRepository;
		this.sessionQueryRepository = sessionQueryRepository;
		this.messageRepository = messageRepository;
		this.sourceRepository = sourceRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.ragAnswerService = ragAnswerService;
		this.promptService = promptService;
		this.taskExecutor = taskExecutor;
		this.transactionTemplate = transactionTemplate;
	}

	@Transactional
	public AgentChatSessionDetailResponse createSession(User user, AgentChatSessionCreateRequest request) {
		UUID workspaceId = resolveWorkspaceId(user.getId(), request.workspaceId());
		String title = normalizeTitle(request.title(), DEFAULT_TITLE);
		AgentChatSession session = sessionRepository.save(new AgentChatSession(workspaceId, user.getId(), title));

		return toDetailResponse(session);
	}

	public SseEmitter streamMessage(User user, UUID sessionId, AgentChatMessageRequest request) {
		StreamContext context = transactionTemplate.execute(status -> createUserMessageForStreaming(user, sessionId, request));
		SseEmitter emitter = new SseEmitter(0L);

		taskExecutor.execute(() -> runMessageStream(user, context, emitter));

		return emitter;
	}

	@Transactional(readOnly = true)
	public AgentChatSessionListResponse getSessions(User user, AgentChatSessionSearchCondition condition) {
		return AgentChatSessionListResponse.from(
			sessionQueryRepository.search(user.getId(), condition)
				.map(AgentChatSessionSummaryResponse::from)
		);
	}

	@Transactional(readOnly = true)
	public AgentChatSessionDetailResponse getSession(User user, UUID sessionId) {
		return toDetailResponse(findOwnedSession(user.getId(), sessionId));
	}

	@Transactional
	public AgentChatSessionDetailResponse sendMessage(User user, UUID sessionId, AgentChatMessageRequest request) {
		AgentChatSession session = findOwnedSession(user.getId(), sessionId);
		String content = request.content().trim();
		messageRepository.save(AgentChatMessage.user(session.getId(), content));

		if (DEFAULT_TITLE.equals(session.getTitle())
			&& messageRepository.countBySessionIdAndRole(session.getId(), AgentChatRole.USER) == 1) {
			session.rename(generateTitle(content));
		}

		RagAnswerResponse answer = ragAnswerService.answer(
			user,
			new RagAnswerRequest(content, request.topK(), session.getWorkspaceId()),
			PromptType.AGENT_CHAT
		);
		AgentChatMessage assistantMessage = messageRepository.save(AgentChatMessage.assistant(session.getId(), answer));
		List<AgentChatMessageSource> sources = answer.sources()
			.stream()
			.map(source -> AgentChatMessageSource.from(assistantMessage.getId(), source))
			.toList();
		sourceRepository.saveAll(sources);
		session.touch();

		return toDetailResponse(session);
	}

	@Transactional
	public void deleteSession(User user, UUID sessionId) {
		AgentChatSession session = findOwnedSession(user.getId(), sessionId);
		List<UUID> messageIds = messageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId())
			.stream()
			.map(AgentChatMessage::getId)
			.toList();

		if (!messageIds.isEmpty()) {
			sourceRepository.deleteByMessageIdIn(messageIds);
		}
		messageRepository.deleteBySessionId(session.getId());
		sessionRepository.delete(session);
	}

	private StreamContext createUserMessageForStreaming(User user, UUID sessionId, AgentChatMessageRequest request) {
		AgentChatSession session = findOwnedSession(user.getId(), sessionId);
		String content = request.content().trim();
		AgentChatMessage userMessage = messageRepository.save(AgentChatMessage.user(session.getId(), content));

		if (DEFAULT_TITLE.equals(session.getTitle())
			&& messageRepository.countBySessionIdAndRole(session.getId(), AgentChatRole.USER) == 1) {
			session.rename(generateTitle(content));
		}
		session.touch();

		return new StreamContext(
			session.getId(),
			session.getWorkspaceId(),
			userMessage.getId(),
			content,
			request.topK()
		);
	}

	private void runMessageStream(User user, StreamContext context, SseEmitter emitter) {
		String stage = "metadata";

		try {
			sendEvent(emitter, "metadata", new AgentChatStreamMetadataResponse(
				context.sessionId(),
				context.userMessageId(),
				ragAnswerService.modelName()
			));

			stage = "ragAnswer";
			RagAnswerResponse answer = ragAnswerService.streamAnswer(
				user,
				new RagAnswerRequest(context.content(), context.topK(), context.workspaceId()),
				PromptType.AGENT_CHAT,
				new RagAnswerStreamHandler() {
					@Override
					public void onSource(com.assistops.api.rag.search.ChunkSearchResult source) {
						sendEvent(emitter, "source", AgentChatStreamSourceResponse.from(source));
					}

					@Override
					public void onToken(String token) {
						sendEvent(emitter, "token", new AgentChatStreamTokenResponse(token));
					}
				}
			);

			stage = "persistAssistantMessage";
			AgentChatMessage assistantMessage = transactionTemplate.execute(status -> saveAssistantMessage(
				context.sessionId(),
				answer
			));

			sendEvent(emitter, "latency", answer.latencyMetrics());
			sendEvent(emitter, "done", new AgentChatStreamDoneResponse(
				assistantMessage.getId(),
				answer.answerId(),
				answer.promptVersionId(),
				answer.promptTemplateName(),
				answer.promptVersion()
			));
			emitter.complete();
		}
		catch (RuntimeException exception) {
			log.warn(
				"Agent chat stream failed: stage={}, sessionId={}, userMessageId={}, questionLength={}",
				stage,
				context.sessionId(),
				context.userMessageId(),
				context.content().length(),
				exception
			);
			try {
				sendEvent(emitter, "error", new AgentChatStreamErrorResponse(
					"답변 스트리밍 중 오류가 발생했습니다. Ollama 모델과 API 로그를 확인해 주세요."
				));
			}
			catch (RuntimeException ignored) {
				log.debug("Failed to send agent chat stream error event.", ignored);
			}
			emitter.complete();
		}
	}

	private AgentChatMessage saveAssistantMessage(UUID sessionId, RagAnswerResponse answer) {
		AgentChatSession session = sessionRepository.findById(sessionId)
			.orElseThrow(() -> new NotFoundException("Agent chat session not found."));
		AgentChatMessage assistantMessage = messageRepository.save(AgentChatMessage.assistant(session.getId(), answer));
		List<AgentChatMessageSource> sources = answer.sources()
			.stream()
			.map(source -> AgentChatMessageSource.from(assistantMessage.getId(), source))
			.toList();
		sourceRepository.saveAll(sources);
		session.touch();

		return assistantMessage;
	}

	private void sendEvent(SseEmitter emitter, String eventName, Object payload) {
		try {
			emitter.send(SseEmitter.event()
				.name(eventName)
				.data(payload));
		}
		catch (IOException exception) {
			throw new AgentChatStreamException("Failed to send SSE event.", exception);
		}
	}

	private AgentChatSession findOwnedSession(UUID userId, UUID sessionId) {
		return sessionRepository.findByIdAndUserId(sessionId, userId)
			.orElseThrow(() -> new NotFoundException("Agent chat session not found."));
	}

	private AgentChatSessionDetailResponse toDetailResponse(AgentChatSession session) {
		List<AgentChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId());
		Map<UUID, List<AgentChatMessageSource>> sourcesByMessageId = findSourcesByMessageId(messages);
		Map<UUID, PromptVersionMetadata> promptMetadataByVersionId = findPromptMetadataByVersionId(messages);

		return AgentChatSessionDetailResponse.from(session, messages, sourcesByMessageId, promptMetadataByVersionId);
	}

	private Map<UUID, List<AgentChatMessageSource>> findSourcesByMessageId(Collection<AgentChatMessage> messages) {
		List<UUID> messageIds = messages.stream()
			.map(AgentChatMessage::getId)
			.toList();

		if (messageIds.isEmpty()) {
			return Map.of();
		}

		return sourceRepository.findByMessageIdInOrderByCreatedAtAsc(messageIds)
			.stream()
			.collect(Collectors.groupingBy(AgentChatMessageSource::getMessageId));
	}

	private Map<UUID, PromptVersionMetadata> findPromptMetadataByVersionId(Collection<AgentChatMessage> messages) {
		return messages.stream()
			.map(AgentChatMessage::getPromptVersionId)
			.filter(java.util.Objects::nonNull)
			.distinct()
			.collect(Collectors.toMap(
				versionId -> versionId,
				promptService::getMetadata
			));
	}

	private UUID resolveWorkspaceId(UUID userId, UUID requestedWorkspaceId) {
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

	private String normalizeTitle(String title, String fallback) {
		if (title == null || title.isBlank()) {
			return fallback;
		}

		return truncate(title.trim(), TITLE_MAX_LENGTH);
	}

	private String generateTitle(String content) {
		if (content.isBlank()) {
			return DEFAULT_TITLE;
		}

		return truncate(content, GENERATED_TITLE_LENGTH);
	}

	private String truncate(String value, int maxCodePoints) {
		if (value.codePointCount(0, value.length()) <= maxCodePoints) {
			return value;
		}

		return value.substring(0, value.offsetByCodePoints(0, maxCodePoints));
	}

	private record StreamContext(
		UUID sessionId,
		UUID workspaceId,
		UUID userMessageId,
		String content,
		Integer topK
	) {
	}

	private static class AgentChatStreamException extends RuntimeException {

		AgentChatStreamException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
