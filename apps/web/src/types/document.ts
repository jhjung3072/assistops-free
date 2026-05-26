export type DocumentStatus = "UPLOADED" | "DELETED";

export type Document = {
  id: string;
  workspaceId: string;
  uploadedBy: string;
  originalFilename: string;
  contentType: string | null;
  sizeBytes: number;
  status: DocumentStatus;
  createdAt: string;
  updatedAt: string;
};

export type DocumentUploadResponse = {
  document: Document;
};

export type DocumentListResponse = {
  documents: Document[];
};
