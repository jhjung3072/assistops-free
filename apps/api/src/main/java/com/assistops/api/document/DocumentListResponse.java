package com.assistops.api.document;

import java.util.List;

public record DocumentListResponse(
	List<DocumentResponse> documents
) {
}
