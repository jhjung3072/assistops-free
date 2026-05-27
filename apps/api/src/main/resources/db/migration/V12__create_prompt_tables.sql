CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(40) NOT NULL,
    active_version_id UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_prompt_templates_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_prompt_templates_created_by
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE prompt_versions (
    id UUID PRIMARY KEY,
    prompt_template_id UUID NOT NULL,
    version INTEGER NOT NULL,
    system_prompt TEXT NOT NULL,
    user_prompt_template TEXT NOT NULL,
    context_template TEXT,
    model VARCHAR(255),
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_prompt_versions_template
        FOREIGN KEY (prompt_template_id) REFERENCES prompt_templates (id) ON DELETE CASCADE,
    CONSTRAINT fk_prompt_versions_created_by
        FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT uk_prompt_versions_template_version
        UNIQUE (prompt_template_id, version)
);

ALTER TABLE prompt_templates
    ADD CONSTRAINT fk_prompt_templates_active_version
    FOREIGN KEY (active_version_id) REFERENCES prompt_versions (id) ON DELETE SET NULL;

CREATE INDEX idx_prompt_templates_workspace_type
    ON prompt_templates (workspace_id, type, updated_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_prompt_versions_template_version
    ON prompt_versions (prompt_template_id, version DESC);
