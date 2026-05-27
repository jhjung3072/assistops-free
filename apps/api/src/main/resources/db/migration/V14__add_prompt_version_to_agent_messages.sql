ALTER TABLE agent_chat_messages
    ADD COLUMN prompt_version_id UUID;

ALTER TABLE agent_chat_messages
    ADD CONSTRAINT fk_agent_chat_messages_prompt_version
    FOREIGN KEY (prompt_version_id) REFERENCES prompt_versions (id) ON DELETE SET NULL;

CREATE INDEX idx_agent_chat_messages_prompt_version_id
    ON agent_chat_messages (prompt_version_id);
