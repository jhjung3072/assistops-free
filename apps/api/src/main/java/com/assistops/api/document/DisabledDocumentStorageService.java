package com.assistops.api.document;

import com.assistops.api.document.storage.DocumentStorageException;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "false")
public class DisabledDocumentStorageService implements DocumentStorageService {

	@Override
	public void upload(String objectKey, MultipartFile file) {
		throw disabled();
	}

	@Override
	public InputStream download(String objectKey) {
		throw disabled();
	}

	@Override
	public void delete(String objectKey) {
		throw disabled();
	}

	private DocumentStorageException disabled() {
		return new DocumentStorageException("Document storage is disabled.");
	}
}
