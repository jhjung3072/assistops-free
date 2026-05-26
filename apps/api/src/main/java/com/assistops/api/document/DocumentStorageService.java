package com.assistops.api.document;

import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {

	void upload(String objectKey, MultipartFile file);

	InputStream download(String objectKey);

	void delete(String objectKey);
}
