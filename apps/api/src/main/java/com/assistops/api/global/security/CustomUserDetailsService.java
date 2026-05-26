package com.assistops.api.global.security;

import com.assistops.api.user.UserRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		String email = normalizeEmail(username);

		return userRepository.findByEmail(email)
			.map(CustomUserDetails::new)
			.orElseThrow(() -> new UsernameNotFoundException("User not found."));
	}

	public CustomUserDetails loadById(UUID userId) {
		return userRepository.findById(userId)
			.map(CustomUserDetails::new)
			.orElseThrow(() -> new UsernameNotFoundException("User not found."));
	}

	private String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}
}
