import type { PageResponse } from "@/types/api";

export type DocumentStatus =
  | "UPLOADED"
  | "PROCESSING"
  | "PROCESSED"
  | "FAILED"
  | "DELETED";

export type DocumentEmbeddingStatus =
  | "NOT_EMBEDDED"
  | "EMBEDDING"
  | "EMBEDDED"
  | "EMBEDDING_FAILED";

export type Document = {
  id: string;
  workspaceId: string;
  uploadedBy: string;
  originalFilename: string;
  contentType: string | null;
  sizeBytes: number;
  status: DocumentStatus;
  chunkCount: number;
  processedAt: string | null;
  processingError: string | null;
  embeddingStatus: DocumentEmbeddingStatus;
  embeddedChunkCount: number;
  embeddedAt: string | null;
  embeddingError: string | null;
  createdAt: string;
  updatedAt: string;
};

export type DocumentUploadResponse = {
  document: Document;
};

export type DocumentListResponse = {
  documents: Document[];
  page: PageResponse<Document>;
};

export type DocumentProcessingResponse = {
  document: Document;
};

export type DocumentEmbeddingResponse = {
  document: Document;
};

export type DocumentChunk = {
  id: string;
  documentId: string;
  workspaceId: string;
  chunkIndex: number;
  content: string;
  tokenCount: number | null;
  charCount: number;
  embeddedAt: string | null;
  embeddingModel: string | null;
  createdAt: string;
};

export type DocumentChunkListResponse = {
  chunks: DocumentChunk[];
};
