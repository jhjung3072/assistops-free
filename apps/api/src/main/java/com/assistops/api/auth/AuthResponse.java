package com.assistops.api.auth;

import com.assistops.api.user.User;

public record AuthResponse(
	String accessToken,
	String tokenType,
	AuthUserResponse user
) {

	private static final String BEARER_TOKEN_TYPE = "Bearer";

	public static AuthResponse bearer(String accessToken, User user) {
		return new AuthResponse(accessToken, BEARER_TOKEN_TYPE, AuthUserResponse.from(user));
	}
}
