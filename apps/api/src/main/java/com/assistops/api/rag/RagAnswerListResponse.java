package com.assistops.api.rag;

import java.util.List;

public record RagAnswerListResponse(
	List<RagAnswerSummary> answers
) {
}
