export type DocumentStatus =
  | "UPLOADED"
  | "PROCESSING"
  | "PROCESSED"
  | "FAILED"
  | "DELETED";

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
  createdAt: string;
  updatedAt: string;
};

export type DocumentUploadResponse = {
  document: Document;
};

export type DocumentListResponse = {
  documents: Document[];
};

export type DocumentProcessingResponse = {
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
  createdAt: string;
};

export type DocumentChunkListResponse = {
  chunks: DocumentChunk[];
};
