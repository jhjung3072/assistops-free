package com.assistops.api.prompt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptVersionRepository extends JpaRepository<PromptVersion, UUID> {

	List<PromptVersion> findByPromptTemplateIdOrderByVersionDesc(UUID promptTemplateId);

	Optional<PromptVersion> findTopByPromptTemplateIdOrderByVersionDesc(UUID promptTemplateId);

	Optional<PromptVersion> findByIdAndPromptTemplateId(UUID id, UUID promptTemplateId);

	List<PromptVersion> findByIdIn(Collection<UUID> ids);
}
