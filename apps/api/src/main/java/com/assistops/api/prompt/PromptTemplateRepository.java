package com.assistops.api.prompt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {

	List<PromptTemplate> findByWorkspaceIdInAndDeletedAtIsNullOrderByUpdatedAtDesc(Collection<UUID> workspaceIds);

	List<PromptTemplate> findByWorkspaceIdInAndTypeAndDeletedAtIsNullOrderByUpdatedAtDesc(
		Collection<UUID> workspaceIds,
		PromptType type
	);

	Optional<PromptTemplate> findByIdAndWorkspaceIdInAndDeletedAtIsNull(UUID id, Collection<UUID> workspaceIds);

	Optional<PromptTemplate> findFirstByWorkspaceIdAndTypeAndDeletedAtIsNullAndActiveVersionIdIsNotNullOrderByUpdatedAtDesc(
		UUID workspaceId,
		PromptType type
	);
}
