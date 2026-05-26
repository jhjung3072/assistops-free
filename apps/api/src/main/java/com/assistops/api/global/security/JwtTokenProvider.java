package com.assistops.api.global.security;

import com.assistops.api.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final String secret;
	private final long accessTokenExpirationMinutes;
	private SecretKey signingKey;

	public JwtTokenProvider(
		@Value("${security.jwt.secret}") String secret,
		@Value("${security.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes
	) {
		this.secret = secret;
		this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
	}

	@PostConstruct
	void initializeSigningKey() {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

		if (keyBytes.length < 32) {
			throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256.");
		}

		signingKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccessToken(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(Duration.ofMinutes(accessTokenExpirationMinutes));

		return Jwts.builder()
			.subject(user.getId().toString())
			.claim("email", user.getEmail())
			.claim("name", user.getName())
			.claim("role", user.getRole().name())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(signingKey)
			.compact();
	}

	public UUID getUserId(String token) {
		return UUID.fromString(parseClaims(token).getSubject());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(signingKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
