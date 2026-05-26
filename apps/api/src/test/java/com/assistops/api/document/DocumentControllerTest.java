package com.assistops.api.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assistops.api.support.AbstractPostgresContainerTest;
import com.assistops.api.rag.embedding.EmbeddingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTest extends AbstractPostgresContainerTest {

	private static final String PASSWORD = "password123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentChunkRepository documentChunkRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoBean
	private DocumentStorageService documentStorageService;

	@MockitoBean
	private EmbeddingService embeddingService;

	@BeforeEach
	void setUp() {
		documentChunkRepository.deleteAll();
		documentRepository.deleteAll();
		doNothing().when(documentStorageService).upload(anyString(), any(MultipartFile.class));
		doNothing().when(documentStorageService).delete(anyString());
		given(documentStorageService.download(anyString()))
			.willAnswer(invocation -> textStream("first paragraph\n\nsecond paragraph for chunking"));
		given(embeddingService.modelName()).willReturn("nomic-embed-text");
		given(embeddingService.embedDocument(anyString())).willReturn(testEmbedding());
		given(embeddingService.embedQuery(anyString())).willReturn(testEmbedding());
	}

	@Test
	void uploadReturnsUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(multipart("/api/documents")
				.file(textFile()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void uploadSucceedsWithAuthentication() throws Exception {
		String accessToken = registerAndGetToken();

		mockMvc.perform(multipart("/api/documents")
				.file(textFile())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.document.id").exists())
			.andExpect(jsonPath("$.document.originalFilename").value("notes.txt"))
			.andExpect(jsonPath("$.document.contentType").value("text/plain"))
			.andExpect(jsonPath("$.document.sizeBytes").value(11))
			.andExpect(jsonPath("$.document.status").value("UPLOADED"));
	}

	@Test
	void uploadFailsWithUnsupportedFileType() throws Exception {
		String accessToken = registerAndGetToken();
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"image.png",
			"image/png",
			"not-image".getBytes(StandardCharsets.UTF_8)
		);

		mockMvc.perform(multipart("/api/documents")
				.file(file)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Only PDF, TXT, and MD files are supported."));
	}

	@Test
	void listDocumentsSucceeds() throws Exception {
		String accessToken = registerAndGetToken();
		uploadDocument(accessToken);

		mockMvc.perform(get("/api/documents")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.documents[0].originalFilename").value("notes.txt"))
			.andExpect(jsonPath("$.documents[0].status").value("UPLOADED"))
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.page.page").value(0))
			.andExpect(jsonPath("$.page.size").value(20));
	}

	@Test
	void listDocumentsFiltersByKeyword() throws Exception {
		String accessToken = registerAndGetToken();
		uploadDocument(accessToken, textFile("release-notes.md", "release"));
		uploadDocument(accessToken, textFile("meeting-notes.txt", "meeting"));

		mockMvc.perform(get("/api/documents")
				.param("keyword", "release")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.documents.length()").value(1))
			.andExpect(jsonPath("$.documents[0].originalFilename").value("release-notes.md"))
			.andExpect(jsonPath("$.page.totalElements").value(1));
	}

	@Test
	void listDocumentsFiltersByStatusAndEmbeddingStatus() throws Exception {
		String accessToken = registerAndGetToken();
		uploadDocument(accessToken, textFile("raw.txt", "raw"));
		String processedDocumentId = uploadDocument(accessToken, textFile("processed.txt", "processed"));
		processDocument(accessToken, processedDocumentId);
		mockMvc.perform(post("/api/documents/{id}/embed", processedDocumentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/documents")
				.param("status", "PROCESSED")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.documents.length()").value(1))
			.andExpect(jsonPath("$.documents[0].id").value(processedDocumentId))
			.andExpect(jsonPath("$.documents[0].status").value("PROCESSED"));

		mockMvc.perform(get("/api/documents")
				.param("embeddingStatus", "EMBEDDED")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.documents.length()").value(1))
			.andExpect(jsonPath("$.documents[0].id").value(processedDocumentId))
			.andExpect(jsonPath("$.documents[0].embeddingStatus").value("EMBEDDED"));
	}

	@Test
	void listDocumentsSupportsPaginationAndClampsSize() throws Exception {
		String accessToken = registerAndGetToken();
		uploadDocument(accessToken, textFile("first.txt", "first"));
		uploadDocument(accessToken, textFile("second.txt", "second"));
		uploadDocument(accessToken, textFile("third.txt", "third"));

		mockMvc.perform(get("/api/documents")
				.param("page", "0")
				.param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.documents.length()").value(2))
			.andExpect(jsonPath("$.page.totalElements").value(3))
			.andExpect(jsonPath("$.page.totalPages").value(2))
			.andExpect(jsonPath("$.page.hasNext").value(true));

		mockMvc.perform(get("/api/documents")
				.param("size", "200")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.size").value(100));
	}

	@Test
	void listDocumentsDoesNotExposeInaccessibleWorkspaceDocuments() throws Exception {
		RegisteredUser registeredUser = register();
		uploadDocument(registeredUser.accessToken(), textFile("visible.txt", "visible"));
		createInaccessibleDocument(registeredUser.userId());

		mockMvc.perform(get("/api/documents")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registeredUser.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.documents.length()").value(1))
			.andExpect(jsonPath("$.documents[0].originalFilename").value("visible.txt"))
			.andExpect(jsonPath("$.page.totalElements").value(1));
	}

	@Test
	void getDocumentSucceeds() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);

		mockMvc.perform(get("/api/documents/{id}", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(documentId))
			.andExpect(jsonPath("$.originalFilename").value("notes.txt"));
	}

	@Test
	void deleteDocumentSucceeds() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);

		mockMvc.perform(delete("/api/documents/{id}", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/documents/{id}", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isNotFound());
	}

	@Test
	void processReturnsUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/documents/{id}/process", UUID.randomUUID()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void processDocumentSucceeds() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);

		mockMvc.perform(post("/api/documents/{id}/process", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.document.id").value(documentId))
			.andExpect(jsonPath("$.document.status").value("PROCESSED"))
			.andExpect(jsonPath("$.document.chunkCount").value(1))
			.andExpect(jsonPath("$.document.processedAt").exists())
			.andExpect(jsonPath("$.document.processingError").doesNotExist());

		long chunkCount = documentChunkRepository.countByDocumentId(UUID.fromString(documentId));

		org.assertj.core.api.Assertions.assertThat(chunkCount).isEqualTo(1);
	}

	@Test
	void getDocumentChunksSucceeds() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);

		mockMvc.perform(post("/api/documents/{id}/process", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/documents/{id}/chunks", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.chunks[0].chunkIndex").value(0))
			.andExpect(jsonPath("$.chunks[0].content").value("first paragraph\n\nsecond paragraph for chunking"))
			.andExpect(jsonPath("$.chunks[0].charCount").value(46))
			.andExpect(jsonPath("$.chunks[0].tokenCount").value(12));
	}

	@Test
	void processFailsWithEmptyExtractedText() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);
		given(documentStorageService.download(anyString()))
			.willAnswer(invocation -> textStream("   \n   "));

		mockMvc.perform(post("/api/documents/{id}/process", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Extracted text is empty."));

		mockMvc.perform(get("/api/documents/{id}", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("FAILED"))
			.andExpect(jsonPath("$.processingError").value("Extracted text is empty."));
	}

	@Test
	void processFailsForDeletedDocument() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);

		mockMvc.perform(delete("/api/documents/{id}", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/documents/{id}/process", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Deleted documents cannot be processed."));
	}

	@Test
	void embedReturnsUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/documents/{id}/embed", UUID.randomUUID()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void embedProcessedDocumentSucceeds() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);
		processDocument(accessToken, documentId);

		mockMvc.perform(post("/api/documents/{id}/embed", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.document.id").value(documentId))
			.andExpect(jsonPath("$.document.embeddingStatus").value("EMBEDDED"))
			.andExpect(jsonPath("$.document.embeddedChunkCount").value(1))
			.andExpect(jsonPath("$.document.embeddedAt").exists())
			.andExpect(jsonPath("$.document.embeddingError").doesNotExist());
	}

	@Test
	void embedFailsForUnprocessedDocument() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);

		mockMvc.perform(post("/api/documents/{id}/embed", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Document must be processed before embedding."));
	}

	@Test
	void searchChunksReturnsUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/search/chunks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"query", "paragraph",
					"topK", 5
				))))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void searchChunksSucceedsWithAuthentication() throws Exception {
		String accessToken = registerAndGetToken();
		String documentId = uploadDocument(accessToken);
		processDocument(accessToken, documentId);

		mockMvc.perform(post("/api/documents/{id}/embed", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/search/chunks")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"query", "paragraph",
					"topK", 5
				))))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.query").value("paragraph"))
			.andExpect(jsonPath("$.topK").value(5))
			.andExpect(jsonPath("$.results[0].documentId").value(documentId))
			.andExpect(jsonPath("$.results[0].documentName").value("notes.txt"))
			.andExpect(jsonPath("$.results[0].chunkIndex").value(0))
			.andExpect(jsonPath("$.results[0].content").value("first paragraph\n\nsecond paragraph for chunking"))
			.andExpect(jsonPath("$.results[0].score").exists())
			.andExpect(jsonPath("$.results[0].distance").exists())
			.andExpect(jsonPath("$.results[0].embeddingModel").value("nomic-embed-text"));
	}

	private String uploadDocument(String accessToken) throws Exception {
		return uploadDocument(accessToken, textFile());
	}

	private String uploadDocument(String accessToken, MockMultipartFile file) throws Exception {
		MvcResult result = mockMvc.perform(multipart("/api/documents")
				.file(file)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.document.id");
	}

	private void processDocument(String accessToken, String documentId) throws Exception {
		mockMvc.perform(post("/api/documents/{id}/process", documentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk());
	}

	private String registerAndGetToken() throws Exception {
		return register().accessToken();
	}

	private RegisteredUser register() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"email", "document-user-" + UUID.randomUUID() + "@example.com",
					"password", PASSWORD,
					"name", "Document User"
				))))
			.andExpect(status().isCreated())
			.andReturn();

		String response = result.getResponse().getContentAsString();
		return new RegisteredUser(
			JsonPath.read(response, "$.accessToken"),
			UUID.fromString(JsonPath.read(response, "$.user.id"))
		);
	}

	private MockMultipartFile textFile() {
		return textFile("notes.txt", "hello world");
	}

	private MockMultipartFile textFile(String filename, String content) {
		return new MockMultipartFile(
			"file",
			filename,
			"text/plain",
			content.getBytes(StandardCharsets.UTF_8)
		);
	}

	private void createInaccessibleDocument(UUID userId) {
		UUID workspaceId = UUID.randomUUID();
		Timestamp timestamp = Timestamp.from(Instant.now());

		jdbcTemplate.update(
			"INSERT INTO workspaces (id, name, slug, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
			workspaceId,
			"Private Document Workspace",
			"private-documents-" + workspaceId,
			timestamp,
			timestamp
		);
		documentRepository.save(new Document(
			workspaceId,
			userId,
			"hidden.txt",
			"documents/" + workspaceId + "/hidden.txt",
			"text/plain",
			6
		));
	}

	private ByteArrayInputStream textStream(String text) {
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}

	private float[] testEmbedding() {
		float[] embedding = new float[768];
		embedding[0] = 1.0f;
		return embedding;
	}

	private record RegisteredUser(String accessToken, UUID userId) {
	}
}
