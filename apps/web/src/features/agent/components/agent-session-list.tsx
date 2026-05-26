"use client";

import { MessageSquarePlus, Trash2 } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { AgentChatSessionSummary } from "@/types/agent";

type AgentSessionListProps = {
  sessions: AgentChatSessionSummary[];
  selectedSessionId: string | null;
  isLoading: boolean;
  isCreating: boolean;
  isDeleting: boolean;
  onCreateSession: () => void;
  onSelectSession: (sessionId: string) => void;
  onDeleteSession: (sessionId: string) => void;
};

export function AgentSessionList({
  sessions,
  selectedSessionId,
  isLoading,
  isCreating,
  isDeleting,
  onCreateSession,
  onSelectSession,
  onDeleteSession,
}: AgentSessionListProps) {
  return (
    <section className="grid content-start gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">채팅 세션</h2>
        <Badge variant="outline">{sessions.length}</Badge>
      </div>

      <Button onClick={onCreateSession} disabled={isCreating}>
        <MessageSquarePlus aria-hidden="true" />
        새 채팅
      </Button>

      {isLoading ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          세션 목록을 불러오는 중...
        </p>
      ) : null}

      {!isLoading && sessions.length === 0 ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          아직 저장된 채팅 세션이 없습니다.
        </p>
      ) : null}

      <div className="grid gap-3">
        {sessions.map((session) => (
          <Card
            key={session.id}
            size="sm"
            className={
              selectedSessionId === session.id ? "border-primary" : undefined
            }
          >
            <CardHeader>
              <CardTitle>
                <button
                  type="button"
                  className="block min-w-0 truncate text-left"
                  onClick={() => onSelectSession(session.id)}
                >
                  {session.title}
                </button>
              </CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3">
              <p className="text-xs text-muted-foreground">
                업데이트 {new Date(session.updatedAt).toLocaleString()}
              </p>
              <Button
                variant="outline"
                size="sm"
                onClick={() => onDeleteSession(session.id)}
                disabled={isDeleting}
              >
                <Trash2 aria-hidden="true" />
                삭제
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
