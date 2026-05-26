package com.assistops.api.document;

import java.time.LocalDateTime;

public record DocumentSearchCondition(
	String keyword,
	DocumentStatus status,
	DocumentEmbeddingStatus embeddingStatus,
	LocalDateTime createdFrom,
	LocalDateTime createdTo,
	Integer page,
	Integer size
) {
}
