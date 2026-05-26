package com.assistops.api.workspace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Workspaces")
@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

	private final WorkspaceRepository workspaceRepository;

	public WorkspaceController(WorkspaceRepository workspaceRepository) {
		this.workspaceRepository = workspaceRepository;
	}

	@Operation(
		summary = "List workspaces",
		description = "Requires authentication. This phase returns all workspaces; user-specific filtering is planned next."
	)
	@GetMapping
	public List<WorkspaceResponse> getWorkspaces() {
		return workspaceRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"))
			.stream()
			.map(WorkspaceResponse::from)
			.toList();
	}
}
