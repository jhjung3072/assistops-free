package com.assistops.api.document;

import com.assistops.api.global.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public record DocumentListResponse(
	List<DocumentResponse> documents,
	PageResponse<DocumentResponse> page
) {

	public static DocumentListResponse from(Page<DocumentResponse> page) {
		return new DocumentListResponse(page.getContent(), PageResponse.from(page));
	}
}
