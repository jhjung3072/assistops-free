package com.assistops.api.prompt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "prompt_versions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptVersion {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "prompt_template_id", nullable = false)
	private UUID promptTemplateId;

	@Column(nullable = false)
	private int version;

	@Column(name = "system_prompt", nullable = false, columnDefinition = "text")
	private String systemPrompt;

	@Column(name = "user_prompt_template", nullable = false, columnDefinition = "text")
	private String userPromptTemplate;

	@Column(name = "context_template", columnDefinition = "text")
	private String contextTemplate;

	@Column(length = 255)
	private String model;

	@Column(name = "created_by", nullable = false)
	private UUID createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public PromptVersion(
		UUID promptTemplateId,
		int version,
		String systemPrompt,
		String userPromptTemplate,
		String contextTemplate,
		String model,
		UUID createdBy
	) {
		this.id = UUID.randomUUID();
		this.promptTemplateId = promptTemplateId;
		this.version = version;
		this.systemPrompt = systemPrompt;
		this.userPromptTemplate = userPromptTemplate;
		this.contextTemplate = contextTemplate;
		this.model = model;
		this.createdBy = createdBy;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
