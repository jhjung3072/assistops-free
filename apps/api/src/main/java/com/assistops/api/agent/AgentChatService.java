package com.assistops.api.agent;

import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.rag.RagAnswerRequest;
import com.assistops.api.rag.RagAnswerResponse;
import com.assistops.api.rag.RagAnswerService;
import com.assistops.api.user.User;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentChatService {

	private static final String DEFAULT_TITLE = "새 채팅";
	private static final int GENERATED_TITLE_LENGTH = 30;
	private static final int TITLE_MAX_LENGTH = 255;

	private final AgentChatSessionRepository sessionRepository;
	private final AgentChatMessageRepository messageRepository;
	private final AgentChatMessageSourceRepository sourceRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final RagAnswerService ragAnswerService;

	public AgentChatService(
		AgentChatSessionRepository sessionRepository,
		AgentChatMessageRepository messageRepository,
		AgentChatMessageSourceRepository sourceRepository,
		WorkspaceMemberRepository workspaceMemberRepository,
		RagAnswerService ragAnswerService
	) {
		this.sessionRepository = sessionRepository;
		this.messageRepository = messageRepository;
		this.sourceRepository = sourceRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.ragAnswerService = ragAnswerService;
	}

	@Transactional
	public AgentChatSessionDetailResponse createSession(User user, AgentChatSessionCreateRequest request) {
		UUID workspaceId = resolveWorkspaceId(user.getId(), request.workspaceId());
		String title = normalizeTitle(request.title(), DEFAULT_TITLE);
		AgentChatSession session = sessionRepository.save(new AgentChatSession(workspaceId, user.getId(), title));

		return toDetailResponse(session);
	}

	@Transactional(readOnly = true)
	public AgentChatSessionListResponse getSessions(User user) {
		List<AgentChatSessionSummaryResponse> sessions = sessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())
			.stream()
			.map(AgentChatSessionSummaryResponse::from)
			.toList();

		return new AgentChatSessionListResponse(sessions);
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
			new RagAnswerRequest(content, request.topK(), session.getWorkspaceId())
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

	private AgentChatSession findOwnedSession(UUID userId, UUID sessionId) {
		return sessionRepository.findByIdAndUserId(sessionId, userId)
			.orElseThrow(() -> new NotFoundException("Agent chat session not found."));
	}

	private AgentChatSessionDetailResponse toDetailResponse(AgentChatSession session) {
		List<AgentChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId());
		Map<UUID, List<AgentChatMessageSource>> sourcesByMessageId = findSourcesByMessageId(messages);

		return AgentChatSessionDetailResponse.from(session, messages, sourcesByMessageId);
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
}
