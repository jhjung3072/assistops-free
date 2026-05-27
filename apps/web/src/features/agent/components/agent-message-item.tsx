"use client";

import { Bot, Loader2, UserRound } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { AgentSourceList } from "@/features/agent/components/agent-source-list";
import type { AgentChatMessage } from "@/types/agent";

type AgentMessageItemProps = {
  message: AgentChatMessage;
};

export function AgentMessageItem({ message }: AgentMessageItemProps) {
  if (message.role === "USER") {
    return (
      <div className="flex justify-end">
        <div className="max-w-[86%] rounded-lg bg-primary px-4 py-3 text-sm leading-6 text-primary-foreground">
          <div className="mb-2 flex items-center justify-end gap-2 text-xs font-medium opacity-80">
            <UserRound aria-hidden="true" className="size-4" />
            You
          </div>
          <p className="whitespace-pre-wrap">{message.content}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex justify-start">
      <Card className="max-w-[92%]">
        <CardHeader>
          <CardTitle className="flex flex-wrap items-center gap-2">
            <Bot aria-hidden="true" />
            Assistant
            {message.model ? (
              <Badge variant="secondary">{message.model}</Badge>
            ) : null}
            {message.promptTemplateName && message.promptVersion ? (
              <Badge variant="outline">
                Prompt {message.promptTemplateName} v{message.promptVersion}
              </Badge>
            ) : null}
            {message.totalMs != null ? (
              <Badge variant="outline">
                총 {formatDuration(message.totalMs)}
              </Badge>
            ) : null}
            {message.chatGenerationMs != null ? (
              <Badge variant="outline">
                LLM {formatDuration(message.chatGenerationMs)}
              </Badge>
            ) : null}
            <Badge variant="outline">
              sources {message.sourceCount ?? message.sources.length}
            </Badge>
            {message.isStreaming ? (
              <Badge variant="outline">streaming</Badge>
            ) : null}
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4">
          {message.error ? (
            <p className="rounded-md border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
              {message.error}
            </p>
          ) : null}
          {message.content ? (
            <p className="whitespace-pre-wrap text-sm leading-7">
              {message.content}
            </p>
          ) : (
            <p className="flex items-center gap-2 text-sm text-muted-foreground">
              <Loader2 aria-hidden="true" className="size-4 animate-spin" />
              로컬 LLM이 답변을 생성하고 있습니다...
            </p>
          )}
          <AgentSourceList sources={message.sources} />
        </CardContent>
      </Card>
    </div>
  );
}

function formatDuration(ms: number) {
  if (ms >= 1000) {
    return `${(ms / 1000).toFixed(1)}초`;
  }

  return `${ms}ms`;
}
