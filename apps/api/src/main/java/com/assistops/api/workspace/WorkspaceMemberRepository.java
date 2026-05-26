package com.assistops.api.workspace;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

	boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

	List<WorkspaceMember> findByUserId(UUID userId);
}
