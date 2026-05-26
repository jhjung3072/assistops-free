package com.assistops.api.document;

import java.io.InputStream;

public record DocumentDownload(
	Document document,
	InputStream inputStream
) {
}
