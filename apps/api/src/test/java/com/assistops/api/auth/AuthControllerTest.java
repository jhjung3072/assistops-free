package com.assistops.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assistops.api.support.AbstractPostgresContainerTest;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import com.assistops.api.workspace.WorkspaceMemberRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
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
class AuthControllerTest extends AbstractPostgresContainerTest {

	private static final String PASSWORD = "password123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WorkspaceMemberRepository workspaceMemberRepository;

	@Test
	void registerSucceeds() throws Exception {
		String email = uniqueEmail();

		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerBody(email))))
			.andExpect(status().isCreated())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.user.email").value(email))
			.andExpect(jsonPath("$.user.name").value("Test User"))
			.andExpect(jsonPath("$.user.role").value("USER"))
			.andExpect(jsonPath("$.user.passwordHash").doesNotExist())
			.andReturn();

		UUID userId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.user.id"));
		List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserId(userId);

		assertThat(memberships)
			.extracting(WorkspaceMember::getRole)
			.contains(WorkspaceMemberRole.MEMBER);
	}

	@Test
	void registerFailsWithDuplicateEmail() throws Exception {
		String email = uniqueEmail();
		registerAndGetToken(email);

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerBody(email))))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Email is already registered."));
	}

	@Test
	void loginSucceeds() throws Exception {
		String email = uniqueEmail();
		registerAndGetToken(email);

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginBody(email, PASSWORD))))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.user.email").value(email))
			.andExpect(jsonPath("$.user.passwordHash").doesNotExist());
	}

	@Test
	void loginFailsWithInvalidPassword() throws Exception {
		String email = uniqueEmail();
		registerAndGetToken(email);

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginBody(email, "wrong-password"))))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("Invalid email or password."));
	}

	@Test
	void meSucceedsWithAuthentication() throws Exception {
		String email = uniqueEmail();
		String accessToken = registerAndGetToken(email);

		mockMvc.perform(get("/api/auth/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.email").value(email))
			.andExpect(jsonPath("$.name").value("Test User"))
			.andExpect(jsonPath("$.role").value("USER"))
			.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	private String registerAndGetToken(String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerBody(email))))
			.andExpect(status().isCreated())
			.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}

	private Map<String, String> registerBody(String email) {
		return Map.of(
			"email", email,
			"password", PASSWORD,
			"name", "Test User"
		);
	}

	private Map<String, String> loginBody(String email, String password) {
		return Map.of(
			"email", email,
			"password", password
		);
	}

	private String uniqueEmail() {
		return "user-" + UUID.randomUUID() + "@example.com";
	}
}
