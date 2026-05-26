package com.assistops.api.agent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentChatMessageSourceRepository extends JpaRepository<AgentChatMessageSource, UUID> {

	List<AgentChatMessageSource> findByMessageIdOrderByCreatedAtAsc(UUID messageId);

	List<AgentChatMessageSource> findByMessageIdInOrderByCreatedAtAsc(Collection<UUID> messageIds);

	void deleteByMessageIdIn(Collection<UUID> messageIds);
}
