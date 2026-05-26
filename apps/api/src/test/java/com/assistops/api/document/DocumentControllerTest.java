package com.assistops.api.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assistops.api.support.AbstractPostgresContainerTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

	@MockitoBean
	private DocumentStorageService documentStorageService;

	@BeforeEach
	void setUp() {
		doNothing().when(documentStorageService).upload(anyString(), any(MultipartFile.class));
		doNothing().when(documentStorageService).delete(anyString());
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
			.andExpect(jsonPath("$.documents[0].status").value("UPLOADED"));
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

	private String uploadDocument(String accessToken) throws Exception {
		MvcResult result = mockMvc.perform(multipart("/api/documents")
				.file(textFile())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.document.id");
	}

	private String registerAndGetToken() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"email", "document-user-" + UUID.randomUUID() + "@example.com",
					"password", PASSWORD,
					"name", "Document User"
				))))
			.andExpect(status().isCreated())
			.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}

	private MockMultipartFile textFile() {
		return new MockMultipartFile(
			"file",
			"notes.txt",
			"text/plain",
			"hello world".getBytes(StandardCharsets.UTF_8)
		);
	}
}
