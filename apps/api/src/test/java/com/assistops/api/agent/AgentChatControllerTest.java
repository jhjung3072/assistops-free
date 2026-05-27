package com.assistops.api.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assistops.api.document.Document;
import com.assistops.api.document.DocumentChunk;
import com.assistops.api.document.DocumentChunkRepository;
import com.assistops.api.document.DocumentRepository;
import com.assistops.api.prompt.DefaultPromptContent;
import com.assistops.api.prompt.PromptTemplate;
import com.assistops.api.prompt.PromptTemplateRepository;
import com.assistops.api.prompt.PromptType;
import com.assistops.api.prompt.PromptVersion;
import com.assistops.api.prompt.PromptVersionRepository;
import com.assistops.api.rag.RagAnswer;
import com.assistops.api.rag.RagAnswerRepository;
import com.assistops.api.rag.RagAnswerRequest;
import com.assistops.api.rag.RagAnswerResponse;
import com.assistops.api.rag.RagAnswerSourceRepository;
import com.assistops.api.rag.RagAnswerSourceResponse;
import com.assistops.api.rag.RagAnswerService;
import com.assistops.api.rag.RagAnswerStreamHandler;
import com.assistops.api.rag.RagLatencyMetrics;
import com.assistops.api.rag.search.ChunkSearchResult;
import com.assistops.api.support.AbstractPostgresContainerTest;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AgentChatControllerTest extends AbstractPostgresContainerTest {

	private static final String PASSWORD = "password123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AgentChatSessionRepository sessionRepository;

	@Autowired
	private AgentChatMessageRepository messageRepository;

	@Autowired
	private AgentChatMessageSourceRepository messageSourceRepository;

	@Autowired
	private WorkspaceMemberRepository workspaceMemberRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private DocumentChunkRepository documentChunkRepository;

	@Autowired
	private RagAnswerRepository ragAnswerRepository;

	@Autowired
	private RagAnswerSourceRepository ragAnswerSourceRepository;

	@Autowired
	private PromptTemplateRepository promptTemplateRepository;

	@Autowired
	private PromptVersionRepository promptVersionRepository;

	@MockitoBean
	private RagAnswerService ragAnswerService;

	@BeforeEach
	void setUp() {
		messageSourceRepository.deleteAll();
		messageRepository.deleteAll();
		sessionRepository.deleteAll();
		ragAnswerSourceRepository.deleteAll();
		ragAnswerRepository.deleteAll();
		documentChunkRepository.deleteAll();
		documentRepository.deleteAll();

		given(ragAnswerService.modelName()).willReturn("llama3.2");
	}

	@Test
	void createSessionReturnsUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/agent/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("title", "Agent"))))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void createSessionSucceedsWithAuthentication() throws Exception {
		RegisteredUser user = register();

		mockMvc.perform(post("/api/agent/sessions")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("title", "문서 질문"))))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.title").value("문서 질문"))
			.andExpect(jsonPath("$.userId").value(user.userId().toString()))
			.andExpect(jsonPath("$.messages").isEmpty());
	}

	@Test
	void listSessionsSucceeds() throws Exception {
		RegisteredUser user = register();
		createSession(user.accessToken(), "첫 세션");

		mockMvc.perform(get("/api/agent/sessions")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessions[0].title").value("첫 세션"))
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.page.size").value(20));
	}

	@Test
	void listSessionsFiltersByKeyword() throws Exception {
		RegisteredUser user = register();
		createSession(user.accessToken(), "릴리스 검토");
		createSession(user.accessToken(), "운영 질문");

		mockMvc.perform(get("/api/agent/sessions")
				.param("keyword", "릴리스")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessions.length()").value(1))
			.andExpect(jsonPath("$.sessions[0].title").value("릴리스 검토"))
			.andExpect(jsonPath("$.page.totalElements").value(1));
	}

	@Test
	void listSessionsSupportsPaginationAndClampsSize() throws Exception {
		RegisteredUser user = register();
		createSession(user.accessToken(), "첫 세션");
		createSession(user.accessToken(), "둘째 세션");
		createSession(user.accessToken(), "셋째 세션");

		mockMvc.perform(get("/api/agent/sessions")
				.param("page", "0")
				.param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessions.length()").value(2))
			.andExpect(jsonPath("$.page.totalElements").value(3))
			.andExpect(jsonPath("$.page.totalPages").value(2))
			.andExpect(jsonPath("$.page.hasNext").value(true));

		mockMvc.perform(get("/api/agent/sessions")
				.param("size", "200")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.size").value(100));
	}

	@Test
	void listSessionsDoesNotExposeOtherUsersSessions() throws Exception {
		RegisteredUser owner = register();
		RegisteredUser otherUser = register();
		createSession(owner.accessToken(), "owner session");
		createSession(otherUser.accessToken(), "other session");

		mockMvc.perform(get("/api/agent/sessions")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessions.length()").value(1))
			.andExpect(jsonPath("$.sessions[0].title").value("owner session"))
			.andExpect(jsonPath("$.page.totalElements").value(1));
	}

	@Test
	void getSessionSucceeds() throws Exception {
		RegisteredUser user = register();
		String sessionId = createSession(user.accessToken(), "상세 세션");

		mockMvc.perform(get("/api/agent/sessions/{id}", sessionId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(sessionId))
			.andExpect(jsonPath("$.title").value("상세 세션"));
	}

	@Test
	void sendMessageStoresUserAndAssistantMessages() throws Exception {
		RegisteredUser user = register();
		String sessionId = createSession(user.accessToken(), null);
		RagAnswerResponse answer = createRagAnswerResponse(user, "AI Knowledge Hub는 로컬 RAG 플랫폼입니다.");
		given(ragAnswerService.answer(any(), any(RagAnswerRequest.class), any(PromptType.class))).willReturn(answer);

		mockMvc.perform(post("/api/agent/sessions/{id}/messages", sessionId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"content", "AI Knowledge Hub는 무엇인가요?",
					"topK", 3
				))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("AI Knowledge Hub는 무엇인가요?"))
			.andExpect(jsonPath("$.messages[0].role").value("USER"))
			.andExpect(jsonPath("$.messages[0].content").value("AI Knowledge Hub는 무엇인가요?"))
			.andExpect(jsonPath("$.messages[1].role").value("ASSISTANT"))
			.andExpect(jsonPath("$.messages[1].content").value("AI Knowledge Hub는 로컬 RAG 플랫폼입니다."))
			.andExpect(jsonPath("$.messages[1].ragAnswerId").value(answer.answerId().toString()))
			.andExpect(jsonPath("$.messages[1].promptVersionId").value(answer.promptVersionId().toString()))
			.andExpect(jsonPath("$.messages[1].promptTemplateName").value("Agent Test Prompt"))
			.andExpect(jsonPath("$.messages[1].promptVersion").value(1))
			.andExpect(jsonPath("$.messages[1].model").value("llama3.2"))
			.andExpect(jsonPath("$.messages[1].totalMs").value(4200))
			.andExpect(jsonPath("$.messages[1].chatGenerationMs").value(3900))
			.andExpect(jsonPath("$.messages[1].sourceCount").value(1))
			.andExpect(jsonPath("$.messages[1].sources[0].documentName").value("agent-notes.txt"))
			.andExpect(jsonPath("$.messages[1].sources[0].chunkIndex").value(0));

		List<AgentChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(UUID.fromString(sessionId));
		assertThat(messages).hasSize(2);
		assertThat(messages.get(0).getRole()).isEqualTo(AgentChatRole.USER);
		assertThat(messages.get(1).getRole()).isEqualTo(AgentChatRole.ASSISTANT);
		assertThat(messages.get(1).getTotalMs()).isEqualTo(4200);
		assertThat(messages.get(1).getPromptVersionId()).isEqualTo(answer.promptVersionId());
		assertThat(messageSourceRepository.findByMessageIdOrderByCreatedAtAsc(messages.get(1).getId())).hasSize(1);
	}

	@Test
	void streamMessageReturnsUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/agent/sessions/{id}/messages/stream", UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"content", "AI Knowledge Hub는 무엇인가요?",
					"topK", 3
				))))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void streamMessageStoresMessagesSourcesAndLatency() throws Exception {
		RegisteredUser user = register();
		String sessionId = createSession(user.accessToken(), null);
		RagAnswerResponse answer = createRagAnswerResponse(user, "AI Knowledge Hub는 로컬 RAG 플랫폼입니다.");
		given(ragAnswerService.streamAnswer(
			any(),
			any(RagAnswerRequest.class),
			any(PromptType.class),
			any(RagAnswerStreamHandler.class)
		))
			.willAnswer(invocation -> {
				RagAnswerStreamHandler handler = invocation.getArgument(3, RagAnswerStreamHandler.class);
				RagAnswerSourceResponse source = answer.sources().getFirst();
				handler.onSource(new ChunkSearchResult(
					answer.workspaceId(),
					source.documentId(),
					source.documentName(),
					source.chunkId(),
					source.chunkIndex(),
					source.content(),
					source.score(),
					0.08,
					"nomic-embed-text"
				));
				handler.onToken("AI Knowledge ");
				handler.onToken("Hub");
				return answer;
			});

		MvcResult result = mockMvc.perform(post("/api/agent/sessions/{id}/messages/stream", sessionId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
				.accept(MediaType.TEXT_EVENT_STREAM)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"content", "AI Knowledge Hub는 무엇인가요?",
					"topK", 3
				))))
			.andExpect(request().asyncStarted())
			.andReturn();
		result.getAsyncResult(5_000);

		MvcResult dispatched = mockMvc.perform(asyncDispatch(result))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString(MediaType.TEXT_EVENT_STREAM_VALUE)))
			.andExpect(content().string(containsString("event:metadata")))
			.andExpect(content().string(containsString("event:source")))
			.andExpect(content().string(containsString("event:token")))
			.andExpect(content().string(containsString("AI Knowledge ")))
			.andExpect(content().string(containsString("event:latency")))
			.andExpect(content().string(containsString("event:done")))
			.andReturn();

		assertThat(dispatched.getResponse().getContentAsString()).contains("assistantMessageId");
		List<AgentChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(UUID.fromString(sessionId));
		assertThat(messages).hasSize(2);
		assertThat(messages.get(0).getRole()).isEqualTo(AgentChatRole.USER);
		assertThat(messages.get(1).getRole()).isEqualTo(AgentChatRole.ASSISTANT);
		assertThat(messages.get(1).getContent()).isEqualTo("AI Knowledge Hub는 로컬 RAG 플랫폼입니다.");
		assertThat(messages.get(1).getTotalMs()).isEqualTo(4200);
		assertThat(messages.get(1).getChatGenerationMs()).isEqualTo(3900);
		assertThat(messageSourceRepository.findByMessageIdOrderByCreatedAtAsc(messages.get(1).getId())).hasSize(1);
	}

	@Test
	void streamMessageSendsErrorEventWhenRagFails() throws Exception {
		RegisteredUser user = register();
		String sessionId = createSession(user.accessToken(), null);
		given(ragAnswerService.streamAnswer(
			any(),
			any(RagAnswerRequest.class),
			any(PromptType.class),
			any(RagAnswerStreamHandler.class)
		))
			.willThrow(new RuntimeException("ollama unavailable"));

		MvcResult result = mockMvc.perform(post("/api/agent/sessions/{id}/messages/stream", sessionId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
				.accept(MediaType.TEXT_EVENT_STREAM)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"content", "실패 테스트",
					"topK", 3
				))))
			.andExpect(request().asyncStarted())
			.andReturn();
		result.getAsyncResult(5_000);

		mockMvc.perform(asyncDispatch(result))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("event:error")))
			.andExpect(content().string(containsString("\"message\"")));

		List<AgentChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(UUID.fromString(sessionId));
		assertThat(messages).hasSize(1);
		assertThat(messages.getFirst().getRole()).isEqualTo(AgentChatRole.USER);
	}

	@Test
	void getSessionFailsForOtherUser() throws Exception {
		RegisteredUser owner = register();
		RegisteredUser otherUser = register();
		String sessionId = createSession(owner.accessToken(), "비공개 세션");

		mockMvc.perform(get("/api/agent/sessions/{id}", sessionId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUser.accessToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Agent chat session not found."));
	}

	@Test
	void deleteSessionSucceeds() throws Exception {
		RegisteredUser user = register();
		String sessionId = createSession(user.accessToken(), "삭제 세션");
		RagAnswerResponse answer = createRagAnswerResponse(user, "삭제할 답변입니다.");
		given(ragAnswerService.answer(any(), any(RagAnswerRequest.class), any(PromptType.class))).willReturn(answer);
		sendMessage(user.accessToken(), sessionId, "삭제할 질문입니다.");

		mockMvc.perform(delete("/api/agent/sessions/{id}", sessionId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isNoContent());

		assertThat(sessionRepository.findById(UUID.fromString(sessionId))).isEmpty();
		assertThat(messageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(UUID.fromString(sessionId))).isEmpty();
		assertThat(messageSourceRepository.findAll()).isEmpty();
	}

	private String createSession(String accessToken, String title) throws Exception {
		Map<String, String> body = title == null ? Map.of() : Map.of("title", title);
		MvcResult result = mockMvc.perform(post("/api/agent/sessions")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isOk())
			.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
	}

	private void sendMessage(String accessToken, String sessionId, String content) throws Exception {
		mockMvc.perform(post("/api/agent/sessions/{id}/messages", sessionId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"content", content,
					"topK", 3
				))))
			.andExpect(status().isOk());
	}

	private RagAnswerResponse createRagAnswerResponse(RegisteredUser user, String answerText) {
		UUID workspaceId = workspaceMemberRepository.findByUserId(user.userId())
			.stream()
			.findFirst()
			.map(WorkspaceMember::getWorkspaceId)
			.orElseThrow();
		Document document = documentRepository.save(new Document(
			workspaceId,
			user.userId(),
			"agent-notes.txt",
			"documents/" + workspaceId + "/agent-notes.txt",
			"text/plain",
			64
		));
		DocumentChunk chunk = documentChunkRepository.save(new DocumentChunk(
			document.getId(),
			workspaceId,
			0,
			"AI Knowledge Hub는 로컬 LLM과 오픈소스 인프라로 동작합니다.",
			16
		));
		RagAnswer ragAnswer = ragAnswerRepository.save(new RagAnswer(
			workspaceId,
			user.userId(),
			"AI Knowledge Hub는 무엇인가요?",
			answerText,
			"llama3.2",
			3,
			null
		));
		PromptVersion promptVersion = createPromptVersion(workspaceId, user.userId());
		RagLatencyMetrics metrics = new RagLatencyMetrics(4200L, 120L, 30L, 5L, 3900L, 40L, 1, 840, answerText.length());
		ragAnswer.updateLatencyMetrics(metrics);

		return new RagAnswerResponse(
			ragAnswer.getId(),
			workspaceId,
			user.userId(),
			ragAnswer.getQuestion(),
			answerText,
			"llama3.2",
			3,
			promptVersion.getId(),
			"Agent Test Prompt",
			promptVersion.getVersion(),
			ragAnswer.getCreatedAt(),
			List.of(new RagAnswerSourceResponse(
				UUID.randomUUID(),
				document.getId(),
				document.getOriginalFilename(),
				chunk.getId(),
				chunk.getChunkIndex(),
				chunk.getContent(),
				0.92
			)),
			metrics
		);
	}

	private PromptVersion createPromptVersion(UUID workspaceId, UUID userId) {
		PromptTemplate template = promptTemplateRepository.save(new PromptTemplate(
			workspaceId,
			"Agent Test Prompt",
			"Agent test prompt",
			PromptType.AGENT_CHAT,
			userId
		));
		PromptVersion version = promptVersionRepository.save(new PromptVersion(
			template.getId(),
			1,
			DefaultPromptContent.SYSTEM_PROMPT,
			DefaultPromptContent.USER_PROMPT_TEMPLATE,
			DefaultPromptContent.CONTEXT_TEMPLATE,
			null,
			userId
		));
		template.activate(version.getId());

		return version;
	}

	private RegisteredUser register() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"email", "agent-user-" + UUID.randomUUID() + "@example.com",
					"password", PASSWORD,
					"name", "Agent User"
				))))
			.andExpect(status().isCreated())
			.andReturn();

		String response = result.getResponse().getContentAsString();
		return new RegisteredUser(
			JsonPath.read(response, "$.accessToken"),
			UUID.fromString(JsonPath.read(response, "$.user.id"))
		);
	}

	private record RegisteredUser(String accessToken, UUID userId) {
	}
}
