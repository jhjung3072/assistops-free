package com.assistops.api.rag;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RagAnswerRepository extends JpaRepository<RagAnswer, UUID> {

	List<RagAnswer> findByWorkspaceIdInOrderByCreatedAtDesc(Collection<UUID> workspaceIds);

	List<RagAnswer> findByUserIdAndWorkspaceIdInOrderByCreatedAtDesc(UUID userId, Collection<UUID> workspaceIds);

	Optional<RagAnswer> findByIdAndWorkspaceIdIn(UUID id, Collection<UUID> workspaceIds);
}
