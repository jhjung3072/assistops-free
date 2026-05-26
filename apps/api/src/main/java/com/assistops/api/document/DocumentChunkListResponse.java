package com.assistops.api.document;

import java.util.List;

public record DocumentChunkListResponse(
	List<DocumentChunkResponse> chunks
) {
}
