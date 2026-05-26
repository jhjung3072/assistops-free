package com.assistops.api.rag;

import java.time.LocalDateTime;

public record RagAnswerSearchCondition(
	String keyword,
	String model,
	LocalDateTime createdFrom,
	LocalDateTime createdTo,
	Integer page,
	Integer size
) {
}
