package com.assistops.api.document;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

	void deleteByDocumentId(UUID documentId);

	long countByDocumentId(UUID documentId);

	List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);
}
