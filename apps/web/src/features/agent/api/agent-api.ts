import { getAccessTokenCookie } from "@/lib/cookie";
import { API_BASE_URL, ApiClientError, apiRequest } from "@/lib/api/client";
import { buildQueryString } from "@/lib/api/query-string";
import type { ApiResponse } from "@/types/api";
import type {
  AgentChatMessageRequest,
  AgentChatStreamCallbacks,
  AgentChatStreamDone,
  AgentChatStreamError,
  AgentChatStreamMetadata,
  AgentChatStreamSource,
  AgentChatStreamToken,
  AgentChatLatencyMetrics,
  AgentChatSessionCreateRequest,
  AgentChatSessionDetail,
  AgentChatSessionListResponse,
} from "@/types/agent";

export type AgentSessionListParams = {
  keyword?: string;
  createdFrom?: string;
  createdTo?: string;
  page?: number;
  size?: number;
};

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

export function getAgentSessions(params: AgentSessionListParams = {}) {
  return apiRequest<ApiResponse<AgentChatSessionListResponse>>(
    `/api/agent/sessions${buildQueryString(params)}`,
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

export async function sendAgentMessageStream(
  sessionId: string,
  request: AgentChatMessageRequest,
  callbacks: AgentChatStreamCallbacks = {},
) {
  const accessToken = getAccessTokenCookie();
  const response = await fetch(
    `${API_BASE_URL}/api/agent/sessions/${sessionId}/messages/stream`,
    {
      method: "POST",
      headers: {
        Accept: "text/event-stream",
        "Content-Type": "application/json",
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    const message =
      (await readErrorMessage(response)) ??
      (response.status === 401
        ? "인증이 필요합니다. 다시 로그인해 주세요."
        : "Agent Chat streaming 요청을 처리하지 못했습니다.");
    throw new ApiClientError(response.status, message);
  }

  if (!response.body) {
    throw new ApiClientError(0, "Streaming response body가 비어 있습니다.");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  let streamError: AgentChatStreamError | null = null;

  while (true) {
    const { done, value } = await reader.read();

    if (value) {
      buffer += decoder.decode(value, { stream: !done });
      const result = consumeSseBuffer(buffer, callbacks);
      buffer = result.remainingBuffer;
      streamError = result.streamError ?? streamError;
    }

    if (done) {
      buffer += decoder.decode();
      const result = consumeSseBuffer(`${buffer}\n\n`, callbacks);
      streamError = result.streamError ?? streamError;
      break;
    }
  }

  if (streamError) {
    throw new ApiClientError(0, streamError.message);
  }
}

export function deleteAgentSession(id: string) {
  return apiRequest<void>(`/api/agent/sessions/${id}`, {
    method: "DELETE",
  });
}

type ConsumeSseResult = {
  remainingBuffer: string;
  streamError: AgentChatStreamError | null;
};

function consumeSseBuffer(
  buffer: string,
  callbacks: AgentChatStreamCallbacks,
): ConsumeSseResult {
  const parts = buffer.split(/\r?\n\r?\n/);
  const remainingBuffer = parts.pop() ?? "";
  let streamError: AgentChatStreamError | null = null;

  for (const part of parts) {
    if (!part.trim()) {
      continue;
    }

    const event = parseSseEvent(part);
    if (!event) {
      continue;
    }

    const error = dispatchSseEvent(event, callbacks);
    if (error) {
      streamError = error;
    }
  }

  return {
    remainingBuffer,
    streamError,
  };
}

function parseSseEvent(rawEvent: string) {
  let eventName = "message";
  const dataLines: string[] = [];

  for (const line of rawEvent.split(/\r?\n/)) {
    if (line.startsWith("event:")) {
      eventName = line.slice("event:".length).trim();
    }

    if (line.startsWith("data:")) {
      dataLines.push(line.slice("data:".length).trimStart());
    }
  }

  if (dataLines.length === 0) {
    return null;
  }

  return {
    eventName,
    data: dataLines.join("\n"),
  };
}

function dispatchSseEvent(
  event: { eventName: string; data: string },
  callbacks: AgentChatStreamCallbacks,
) {
  const data = parseEventData(event.data);

  switch (event.eventName) {
    case "metadata":
      callbacks.onMetadata?.(data as AgentChatStreamMetadata);
      return null;
    case "token":
      callbacks.onToken?.(data as AgentChatStreamToken);
      return null;
    case "source":
      callbacks.onSource?.(data as AgentChatStreamSource);
      return null;
    case "latency":
      callbacks.onLatency?.(data as AgentChatLatencyMetrics);
      return null;
    case "done":
      callbacks.onDone?.(data as AgentChatStreamDone);
      return null;
    case "error": {
      const streamError = data as AgentChatStreamError;
      callbacks.onError?.(streamError);
      return streamError;
    }
    default:
      return null;
  }
}

function parseEventData(data: string) {
  try {
    return JSON.parse(data) as unknown;
  } catch {
    return data;
  }
}

async function readErrorMessage(response: Response) {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    const data = JSON.parse(text) as { message?: string };
    return data.message ?? null;
  } catch {
    return text;
  }
}
