import type { PageResponse } from "@/types/api";

export type AgentChatRole = "USER" | "ASSISTANT" | "SYSTEM";

export type AgentChatMessageSource = {
  id: string;
  documentId: string;
  documentName: string;
  chunkId: string;
  chunkIndex: number;
  content: string;
  score: number | null;
};

export type AgentChatLatencyMetrics = {
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

export type AgentChatMessage = {
  id: string;
  sessionId: string;
  role: AgentChatRole;
  content: string;
  ragAnswerId: string | null;
  promptVersionId: string | null;
  promptTemplateName: string | null;
  promptVersion: number | null;
  model: string | null;
  totalMs: number | null;
  chatGenerationMs: number | null;
  sourceCount: number | null;
  createdAt: string;
  sources: AgentChatMessageSource[];
  latencyMetrics?: AgentChatLatencyMetrics | null;
  isStreaming?: boolean;
  error?: string | null;
};

export type AgentChatSessionSummary = {
  id: string;
  workspaceId: string;
  userId: string;
  title: string;
  createdAt: string;
  updatedAt: string;
};

export type AgentChatSessionDetail = AgentChatSessionSummary & {
  messages: AgentChatMessage[];
};

export type AgentChatSessionListResponse = {
  sessions: AgentChatSessionSummary[];
  page: PageResponse<AgentChatSessionSummary>;
};

export type AgentChatSessionCreateRequest = {
  title?: string;
  workspaceId?: string;
};

export type AgentChatMessageRequest = {
  content: string;
  topK?: number;
};

export type AgentChatStreamMetadata = {
  sessionId: string;
  userMessageId: string;
  model: string;
};

export type AgentChatStreamToken = {
  content: string;
};

export type AgentChatStreamSource = Omit<AgentChatMessageSource, "id">;

export type AgentChatStreamDone = {
  assistantMessageId: string;
  ragAnswerId: string;
  promptVersionId: string | null;
  promptTemplateName: string | null;
  promptVersion: number | null;
};

export type AgentChatStreamError = {
  message: string;
};

export type AgentChatStreamCallbacks = {
  onMetadata?: (metadata: AgentChatStreamMetadata) => void;
  onToken?: (token: AgentChatStreamToken) => void;
  onSource?: (source: AgentChatStreamSource) => void;
  onLatency?: (latency: AgentChatLatencyMetrics) => void;
  onDone?: (done: AgentChatStreamDone) => void;
  onError?: (error: AgentChatStreamError) => void;
};
