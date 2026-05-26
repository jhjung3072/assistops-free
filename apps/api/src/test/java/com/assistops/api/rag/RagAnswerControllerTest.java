package com.assistops.api.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assistops.api.document.Document;
import com.assistops.api.document.DocumentChunk;
import com.assistops.api.document.DocumentChunkRepository;
import com.assistops.api.document.DocumentRepository;
import com.assistops.api.rag.generation.RagGenerationService;
import com.assistops.api.rag.generation.RagGenerationResult;
import com.assistops.api.rag.search.ChunkSearchRequest;
import com.assistops.api.rag.search.ChunkSearchResponse;
import com.assistops.api.rag.search.ChunkSearchResult;
import com.assistops.api.rag.search.ChunkSearchService;
import com.assistops.api.support.AbstractPostgresContainerTest;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RagAnswerControllerTest extends AbstractPostgresContainerTest {

	private static final String PASSWORD = "password123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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
	private JdbcTemplate jdbcTemplate;

	@MockitoBean
	private ChunkSearchService chunkSearchService;

	@MockitoBean
	private RagGenerationService ragGenerationService;

	@BeforeEach
	void setUp() {
		ragAnswerSourceRepository.deleteAll();
		ragAnswerRepository.deleteAll();
		documentChunkRepository.deleteAll();
		documentRepository.deleteAll();

		given(ragGenerationService.modelName()).willReturn("llama3.2");
		given(ragGenerationService.generateAnswer(any(), any()))
			.willReturn(new RagGenerationResult(
				"문서에 따르면 AssistOps Free는 로컬 인프라 기반 자동화 플랫폼입니다.",
				4,
				123,
				120
			));
	}

	@Test
	void answerReturnsUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/rag/answer")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"question", "AssistOps Free는 무엇인가요?",
					"topK", 5
				))))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void answerSucceedsWithAuthentication() throws Exception {
		RegisteredUser registeredUser = register();
		ChunkSearchResult source = createSourceResult(registeredUser);
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("AssistOps Free는 무엇인가요?", 5, List.of(source)),
				11,
				22
			));

		MvcResult result = mockMvc.perform(post("/api/rag/answer")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"question", "AssistOps Free는 무엇인가요?",
					"topK", 5
				))))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.answerId").exists())
			.andExpect(jsonPath("$.question").value("AssistOps Free는 무엇인가요?"))
			.andExpect(jsonPath("$.answer").value("문서에 따르면 AssistOps Free는 로컬 인프라 기반 자동화 플랫폼입니다."))
			.andExpect(jsonPath("$.model").value("llama3.2"))
			.andExpect(jsonPath("$.latencyMetrics.totalMs").exists())
			.andExpect(jsonPath("$.latencyMetrics.queryEmbeddingMs").value(11))
			.andExpect(jsonPath("$.latencyMetrics.vectorSearchMs").value(22))
			.andExpect(jsonPath("$.latencyMetrics.promptBuildMs").value(4))
			.andExpect(jsonPath("$.latencyMetrics.chatGenerationMs").value(123))
			.andExpect(jsonPath("$.latencyMetrics.answerPersistMs").exists())
			.andExpect(jsonPath("$.latencyMetrics.sourceCount").value(1))
			.andExpect(jsonPath("$.latencyMetrics.promptContextCharCount").value(120))
			.andExpect(jsonPath("$.latencyMetrics.answerCharCount").value(45))
			.andExpect(jsonPath("$.sources[0].documentName").value("rag-notes.txt"))
			.andExpect(jsonPath("$.sources[0].chunkIndex").value(0))
			.andExpect(jsonPath("$.sources[0].score").value(0.91))
			.andReturn();

		UUID answerId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.answerId"));

		RagAnswer savedAnswer = ragAnswerRepository.findById(answerId).orElseThrow();
		assertThat(savedAnswer.getTotalMs()).isNotNull();
		assertThat(savedAnswer.getQueryEmbeddingMs()).isEqualTo(11);
		assertThat(savedAnswer.getVectorSearchMs()).isEqualTo(22);
		assertThat(savedAnswer.getPromptBuildMs()).isEqualTo(4);
		assertThat(savedAnswer.getChatGenerationMs()).isEqualTo(123);
		assertThat(savedAnswer.getAnswerPersistMs()).isNotNull();
		assertThat(savedAnswer.getSourceCount()).isEqualTo(1);
		assertThat(savedAnswer.getPromptContextCharCount()).isEqualTo(120);
		assertThat(savedAnswer.getAnswerCharCount()).isEqualTo(45);
		assertThat(ragAnswerSourceRepository.countByRagAnswerId(answerId)).isEqualTo(1);
	}

	@Test
	void answerReturnsInsufficientContextWhenSearchResultsAreEmpty() throws Exception {
		RegisteredUser registeredUser = register();
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("없는 내용인가요?", 3, List.of()),
				7,
				8
			));

		mockMvc.perform(post("/api/rag/answer")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"question", "없는 내용인가요?"
				))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.answer").value("제공된 문서만으로는 답변하기 어렵습니다."))
			.andExpect(jsonPath("$.topK").value(3))
			.andExpect(jsonPath("$.latencyMetrics.chatGenerationMs").value(0))
			.andExpect(jsonPath("$.latencyMetrics.sourceCount").value(0))
			.andExpect(jsonPath("$.latencyMetrics.promptContextCharCount").value(0))
			.andExpect(jsonPath("$.sources").isEmpty());

		verify(ragGenerationService, never()).generateAnswer(any(), any());
		assertThat(ragAnswerRepository.findAll()).hasSize(1);
		assertThat(ragAnswerSourceRepository.findAll()).isEmpty();
	}

	@Test
	void answerUsesDefaultTopKThree() throws Exception {
		RegisteredUser registeredUser = register();
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("기본 topK는?", 3, List.of()),
				1,
				1
			));

		mockMvc.perform(post("/api/rag/answer")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"question", "기본 topK는?"
				))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.topK").value(3));

		ArgumentCaptor<ChunkSearchRequest> requestCaptor = ArgumentCaptor.forClass(ChunkSearchRequest.class);
		verify(chunkSearchService).searchWithMetrics(any(), requestCaptor.capture());
		assertThat(requestCaptor.getValue().topK()).isEqualTo(3);
	}

	@Test
	void answerFailsWhenTopKExceedsAllowedRange() throws Exception {
		RegisteredUser registeredUser = register();

		mockMvc.perform(post("/api/rag/answer")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"question", "topK가 너무 큰가요?",
					"topK", 9
				))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors.topK").value("topK must be 8 or smaller."));
	}

	@Test
	void listAnswersSucceeds() throws Exception {
		RegisteredUser registeredUser = register();
		ChunkSearchResult source = createSourceResult(registeredUser);
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("질문", 5, List.of(source)),
				11,
				22
			));
		createAnswer(registeredUser.accessToken(), "질문");

		mockMvc.perform(get("/api/rag/answers")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken()))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.answers[0].answerId").exists())
			.andExpect(jsonPath("$.answers[0].question").value("질문"))
			.andExpect(jsonPath("$.answers[0].sourceCount").value(1))
			.andExpect(jsonPath("$.answers[0].totalMs").exists())
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.page.size").value(20));
	}

	@Test
	void listAnswersFiltersByKeyword() throws Exception {
		RegisteredUser registeredUser = register();
		ChunkSearchResult source = createSourceResult(registeredUser);
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("질문", 5, List.of(source)),
				11,
				22
			));
		createAnswer(registeredUser.accessToken(), "릴리스 노트 요약");
		createAnswer(registeredUser.accessToken(), "운영 절차 질문");

		mockMvc.perform(get("/api/rag/answers")
				.param("keyword", "릴리스")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.answers.length()").value(1))
			.andExpect(jsonPath("$.answers[0].question").value("릴리스 노트 요약"))
			.andExpect(jsonPath("$.page.totalElements").value(1));
	}

	@Test
	void listAnswersSupportsPaginationAndClampsSize() throws Exception {
		RegisteredUser registeredUser = register();
		ChunkSearchResult source = createSourceResult(registeredUser);
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("질문", 5, List.of(source)),
				11,
				22
			));
		createAnswer(registeredUser.accessToken(), "첫 질문");
		createAnswer(registeredUser.accessToken(), "둘째 질문");
		createAnswer(registeredUser.accessToken(), "셋째 질문");

		mockMvc.perform(get("/api/rag/answers")
				.param("page", "0")
				.param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.answers.length()").value(2))
			.andExpect(jsonPath("$.page.totalElements").value(3))
			.andExpect(jsonPath("$.page.totalPages").value(2))
			.andExpect(jsonPath("$.page.hasNext").value(true));

		mockMvc.perform(get("/api/rag/answers")
				.param("size", "200")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.size").value(100));
	}

	@Test
	void listAnswersDoesNotExposeOtherUsersAnswers() throws Exception {
		RegisteredUser owner = register();
		RegisteredUser otherUser = register();
		ChunkSearchResult ownerSource = createSourceResult(owner);
		ChunkSearchResult otherSource = createSourceResult(otherUser);
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("질문", 5, List.of(ownerSource)),
				11,
				22
			))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("질문", 5, List.of(otherSource)),
				11,
				22
			));
		createAnswer(owner.accessToken(), "owner question");
		createAnswer(otherUser.accessToken(), "other question");

		mockMvc.perform(get("/api/rag/answers")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.answers.length()").value(1))
			.andExpect(jsonPath("$.answers[0].question").value("owner question"))
			.andExpect(jsonPath("$.page.totalElements").value(1));
	}

	@Test
	void getAnswerSucceeds() throws Exception {
		RegisteredUser registeredUser = register();
		ChunkSearchResult source = createSourceResult(registeredUser);
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("상세 질문", 5, List.of(source)),
				11,
				22
			));
		String answerId = createAnswer(registeredUser.accessToken(), "상세 질문");

		mockMvc.perform(get("/api/rag/answers/{id}", answerId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.answerId").value(answerId))
			.andExpect(jsonPath("$.latencyMetrics.totalMs").exists())
			.andExpect(jsonPath("$.sources[0].documentName").value("rag-notes.txt"));
	}

	@Test
	void getAnswerFailsWithoutWorkspaceAccess() throws Exception {
		RegisteredUser registeredUser = register();
		UUID inaccessibleAnswerId = insertInaccessibleAnswer(registeredUser.userId());

		mockMvc.perform(get("/api/rag/answers/{id}", inaccessibleAnswerId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("RAG answer not found."));
	}

	@Test
	void deleteAnswerSucceeds() throws Exception {
		RegisteredUser registeredUser = register();
		ChunkSearchResult source = createSourceResult(registeredUser);
		given(chunkSearchService.searchWithMetrics(any(), any(ChunkSearchRequest.class)))
			.willReturn(new ChunkSearchService.ChunkSearchResultWithMetrics(
				new ChunkSearchResponse("삭제 질문", 5, List.of(source)),
				11,
				22
			));
		String answerId = createAnswer(registeredUser.accessToken(), "삭제 질문");

		mockMvc.perform(delete("/api/rag/answers/{id}", answerId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken()))
			.andExpect(status().isNoContent());

		assertThat(ragAnswerRepository.findById(UUID.fromString(answerId))).isEmpty();
		assertThat(ragAnswerSourceRepository.countByRagAnswerId(UUID.fromString(answerId))).isZero();
	}

	private String createAnswer(String accessToken, String question) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/rag/answer")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"question", question,
					"topK", 5
				))))
			.andExpect(status().isOk())
			.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.answerId");
	}

	private ChunkSearchResult createSourceResult(RegisteredUser registeredUser) {
		UUID workspaceId = workspaceMemberRepository.findByUserId(registeredUser.userId())
			.stream()
			.findFirst()
			.map(WorkspaceMember::getWorkspaceId)
			.orElseThrow();
		Document document = documentRepository.save(new Document(
			workspaceId,
			registeredUser.userId(),
			"rag-notes.txt",
			"documents/" + workspaceId + "/" + UUID.randomUUID() + "-rag-notes.txt",
			"text/plain",
			44
		));
		DocumentChunk chunk = documentChunkRepository.save(new DocumentChunk(
			document.getId(),
			workspaceId,
			0,
			"AssistOps Free는 로컬 인프라 기반 AI 자동화 플랫폼입니다.",
			14
		));

		return new ChunkSearchResult(
			workspaceId,
			document.getId(),
			document.getOriginalFilename(),
			chunk.getId(),
			chunk.getChunkIndex(),
			chunk.getContent(),
			0.91,
			0.09,
			"nomic-embed-text"
		);
	}

	private UUID insertInaccessibleAnswer(UUID userId) {
		UUID workspaceId = UUID.randomUUID();
		UUID answerId = UUID.randomUUID();
		Instant now = Instant.now();
		Timestamp timestamp = Timestamp.from(now);

		jdbcTemplate.update(
			"INSERT INTO workspaces (id, name, slug, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
			workspaceId,
			"Private Workspace",
			"private-" + workspaceId,
			timestamp,
			timestamp
		);
		jdbcTemplate.update(
			"""
			INSERT INTO rag_answers (id, workspace_id, user_id, question, answer, model, top_k, created_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?)
			""",
			answerId,
			workspaceId,
			userId,
			"private",
			"private answer",
			"llama3.2",
			5,
			timestamp
		);

		return answerId;
	}

	private RegisteredUser register() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"email", "rag-user-" + UUID.randomUUID() + "@example.com",
					"password", PASSWORD,
					"name", "RAG User"
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
