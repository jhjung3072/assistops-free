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
  sendAgentMessage,
} from "@/features/agent/api/agent-api";
import { AgentMessageInput } from "@/features/agent/components/agent-message-input";
import { AgentMessageList } from "@/features/agent/components/agent-message-list";
import { AgentSessionList } from "@/features/agent/components/agent-session-list";
import { getApiErrorMessage } from "@/lib/api/client";

export function AgentChatLayout() {
  const queryClient = useQueryClient();
  const [selectedSessionId, setSelectedSessionId] = useState<string | null>(
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

  const sendMutation = useMutation({
    mutationFn: ({
      sessionId,
      content,
      topK,
    }: {
      sessionId: string;
      content: string;
      topK: number;
    }) => sendAgentMessage(sessionId, { content, topK }),
    onSuccess: (session) => {
      setSelectedSessionId(session.id);
      queryClient.setQueryData(["agent", "sessions", session.id], session);
      queryClient.invalidateQueries({ queryKey: ["agent", "sessions"] });
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
    sendMutation.error ??
    deleteMutation.error;

  function handleSendMessage(content: string, topK: number) {
    if (!activeSessionId) {
      return;
    }

    sendMutation.mutate({
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
        onSelectSession={setSelectedSessionId}
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
              세션형 UI로 질문과 RAG 답변을 저장합니다. 아직 이전 메시지를 LLM context로 재사용하지는 않습니다.
            </p>
          </div>
          {selectedSession ? (
            <Badge variant="secondary">
              messages {selectedSession.messages.length}
            </Badge>
          ) : null}
        </div>

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
              messages={messages}
              isLoading={sessionQuery.isLoading}
              isSending={sendMutation.isPending}
            />
            <AgentMessageInput
              disabled={!activeSessionId}
              isSending={sendMutation.isPending}
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
