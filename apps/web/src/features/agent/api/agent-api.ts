import { apiRequest } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";
import type {
  AgentChatMessageRequest,
  AgentChatSessionCreateRequest,
  AgentChatSessionDetail,
  AgentChatSessionListResponse,
} from "@/types/agent";

export function createAgentSession(
  request: AgentChatSessionCreateRequest = {},
) {
  return apiRequest<ApiResponse<AgentChatSessionDetail>>(
    "/api/agent/sessions",
    {
      method: "POST",
      body: JSON.stringify(request),
    },
  );
}

export function getAgentSessions() {
  return apiRequest<ApiResponse<AgentChatSessionListResponse>>(
    "/api/agent/sessions",
  );
}

export function getAgentSession(id: string) {
  return apiRequest<ApiResponse<AgentChatSessionDetail>>(
    `/api/agent/sessions/${id}`,
  );
}

export function sendAgentMessage(
  sessionId: string,
  request: AgentChatMessageRequest,
) {
  return apiRequest<ApiResponse<AgentChatSessionDetail>>(
    `/api/agent/sessions/${sessionId}/messages`,
    {
      method: "POST",
      body: JSON.stringify(request),
    },
  );
}

export function deleteAgentSession(id: string) {
  return apiRequest<void>(`/api/agent/sessions/${id}`, {
    method: "DELETE",
  });
}
