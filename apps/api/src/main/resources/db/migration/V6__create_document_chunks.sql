CREATE TABLE document_chunks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    token_count INTEGER,
    char_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_document_chunks_document
        FOREIGN KEY (document_id)
        REFERENCES documents (id),
    CONSTRAINT fk_document_chunks_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces (id),
    CONSTRAINT uk_document_chunks_document_chunk_index
        UNIQUE (document_id, chunk_index)
);

CREATE INDEX idx_document_chunks_document_chunk_index
    ON document_chunks (document_id, chunk_index);

CREATE INDEX idx_document_chunks_workspace
    ON document_chunks (workspace_id);
