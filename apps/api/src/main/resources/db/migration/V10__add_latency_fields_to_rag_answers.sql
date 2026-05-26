ALTER TABLE rag_answers
    ADD COLUMN IF NOT EXISTS total_ms BIGINT,
    ADD COLUMN IF NOT EXISTS query_embedding_ms BIGINT,
    ADD COLUMN IF NOT EXISTS vector_search_ms BIGINT,
    ADD COLUMN IF NOT EXISTS prompt_build_ms BIGINT,
    ADD COLUMN IF NOT EXISTS chat_generation_ms BIGINT,
    ADD COLUMN IF NOT EXISTS answer_persist_ms BIGINT,
    ADD COLUMN IF NOT EXISTS source_count INTEGER,
    ADD COLUMN IF NOT EXISTS prompt_context_char_count INTEGER,
    ADD COLUMN IF NOT EXISTS answer_char_count INTEGER;
