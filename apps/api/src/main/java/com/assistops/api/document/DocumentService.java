package com.assistops.api.document;

import com.assistops.api.global.exception.BadRequestException;
import com.assistops.api.global.exception.NotFoundException;
import com.assistops.api.user.User;
import com.assistops.api.workspace.WorkspaceMember;
import com.assistops.api.workspace.WorkspaceMemberRepository;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

	private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "txt", "md");
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"application/pdf",
		"text/plain",
		"text/markdown"
	);
	private static final String OCTET_STREAM = "application/octet-stream";

	private final DocumentRepository documentRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final DocumentStorageService documentStorageService;

	public DocumentService(
		DocumentRepository documentRepository,
		WorkspaceMemberRepository workspaceMemberRepository,
		DocumentStorageService documentStorageService
	) {
		this.documentRepository = documentRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.documentStorageService = documentStorageService;
	}

	@Transactional
	public DocumentUploadResponse upload(User user, MultipartFile file, UUID requestedWorkspaceId) {
		UUID workspaceId = resolveWorkspaceId(user.getId(), requestedWorkspaceId);
		ValidatedFile validatedFile = validateFile(file);
		String objectKey = createObjectKey(workspaceId, validatedFile.extension());

		documentStorageService.upload(objectKey, file);

		Document document = documentRepository.save(new Document(
			workspaceId,
			user.getId(),
			validatedFile.originalFilename(),
			objectKey,
			validatedFile.contentType(),
			file.getSize()
		));

		return new DocumentUploadResponse(DocumentResponse.from(document));
	}

	@Transactional(readOnly = true)
	public DocumentListResponse getDocuments(User user) {
		List<UUID> workspaceIds = accessibleWorkspaceIds(user.getId());
		List<DocumentResponse> documents = documentRepository
			.findByWorkspaceIdInAndStatusNotOrderByCreatedAtDesc(workspaceIds, DocumentStatus.DELETED)
			.stream()
			.map(DocumentResponse::from)
			.toList();

		return new DocumentListResponse(documents);
	}

	@Transactional(readOnly = true)
	public DocumentResponse getDocument(User user, UUID documentId) {
		return DocumentResponse.from(findAccessibleUploadedDocument(user.getId(), documentId));
	}

	@Transactional(readOnly = true)
	public DocumentDownload download(User user, UUID documentId) {
		Document document = findAccessibleUploadedDocument(user.getId(), documentId);
		InputStream inputStream = documentStorageService.download(document.getStoredObjectKey());

		return new DocumentDownload(document, inputStream);
	}

	@Transactional
	public void delete(User user, UUID documentId) {
		Document document = findAccessibleUploadedDocument(user.getId(), documentId);

		documentStorageService.delete(document.getStoredObjectKey());
		document.markDeleted();
	}

	private Document findAccessibleUploadedDocument(UUID userId, UUID documentId) {
		List<UUID> workspaceIds = accessibleWorkspaceIds(userId);
		Document document = documentRepository.findByIdAndWorkspaceIdIn(documentId, workspaceIds)
			.orElseThrow(() -> new NotFoundException("Document not found."));

		if (document.getStatus() == DocumentStatus.DELETED) {
			throw new NotFoundException("Document not found.");
		}

		return document;
	}

	private UUID resolveWorkspaceId(UUID userId, UUID requestedWorkspaceId) {
		if (requestedWorkspaceId != null) {
			if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(requestedWorkspaceId, userId)) {
				throw new NotFoundException("Workspace not found.");
			}

			return requestedWorkspaceId;
		}

		return workspaceMemberRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
			.map(WorkspaceMember::getWorkspaceId)
			.orElseThrow(() -> new NotFoundException("Workspace membership not found."));
	}

	private List<UUID> accessibleWorkspaceIds(UUID userId) {
		List<UUID> workspaceIds = workspaceMemberRepository.findByUserId(userId)
			.stream()
			.map(WorkspaceMember::getWorkspaceId)
			.toList();

		if (workspaceIds.isEmpty()) {
			throw new NotFoundException("Workspace membership not found.");
		}

		return workspaceIds;
	}

	private ValidatedFile validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("File must not be empty.");
		}

		if (file.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new BadRequestException("File size must be 10MB or smaller.");
		}

		String originalFilename = StringUtils.cleanPath(
			StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "document"
		);
		String extension = extensionOf(originalFilename);

		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new BadRequestException("Only PDF, TXT, and MD files are supported.");
		}

		String contentType = file.getContentType();

		if (!isAllowedContentType(contentType, extension)) {
			throw new BadRequestException("Unsupported file content type.");
		}

		return new ValidatedFile(originalFilename, extension, contentType);
	}

	private boolean isAllowedContentType(String contentType, String extension) {
		if (!StringUtils.hasText(contentType)) {
			return false;
		}

		String normalizedContentType = contentType.toLowerCase(Locale.ROOT);

		if (ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
			return true;
		}

		return OCTET_STREAM.equals(normalizedContentType) && Set.of("txt", "md").contains(extension);
	}

	private String extensionOf(String filename) {
		int extensionStart = filename.lastIndexOf('.');

		if (extensionStart < 0 || extensionStart == filename.length() - 1) {
			return "";
		}

		return filename.substring(extensionStart + 1).toLowerCase(Locale.ROOT);
	}

	private String createObjectKey(UUID workspaceId, String extension) {
		return "documents/" + workspaceId + "/" + UUID.randomUUID() + "." + extension;
	}

	private record ValidatedFile(
		String originalFilename,
		String extension,
		String contentType
	) {
	}
}
