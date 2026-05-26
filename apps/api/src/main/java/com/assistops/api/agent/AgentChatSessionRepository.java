package com.assistops.api.agent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentChatSessionRepository extends JpaRepository<AgentChatSession, UUID> {

	List<AgentChatSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);

	Optional<AgentChatSession> findByIdAndUserId(UUID id, UUID userId);
}
