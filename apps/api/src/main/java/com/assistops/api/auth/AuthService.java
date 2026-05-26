package com.assistops.api.auth;

import com.assistops.api.global.exception.DuplicateEmailException;
import com.assistops.api.global.exception.InvalidCredentialsException;
import com.assistops.api.global.exception.UnauthorizedException;
import com.assistops.api.global.security.CustomUserDetails;
import com.assistops.api.global.security.JwtTokenProvider;
import com.assistops.api.user.User;
import com.assistops.api.user.UserRepository;
import com.assistops.api.user.UserRole;
import com.assistops.api.workspace.WorkspaceMembershipService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final WorkspaceMembershipService workspaceMembershipService;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtTokenProvider jwtTokenProvider,
		WorkspaceMembershipService workspaceMembershipService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.workspaceMembershipService = workspaceMembershipService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmail(email)) {
			throw new DuplicateEmailException("Email is already registered.");
		}

		User user = userRepository.saveAndFlush(new User(
			email,
			passwordEncoder.encode(request.password()),
			request.name().trim(),
			UserRole.USER
		));

		workspaceMembershipService.assignDefaultWorkspaceMembership(user);

		return AuthResponse.bearer(jwtTokenProvider.generateAccessToken(user), user);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
			.orElseThrow(this::invalidCredentials);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw invalidCredentials();
		}

		return AuthResponse.bearer(jwtTokenProvider.generateAccessToken(user), user);
	}

	@Transactional(readOnly = true)
	public AuthUserResponse getCurrentUser(CustomUserDetails userDetails) {
		if (userDetails == null) {
			throw new UnauthorizedException("Authentication is required.");
		}

		return AuthUserResponse.from(userDetails.getUser());
	}

	private InvalidCredentialsException invalidCredentials() {
		return new InvalidCredentialsException("Invalid email or password.");
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
