CREATE TABLE documents (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    uploaded_by UUID NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_object_key VARCHAR(512) NOT NULL UNIQUE,
    content_type VARCHAR(255),
    size_bytes BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_documents_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces (id),
    CONSTRAINT fk_documents_uploaded_by
        FOREIGN KEY (uploaded_by)
        REFERENCES users (id)
);

CREATE INDEX idx_documents_workspace_status_created_at
    ON documents (workspace_id, status, created_at DESC);

CREATE INDEX idx_documents_uploaded_by
    ON documents (uploaded_by);
