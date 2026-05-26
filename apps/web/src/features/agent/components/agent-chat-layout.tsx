"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bot } from "lucide-react";
import { useState } from "react";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import {
  createAgentSession,
  deleteAgentSession,
  getAgentSession,
  getAgentSessions,
  sendAgentMessageStream,
} from "@/features/agent/api/agent-api";
import { AgentMessageInput } from "@/features/agent/components/agent-message-input";
import { AgentMessageList } from "@/features/agent/components/agent-message-list";
import { AgentSessionList } from "@/features/agent/components/agent-session-list";
import { getApiErrorMessage } from "@/lib/api/client";
import type {
  AgentChatMessage,
  AgentChatMessageSource,
  AgentChatStreamError,
} from "@/types/agent";

export function AgentChatLayout() {
  const queryClient = useQueryClient();
  const [selectedSessionId, setSelectedSessionId] = useState<string | null>(
    null,
  );
  const [streamingMessages, setStreamingMessages] = useState<
    AgentChatMessage[]
  >([]);
  const [streamErrorMessage, setStreamErrorMessage] = useState<string | null>(
    null,
  );

  const sessionsQuery = useQuery({
    queryKey: ["agent", "sessions"],
    queryFn: getAgentSessions,
    retry: false,
  });

  const createMutation = useMutation({
    mutationFn: () => createAgentSession(),
    onSuccess: (session) => {
      setSelectedSessionId(session.id);
      queryClient.invalidateQueries({ queryKey: ["agent", "sessions"] });
      queryClient.setQueryData(["agent", "sessions", session.id], session);
    },
  });

  const streamMutation = useMutation({
    mutationFn: async ({
      sessionId,
      content,
      topK,
    }: {
      sessionId: string;
      content: string;
      topK: number;
    }) => {
      const startedAt = new Date().toISOString();
      const userMessageId = `optimistic-user-${Date.now()}`;
      const assistantMessageId = `streaming-assistant-${Date.now()}`;

      setStreamErrorMessage(null);
      setStreamingMessages([
        createOptimisticUserMessage(sessionId, userMessageId, content, startedAt),
        createStreamingAssistantMessage(sessionId, assistantMessageId, startedAt),
      ]);

      await sendAgentMessageStream(sessionId, { content, topK }, {
        onMetadata: (metadata) => {
          setStreamingMessages((messages) =>
            messages.map((message) => {
              if (message.id === userMessageId) {
                return {
                  ...message,
                  id: metadata.userMessageId,
                };
              }

              if (message.id === assistantMessageId) {
                return {
                  ...message,
                  model: metadata.model,
                };
              }

              return message;
            }),
          );
        },
        onSource: (source) => {
          setStreamingMessages((messages) =>
            messages.map((message) => {
              if (message.id !== assistantMessageId) {
                return message;
              }

              const nextSource: AgentChatMessageSource = {
                id: `${assistantMessageId}-source-${message.sources.length}`,
                ...source,
              };

              return {
                ...message,
                sourceCount: message.sources.length + 1,
                sources: [...message.sources, nextSource],
              };
            }),
          );
        },
        onToken: (token) => {
          setStreamingMessages((messages) =>
            messages.map((message) =>
              message.id === assistantMessageId
                ? {
                    ...message,
                    content: `${message.content}${token.content}`,
                  }
                : message,
            ),
          );
        },
        onLatency: (latencyMetrics) => {
          setStreamingMessages((messages) =>
            messages.map((message) =>
              message.id === assistantMessageId
                ? {
                    ...message,
                    totalMs: latencyMetrics.totalMs,
                    chatGenerationMs: latencyMetrics.chatGenerationMs,
                    sourceCount: latencyMetrics.sourceCount,
                    latencyMetrics,
                  }
                : message,
            ),
          );
        },
        onDone: (done) => {
          setStreamingMessages((messages) =>
            messages.map((message) =>
              message.id === assistantMessageId
                ? {
                    ...message,
                    id: done.assistantMessageId,
                    ragAnswerId: done.ragAnswerId,
                    isStreaming: false,
                  }
                : message,
            ),
          );
        },
        onError: (streamError: AgentChatStreamError) => {
          setStreamErrorMessage(streamError.message);
          setStreamingMessages((messages) =>
            messages.map((message) =>
              message.id === assistantMessageId
                ? {
                    ...message,
                    isStreaming: false,
                    error: streamError.message,
                  }
                : message,
            ),
          );
        },
      });

      return sessionId;
    },
    onSuccess: (sessionId) => {
      setSelectedSessionId(sessionId);
      setStreamingMessages([]);
      queryClient.invalidateQueries({ queryKey: ["agent", "sessions"] });
      queryClient.invalidateQueries({ queryKey: ["agent", "sessions", sessionId] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteAgentSession,
    onSuccess: (_data, sessionId) => {
      if (selectedSessionId === sessionId) {
        setSelectedSessionId(null);
      }
      queryClient.removeQueries({ queryKey: ["agent", "sessions", sessionId] });
      queryClient.invalidateQueries({ queryKey: ["agent", "sessions"] });
    },
  });

  const sessions = sessionsQuery.data?.sessions ?? [];
  const activeSessionId = selectedSessionId ?? sessions[0]?.id ?? null;

  const sessionQuery = useQuery({
    queryKey: ["agent", "sessions", activeSessionId],
    queryFn: () => getAgentSession(activeSessionId ?? ""),
    enabled: Boolean(activeSessionId),
    retry: false,
  });

  const selectedSession = sessionQuery.data;
  const messages = selectedSession?.messages ?? [];
  const error =
    sessionsQuery.error ??
    sessionQuery.error ??
    createMutation.error ??
    (streamErrorMessage ? null : streamMutation.error) ??
    deleteMutation.error;

  function handleSendMessage(content: string, topK: number) {
    if (!activeSessionId) {
      return;
    }

    streamMutation.mutate({
      sessionId: activeSessionId,
      content,
      topK,
    });
  }

  return (
    <section className="grid gap-6 lg:grid-cols-[280px_minmax(0,1fr)]">
      <AgentSessionList
        sessions={sessions}
        selectedSessionId={activeSessionId}
        isLoading={sessionsQuery.isLoading}
        isCreating={createMutation.isPending}
        isDeleting={deleteMutation.isPending}
        onCreateSession={() => createMutation.mutate()}
        onSelectSession={(sessionId) => {
          setStreamingMessages([]);
          setStreamErrorMessage(null);
          setSelectedSessionId(sessionId);
        }}
        onDeleteSession={(sessionId) => deleteMutation.mutate(sessionId)}
      />

      <div className="grid content-start gap-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <Badge variant="outline" className="mb-3">
              Agent Chat
            </Badge>
            <h2 className="flex items-center gap-2 text-2xl font-semibold">
              <Bot aria-hidden="true" />
              {selectedSession?.title ?? "세션을 선택해 주세요"}
            </h2>
            <p className="mt-2 text-sm text-muted-foreground">
              세션형 UI로 질문과 RAG 답변을 저장합니다. 답변은 fetch streaming으로 점진 표시되지만, 총 생성 시간은 로컬 모델 성능에 영향을 받습니다.
            </p>
          </div>
          {selectedSession ? (
            <Badge variant="secondary">
              messages {selectedSession.messages.length}
            </Badge>
          ) : null}
        </div>

        {streamErrorMessage ? (
          <Alert variant="destructive">
            <AlertDescription>{streamErrorMessage}</AlertDescription>
          </Alert>
        ) : null}

        {error ? (
          <Alert variant="destructive">
            <AlertDescription>
              {getApiErrorMessage(error, "Agent Chat 요청을 처리하지 못했습니다.")}
            </AlertDescription>
          </Alert>
        ) : null}

        {activeSessionId ? (
          <>
            <AgentMessageList
              messages={[...messages, ...streamingMessages]}
              isLoading={sessionQuery.isLoading}
              isSending={streamMutation.isPending}
            />
            <AgentMessageInput
              disabled={!activeSessionId}
              isSending={streamMutation.isPending}
              onSendMessage={handleSendMessage}
            />
          </>
        ) : (
          <div className="grid min-h-96 place-items-center rounded-lg border bg-card p-8 text-center">
            <div className="max-w-sm">
              <Bot
                aria-hidden="true"
                className="mx-auto mb-3 size-9 text-muted-foreground"
              />
              <p className="font-medium">새 채팅을 만들면 질문을 시작할 수 있습니다.</p>
              <p className="mt-2 text-sm text-muted-foreground">
                문서를 업로드하고 Process, Embed를 완료한 뒤 질문하면 출처와 latency가 함께 저장됩니다.
              </p>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}

function createOptimisticUserMessage(
  sessionId: string,
  id: string,
  content: string,
  createdAt: string,
): AgentChatMessage {
  return {
    id,
    sessionId,
    role: "USER",
    content,
    ragAnswerId: null,
    model: null,
    totalMs: null,
    chatGenerationMs: null,
    sourceCount: 0,
    createdAt,
    sources: [],
  };
}

function createStreamingAssistantMessage(
  sessionId: string,
  id: string,
  createdAt: string,
): AgentChatMessage {
  return {
    id,
    sessionId,
    role: "ASSISTANT",
    content: "",
    ragAnswerId: null,
    model: null,
    totalMs: null,
    chatGenerationMs: null,
    sourceCount: 0,
    createdAt,
    sources: [],
    isStreaming: true,
  };
}
