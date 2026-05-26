package com.assistops.api.auth;

import com.assistops.api.user.User;
import com.assistops.api.user.UserRole;
import java.util.UUID;

public record AuthUserResponse(
	UUID id,
	String email,
	String name,
	UserRole role
) {

	public static AuthUserResponse from(User user) {
		return new AuthUserResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getRole()
		);
	}
}
