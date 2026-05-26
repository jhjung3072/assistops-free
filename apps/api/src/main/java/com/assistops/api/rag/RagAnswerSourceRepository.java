package com.assistops.api.rag;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RagAnswerSourceRepository extends JpaRepository<RagAnswerSource, UUID> {

	List<RagAnswerSource> findByRagAnswerIdOrderByCreatedAtAsc(UUID ragAnswerId);

	void deleteByRagAnswerId(UUID ragAnswerId);

	long countByRagAnswerId(UUID ragAnswerId);
}
