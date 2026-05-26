export type ChunkSearchRequest = {
  query: string;
  topK?: number;
  workspaceId?: string;
};

export type ChunkSearchResult = {
  documentId: string;
  documentName: string;
  chunkId: string;
  chunkIndex: number;
  content: string;
  score: number;
  distance: number;
  embeddingModel: string | null;
};

export type ChunkSearchResponse = {
  query: string;
  topK: number;
  results: ChunkSearchResult[];
};
