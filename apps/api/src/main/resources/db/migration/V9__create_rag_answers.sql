CREATE TABLE rag_answers (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    model VARCHAR(255) NOT NULL,
    top_k INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_rag_answers_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_rag_answers_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE rag_answer_sources (
    id UUID PRIMARY KEY,
    rag_answer_id UUID NOT NULL,
    document_id UUID NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    chunk_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    score DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_rag_answer_sources_answer
        FOREIGN KEY (rag_answer_id) REFERENCES rag_answers (id) ON DELETE CASCADE,
    CONSTRAINT fk_rag_answer_sources_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT fk_rag_answer_sources_chunk FOREIGN KEY (chunk_id) REFERENCES document_chunks (id)
);

CREATE INDEX idx_rag_answers_workspace_created_at
    ON rag_answers (workspace_id, created_at DESC);

CREATE INDEX idx_rag_answers_user_created_at
    ON rag_answers (user_id, created_at DESC);

CREATE INDEX idx_rag_answer_sources_answer_id
    ON rag_answer_sources (rag_answer_id);
