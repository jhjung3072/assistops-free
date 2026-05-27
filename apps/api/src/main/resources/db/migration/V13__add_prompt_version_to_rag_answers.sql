ALTER TABLE rag_answers
    ADD COLUMN prompt_version_id UUID;

ALTER TABLE rag_answers
    ADD CONSTRAINT fk_rag_answers_prompt_version
    FOREIGN KEY (prompt_version_id) REFERENCES prompt_versions (id) ON DELETE SET NULL;

CREATE INDEX idx_rag_answers_prompt_version_id
    ON rag_answers (prompt_version_id);
