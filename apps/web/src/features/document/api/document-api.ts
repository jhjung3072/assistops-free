import { apiRequest, apiRequestBlob } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";
import type {
  Document,
  DocumentListResponse,
  DocumentUploadResponse,
} from "@/types/document";

export type UploadDocumentInput = {
  file: File;
  workspaceId?: string;
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

export function getDocuments() {
  return apiRequest<ApiResponse<DocumentListResponse>>("/api/documents");
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
