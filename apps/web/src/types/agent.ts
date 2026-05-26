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

export type AgentChatMessage = {
  id: string;
  sessionId: string;
  role: AgentChatRole;
  content: string;
  ragAnswerId: string | null;
  model: string | null;
  totalMs: number | null;
  chatGenerationMs: number | null;
  sourceCount: number | null;
  createdAt: string;
  sources: AgentChatMessageSource[];
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
};

export type AgentChatSessionCreateRequest = {
  title?: string;
  workspaceId?: string;
};

export type AgentChatMessageRequest = {
  content: string;
  topK?: number;
};
