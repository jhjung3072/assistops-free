package com.assistops.api.rag;

import com.assistops.api.global.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public record RagAnswerListResponse(
	List<RagAnswerSummary> answers,
	PageResponse<RagAnswerSummary> page
) {

	public static RagAnswerListResponse from(Page<RagAnswerSummary> page) {
		return new RagAnswerListResponse(page.getContent(), PageResponse.from(page));
	}
}
