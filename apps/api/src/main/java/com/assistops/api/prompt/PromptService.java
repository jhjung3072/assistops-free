package com.assistops.api.prompt;

import com.assistops.api.global.exception.BadRequestException;
import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.user.User;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PromptService {

	private final PromptTemplateRepository promptTemplateRepository;
	private final PromptVersionRepository promptVersionRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;

	public PromptService(
		PromptTemplateRepository promptTemplateRepository,
		PromptVersionRepository promptVersionRepository,
		WorkspaceMemberRepository workspaceMemberRepository
	) {
		this.promptTemplateRepository = promptTemplateRepository;
		this.promptVersionRepository = promptVersionRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
	}

	@Transactional
	public PromptTemplateResponse createTemplate(User user, PromptTemplateCreateRequest request) {
		UUID workspaceId = resolveWorkspaceId(user.getId(), request.workspaceId());
		PromptTemplate template = promptTemplateRepository.save(new PromptTemplate(
			workspaceId,
			request.name().trim(),
			normalizeBlankToNull(request.description()),
			request.type(),
			user.getId()
		));
		PromptVersion version = promptVersionRepository.save(new PromptVersion(
			template.getId(),
			1,
			defaultIfBlank(request.systemPrompt(), DefaultPromptContent.SYSTEM_PROMPT),
			defaultIfBlank(request.userPromptTemplate(), DefaultPromptContent.USER_PROMPT_TEMPLATE),
			defaultIfBlank(request.contextTemplate(), DefaultPromptContent.CONTEXT_TEMPLATE),
			normalizeBlankToNull(request.model()),
			user.getId()
		));
		validatePromptVersion(version);
		template.activate(version.getId());

		return PromptTemplateResponse.from(template, version);
	}

	@Transactional(readOnly = true)
	public PromptTemplateListResponse getTemplates(User user, PromptType type) {
		List<UUID> workspaceIds = accessibleWorkspaceIds(user.getId());
		List<PromptTemplate> templates = type == null
			? promptTemplateRepository.findByWorkspaceIdInAndDeletedAtIsNullOrderByUpdatedAtDesc(workspaceIds)
			: promptTemplateRepository.findByWorkspaceIdInAndTypeAndDeletedAtIsNullOrderByUpdatedAtDesc(
				workspaceIds,
				type
			);
		List<UUID> activeVersionIds = templates.stream()
			.map(PromptTemplate::getActiveVersionId)
			.filter(java.util.Objects::nonNull)
			.toList();
		List<PromptVersion> activeVersions = activeVersionIds.isEmpty()
			? List.of()
			: promptVersionRepository.findByIdIn(activeVersionIds);

		return new PromptTemplateListResponse(PromptTemplateResponse.from(templates, activeVersions));
	}

	@Transactional(readOnly = true)
	public PromptTemplateResponse getTemplate(User user, UUID templateId) {
		PromptTemplate template = findAccessibleTemplate(user.getId(), templateId);
		PromptVersion activeVersion = template.getActiveVersionId() == null
			? null
			: promptVersionRepository.findById(template.getActiveVersionId()).orElse(null);

		return PromptTemplateResponse.from(template, activeVersion);
	}

	@Transactional
	public void deleteTemplate(User user, UUID templateId) {
		PromptTemplate template = findAccessibleTemplate(user.getId(), templateId);
		template.markDeleted();
	}

	@Transactional
	public PromptVersionResponse createVersion(User user, UUID templateId, PromptVersionCreateRequest request) {
		PromptTemplate template = findAccessibleTemplate(user.getId(), templateId);
		int nextVersion = promptVersionRepository.findTopByPromptTemplateIdOrderByVersionDesc(template.getId())
			.map(PromptVersion::getVersion)
			.orElse(0) + 1;
		PromptVersion version = new PromptVersion(
			template.getId(),
			nextVersion,
			request.systemPrompt().trim(),
			request.userPromptTemplate().trim(),
			normalizeBlankToNull(request.contextTemplate()),
			normalizeBlankToNull(request.model()),
			user.getId()
		);
		validatePromptVersion(version);
		PromptVersion savedVersion = promptVersionRepository.save(version);

		if (template.getActiveVersionId() == null) {
			template.activate(savedVersion.getId());
		}

		return PromptVersionResponse.from(savedVersion, template.getActiveVersionId());
	}

	@Transactional(readOnly = true)
	public PromptVersionListResponse getVersions(User user, UUID templateId) {
		PromptTemplate template = findAccessibleTemplate(user.getId(), templateId);
		List<PromptVersionResponse> versions = promptVersionRepository
			.findByPromptTemplateIdOrderByVersionDesc(template.getId())
			.stream()
			.map(version -> PromptVersionResponse.from(version, template.getActiveVersionId()))
			.toList();

		return new PromptVersionListResponse(versions);
	}

	@Transactional
	public PromptVersionResponse activateVersion(User user, UUID templateId, UUID versionId) {
		PromptTemplate template = findAccessibleTemplate(user.getId(), templateId);
		PromptVersion version = promptVersionRepository.findByIdAndPromptTemplateId(versionId, template.getId())
			.orElseThrow(() -> new NotFoundException("Prompt version not found."));

		template.activate(version.getId());

		return PromptVersionResponse.from(version, template.getActiveVersionId());
	}

	@Transactional
	public PromptTemplateResponse getActiveTemplate(User user, PromptType type, UUID requestedWorkspaceId) {
		UUID workspaceId = resolveWorkspaceId(user.getId(), requestedWorkspaceId);
		PromptResolvedVersion resolvedVersion = resolveActivePromptVersion(user, workspaceId, type);

		return PromptTemplateResponse.from(resolvedVersion.template(), resolvedVersion.version());
	}

	@Transactional
	public PromptResolvedVersion resolveActivePromptVersion(User user, UUID workspaceId, PromptType type) {
		if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
			throw new NotFoundException("Workspace not found.");
		}

		Optional<PromptTemplate> existingTemplate =
			promptTemplateRepository.findFirstByWorkspaceIdAndTypeAndDeletedAtIsNullAndActiveVersionIdIsNotNullOrderByUpdatedAtDesc(
				workspaceId,
				type
			);

		if (existingTemplate.isPresent()) {
			PromptTemplate template = existingTemplate.get();
			PromptVersion version = promptVersionRepository.findById(template.getActiveVersionId())
				.orElseThrow(() -> new NotFoundException("Active prompt version not found."));

			return new PromptResolvedVersion(template, version);
		}

		return createDefaultPrompt(user, workspaceId, type);
	}

	@Transactional(readOnly = true)
	public PromptVersionMetadata getMetadata(UUID promptVersionId) {
		if (promptVersionId == null) {
			return null;
		}

		return promptVersionRepository.findById(promptVersionId)
			.flatMap(version -> promptTemplateRepository.findById(version.getPromptTemplateId())
				.map(template -> new PromptVersionMetadata(
					version.getId(),
					template.getName(),
					version.getVersion()
				)))
			.orElse(PromptVersionMetadata.empty(promptVersionId));
	}

	private PromptResolvedVersion createDefaultPrompt(User user, UUID workspaceId, PromptType type) {
		PromptTemplate template = promptTemplateRepository.save(new PromptTemplate(
			workspaceId,
			DefaultPromptContent.defaultName(type),
			DefaultPromptContent.defaultDescription(type),
			type,
			user.getId()
		));
		PromptVersion version = promptVersionRepository.save(new PromptVersion(
			template.getId(),
			1,
			DefaultPromptContent.SYSTEM_PROMPT,
			DefaultPromptContent.USER_PROMPT_TEMPLATE,
			DefaultPromptContent.CONTEXT_TEMPLATE,
			null,
			user.getId()
		));
		template.activate(version.getId());

		return new PromptResolvedVersion(template, version);
	}

	private PromptTemplate findAccessibleTemplate(UUID userId, UUID templateId) {
		List<UUID> workspaceIds = accessibleWorkspaceIds(userId);

		return promptTemplateRepository.findByIdAndWorkspaceIdInAndDeletedAtIsNull(templateId, workspaceIds)
			.orElseThrow(() -> new NotFoundException("Prompt template not found."));
	}

	private UUID resolveWorkspaceId(UUID userId, UUID requestedWorkspaceId) {
		if (requestedWorkspaceId != null) {
			if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(requestedWorkspaceId, userId)) {
				throw new NotFoundException("Workspace not found.");
			}

			return requestedWorkspaceId;
		}

		return workspaceMemberRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
			.map(WorkspaceMember::getWorkspaceId)
			.orElseThrow(() -> new NotFoundException("Workspace membership not found."));
	}

	private List<UUID> accessibleWorkspaceIds(UUID userId) {
		List<UUID> workspaceIds = workspaceMemberRepository.findByUserId(userId)
			.stream()
			.map(WorkspaceMember::getWorkspaceId)
			.toList();

		if (workspaceIds.isEmpty()) {
			throw new NotFoundException("Workspace membership not found.");
		}

		return workspaceIds;
	}

	private void validatePromptVersion(PromptVersion version) {
		if (!version.getUserPromptTemplate().contains("{{question}}")) {
			throw new BadRequestException("userPromptTemplate must include {{question}}.");
		}
		if (!version.getUserPromptTemplate().contains("{{context}}")) {
			throw new BadRequestException("userPromptTemplate must include {{context}}.");
		}
	}

	private String defaultIfBlank(String value, String fallback) {
		return StringUtils.hasText(value) ? value.trim() : fallback;
	}

	private String normalizeBlankToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
