import { apiRequest, apiRequestBlob } from "@/lib/api/client";
import { buildQueryString } from "@/lib/api/query-string";
import type { ApiResponse } from "@/types/api";
import type {
  Document,
  DocumentEmbeddingStatus,
  DocumentChunkListResponse,
  DocumentEmbeddingResponse,
  DocumentListResponse,
  DocumentProcessingResponse,
  DocumentStatus,
  DocumentUploadResponse,
} from "@/types/document";

export type UploadDocumentInput = {
  file: File;
  workspaceId?: string;
};

export type DocumentListParams = {
  keyword?: string;
  status?: DocumentStatus | "";
  embeddingStatus?: DocumentEmbeddingStatus | "";
  createdFrom?: string;
  createdTo?: string;
  page?: number;
  size?: number;
};

export function uploadDocument({ file, workspaceId }: UploadDocumentInput) {
  const formData = new FormData();
  formData.append("file", file);

  if (workspaceId) {
    formData.append("workspaceId", workspaceId);
  }

  return apiRequest<ApiResponse<DocumentUploadResponse>>("/api/documents", {
    method: "POST",
    body: formData,
  });
}

export function getDocuments(params: DocumentListParams = {}) {
  return apiRequest<ApiResponse<DocumentListResponse>>(
    `/api/documents${buildQueryString(params)}`,
  );
}

export function getDocument(id: string) {
  return apiRequest<ApiResponse<Document>>(`/api/documents/${id}`);
}

export function deleteDocument(id: string) {
  return apiRequest<void>(`/api/documents/${id}`, {
    method: "DELETE",
  });
}

export function downloadDocument(id: string) {
  return apiRequestBlob(`/api/documents/${id}/download`);
}

export function processDocument(id: string) {
  return apiRequest<ApiResponse<DocumentProcessingResponse>>(
    `/api/documents/${id}/process`,
    {
      method: "POST",
    },
  );
}

export function getDocumentChunks(id: string) {
  return apiRequest<ApiResponse<DocumentChunkListResponse>>(
    `/api/documents/${id}/chunks`,
  );
}

export function embedDocument(id: string) {
  return apiRequest<ApiResponse<DocumentEmbeddingResponse>>(
    `/api/documents/${id}/embed`,
    {
      method: "POST",
    },
  );
}
