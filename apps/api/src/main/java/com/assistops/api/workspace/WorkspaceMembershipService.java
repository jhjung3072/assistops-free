package com.assistops.api.workspace;

import com.assistops.api.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceMembershipService {

	private static final String DEFAULT_WORKSPACE_SLUG = "default";

	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;

	public WorkspaceMembershipService(
		WorkspaceRepository workspaceRepository,
		WorkspaceMemberRepository workspaceMemberRepository
	) {
		this.workspaceRepository = workspaceRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
	}

	@Transactional
	public void assignDefaultWorkspaceMembership(User user) {
		workspaceRepository.findBySlug(DEFAULT_WORKSPACE_SLUG)
			.filter(workspace -> !workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), user.getId()))
			.ifPresent(workspace -> workspaceMemberRepository.save(
				new WorkspaceMember(workspace.getId(), user.getId(), WorkspaceMemberRole.MEMBER)
			));
	}
}
