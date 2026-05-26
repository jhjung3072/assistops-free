package com.assistops.api.agent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentChatMessageRepository extends JpaRepository<AgentChatMessage, UUID> {

	List<AgentChatMessage> findBySessionIdOrderByCreatedAtAscIdAsc(UUID sessionId);

	long countBySessionIdAndRole(UUID sessionId, AgentChatRole role);

	void deleteBySessionId(UUID sessionId);

	void deleteBySessionIdIn(Collection<UUID> sessionIds);
}
