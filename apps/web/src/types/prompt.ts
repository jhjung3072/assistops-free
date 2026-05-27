export type PromptType = "RAG_ANSWER" | "AGENT_CHAT";

export type PromptVersion = {
  id: string;
  promptTemplateId: string;
  version: number;
  systemPrompt: string;
  userPromptTemplate: string;
  contextTemplate: string | null;
  model: string | null;
  createdBy: string;
  createdAt: string;
  active: boolean;
};

export type PromptTemplate = {
  id: string;
  workspaceId: string;
  name: string;
  description: string | null;
  type: PromptType;
  activeVersionId: string | null;
  activeVersion: PromptVersion | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
};

export type PromptTemplateListResponse = {
  prompts: PromptTemplate[];
};

export type PromptVersionListResponse = {
  versions: PromptVersion[];
};

export type PromptTemplateCreateRequest = {
  workspaceId?: string;
  name: string;
  description?: string;
  type: PromptType;
  systemPrompt?: string;
  userPromptTemplate?: string;
  contextTemplate?: string;
  model?: string;
};

export type PromptVersionCreateRequest = {
  systemPrompt: string;
  userPromptTemplate: string;
  contextTemplate?: string;
  model?: string;
};
