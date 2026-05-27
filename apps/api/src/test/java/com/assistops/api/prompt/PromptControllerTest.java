package com.assistops.api.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assistops.api.support.AbstractPostgresContainerTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PromptControllerTest extends AbstractPostgresContainerTest {

	private static final String PASSWORD = "password123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PromptTemplateRepository promptTemplateRepository;

	@Autowired
	private PromptVersionRepository promptVersionRepository;

	@Test
	void createPromptTemplateCreatesActiveFirstVersion() throws Exception {
		RegisteredUser user = register();

		MvcResult result = mockMvc.perform(post("/api/prompts")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(promptBody("RAG Prompt", "RAG_ANSWER"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.name").value("RAG Prompt"))
			.andExpect(jsonPath("$.type").value("RAG_ANSWER"))
			.andExpect(jsonPath("$.activeVersionId").exists())
			.andExpect(jsonPath("$.activeVersion.version").value(1))
			.andReturn();

		UUID templateId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
		UUID activeVersionId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.activeVersionId"));

		assertThat(promptTemplateRepository.findById(templateId)).isPresent();
		assertThat(promptVersionRepository.findById(activeVersionId)).isPresent();
	}

	@Test
	void createVersionAndActivateSucceeds() throws Exception {
		RegisteredUser user = register();
		UUID templateId = createPromptTemplate(user.accessToken(), "Versioned Prompt", "RAG_ANSWER");

		MvcResult versionResult = mockMvc.perform(post("/api/prompts/{id}/versions", templateId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"systemPrompt", "새 규칙: {{language}}로 답하세요.",
					"userPromptTemplate", "Context:\n{{context}}\nQuestion:\n{{question}}\nAnswer:",
					"contextTemplate", "[{{index}}] {{documentName}}\n{{content}}",
					"model", "llama3.2"
				))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.version").value(2))
			.andExpect(jsonPath("$.active").value(false))
			.andReturn();
		UUID versionId = UUID.fromString(JsonPath.read(versionResult.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(post("/api/prompts/{id}/versions/{versionId}/activate", templateId, versionId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(versionId.toString()))
			.andExpect(jsonPath("$.active").value(true));

		mockMvc.perform(get("/api/prompts/{id}", templateId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.activeVersionId").value(versionId.toString()))
			.andExpect(jsonPath("$.activeVersion.version").value(2));
	}

	@Test
	void activePromptCreatesDefaultWhenMissing() throws Exception {
		RegisteredUser user = register();

		mockMvc.perform(get("/api/prompts/active")
				.param("type", "AGENT_CHAT")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.type").value("AGENT_CHAT"))
			.andExpect(jsonPath("$.activeVersion.version").value(1));
	}

	@Test
	void deletedTemplateIsExcludedFromList() throws Exception {
		RegisteredUser user = register();
		UUID templateId = createPromptTemplate(user.accessToken(), "Delete Me", "RAG_ANSWER");

		mockMvc.perform(delete("/api/prompts/{id}", templateId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/prompts")
				.param("type", "RAG_ANSWER")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.prompts[?(@.id == '" + templateId + "')]").isEmpty());
	}

	@Test
	void inaccessibleWorkspacePromptReturnsNotFound() throws Exception {
		RegisteredUser user = register();
		UUID templateId = insertPrivateWorkspacePrompt(user.userId());

		mockMvc.perform(get("/api/prompts/{id}", templateId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Prompt template not found."));
	}

	private UUID createPromptTemplate(String accessToken, String name, String type) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/prompts")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(promptBody(name, type))))
			.andExpect(status().isOk())
			.andReturn();

		return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
	}

	private Map<String, String> promptBody(String name, String type) {
		return Map.of(
			"name", name,
			"type", type,
			"systemPrompt", "답변은 {{language}}로 하고 제공된 문서만 사용하세요.",
			"userPromptTemplate", "Context:\n{{context}}\n\nQuestion:\n{{question}}\n\nAnswer:",
			"contextTemplate", "[{{index}}] {{documentName}} / chunkIndex={{chunkIndex}}\n{{content}}"
		);
	}

	private UUID insertPrivateWorkspacePrompt(UUID userId) {
		UUID workspaceId = UUID.randomUUID();
		UUID templateId = UUID.randomUUID();
		UUID versionId = UUID.randomUUID();
		Timestamp timestamp = Timestamp.from(Instant.now());

		jdbcTemplate.update(
			"INSERT INTO workspaces (id, name, slug, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
			workspaceId,
			"Private Prompt Workspace",
			"private-prompts-" + workspaceId,
			timestamp,
			timestamp
		);
		jdbcTemplate.update(
			"""
			INSERT INTO prompt_templates
			(id, workspace_id, name, description, type, active_version_id, created_by, created_at, updated_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
			""",
			templateId,
			workspaceId,
			"Private Prompt",
			"private",
			"RAG_ANSWER",
			null,
			userId,
			timestamp,
			timestamp
		);
		jdbcTemplate.update(
			"""
			INSERT INTO prompt_versions
			(id, prompt_template_id, version, system_prompt, user_prompt_template, context_template, model, created_by, created_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
			""",
			versionId,
			templateId,
			1,
			DefaultPromptContent.SYSTEM_PROMPT,
			DefaultPromptContent.USER_PROMPT_TEMPLATE,
			DefaultPromptContent.CONTEXT_TEMPLATE,
			null,
			userId,
			timestamp
		);
		jdbcTemplate.update(
			"UPDATE prompt_templates SET active_version_id = ? WHERE id = ?",
			versionId,
			templateId
		);

		return templateId;
	}

	private RegisteredUser register() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"email", "prompt-user-" + UUID.randomUUID() + "@example.com",
					"password", PASSWORD,
					"name", "Prompt User"
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
