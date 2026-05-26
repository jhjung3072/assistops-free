package com.assistops.api.document.processing;

import java.io.InputStream;

public interface DocumentTextExtractor {

	String extract(InputStream inputStream, String contentType, String filename);
}
