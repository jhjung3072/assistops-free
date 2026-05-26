package com.assistops.api.workspace;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assistops.api.support.AbstractPostgresContainerTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class WorkspaceControllerTest extends AbstractPostgresContainerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getWorkspacesReturnsUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/workspaces"))
			.andExpect(status().isUnauthorized());
	}

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void getWorkspacesReturnsDefaultWorkspaceWithAuthentication() throws Exception {
		String accessToken = registerAndGetToken();

		mockMvc.perform(get("/api/workspaces")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$[0].name").value("Default Workspace"))
			.andExpect(jsonPath("$[0].slug").value("default"));
	}

	private String registerAndGetToken() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"email", "workspace-user-" + UUID.randomUUID() + "@example.com",
					"password", "password123",
					"name", "Workspace User"
				))))
			.andExpect(status().isCreated())
			.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}
}
