import type { PageResponse } from "@/types/api";

export type RagAnswerRequest = {
  question: string;
  topK?: number;
  workspaceId?: string;
};

export type RagAnswerSource = {
  id: string;
  documentId: string;
  documentName: string;
  chunkId: string;
  chunkIndex: number;
  content: string;
  score: number | null;
};

export type RagLatencyMetrics = {
  totalMs: number | null;
  queryEmbeddingMs: number | null;
  vectorSearchMs: number | null;
  promptBuildMs: number | null;
  chatGenerationMs: number | null;
  answerPersistMs: number | null;
  sourceCount: number | null;
  promptContextCharCount: number | null;
  answerCharCount: number | null;
};

export type RagAnswerResponse = {
  answerId: string;
  workspaceId: string;
  userId: string;
  question: string;
  answer: string;
  model: string;
  topK: number;
  createdAt: string;
  sources: RagAnswerSource[];
  latencyMetrics?: RagLatencyMetrics | null;
};

export type RagAnswerDetail = RagAnswerResponse;

export type RagAnswerSummary = {
  answerId: string;
  workspaceId: string;
  question: string;
  answerPreview: string;
  model: string;
  topK: number;
  sourceCount: number;
  totalMs: number | null;
  createdAt: string;
};

export type RagAnswerListResponse = {
  answers: RagAnswerSummary[];
  page: PageResponse<RagAnswerSummary>;
};
