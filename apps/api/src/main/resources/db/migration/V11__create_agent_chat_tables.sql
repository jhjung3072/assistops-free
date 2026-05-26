CREATE TABLE agent_chat_sessions (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_agent_chat_sessions_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_agent_chat_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE agent_chat_messages (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    role VARCHAR(40) NOT NULL,
    content TEXT NOT NULL,
    rag_answer_id UUID,
    model VARCHAR(255),
    total_ms BIGINT,
    chat_generation_ms BIGINT,
    source_count INTEGER,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_agent_chat_messages_session
        FOREIGN KEY (session_id) REFERENCES agent_chat_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_chat_messages_rag_answer
        FOREIGN KEY (rag_answer_id) REFERENCES rag_answers (id) ON DELETE SET NULL
);

CREATE TABLE agent_chat_message_sources (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    document_id UUID NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    chunk_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    score DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_agent_chat_message_sources_message
        FOREIGN KEY (message_id) REFERENCES agent_chat_messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_chat_message_sources_document
        FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT fk_agent_chat_message_sources_chunk
        FOREIGN KEY (chunk_id) REFERENCES document_chunks (id)
);

CREATE INDEX idx_agent_chat_sessions_user_updated_at
    ON agent_chat_sessions (user_id, updated_at DESC);

CREATE INDEX idx_agent_chat_sessions_workspace_updated_at
    ON agent_chat_sessions (workspace_id, updated_at DESC);

CREATE INDEX idx_agent_chat_messages_session_created_at
    ON agent_chat_messages (session_id, created_at ASC);

CREATE INDEX idx_agent_chat_message_sources_message_id
    ON agent_chat_message_sources (message_id);
