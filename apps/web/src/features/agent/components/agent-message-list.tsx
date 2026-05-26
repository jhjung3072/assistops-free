"use client";

import { Loader2, MessageSquareText } from "lucide-react";

import { AgentMessageItem } from "@/features/agent/components/agent-message-item";
import type { AgentChatMessage } from "@/types/agent";

type AgentMessageListProps = {
  messages: AgentChatMessage[];
  isLoading: boolean;
  isSending: boolean;
};

export function AgentMessageList({
  messages,
  isLoading,
  isSending,
}: AgentMessageListProps) {
  if (isLoading) {
    return (
      <div className="rounded-lg border bg-card px-4 py-6 text-sm text-muted-foreground">
        메시지를 불러오는 중...
      </div>
    );
  }

  if (messages.length === 0) {
    return (
      <div className="grid min-h-72 place-items-center rounded-lg border bg-card p-8 text-center">
        <div className="max-w-sm">
          <MessageSquareText
            aria-hidden="true"
            className="mx-auto mb-3 size-8 text-muted-foreground"
          />
          <p className="font-medium">첫 질문을 입력해 보세요.</p>
          <p className="mt-2 text-sm text-muted-foreground">
            현재는 이전 대화 맥락을 LLM prompt에 넣지 않고, 각 질문마다 RAG Answer Service를 호출합니다.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-h-[62vh] overflow-y-auto rounded-lg border bg-muted/30 p-4">
      <div className="grid gap-4">
        {messages.map((message) => (
          <AgentMessageItem key={message.id} message={message} />
        ))}
        {isSending ? (
          <div className="flex items-center gap-2 rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
            <Loader2 aria-hidden="true" className="size-4 animate-spin" />
            관련 chunk를 검색하고 로컬 LLM이 답변을 생성하고 있습니다.
          </div>
        ) : null}
      </div>
    </div>
  );
}
