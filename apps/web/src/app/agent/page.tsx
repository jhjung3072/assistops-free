"use client";

import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import { AgentChatLayout } from "@/features/agent/components/agent-chat-layout";
import { AuthGuard } from "@/features/auth/components/auth-guard";

export default function AgentPage() {
  return (
    <>
      <AppHeader />
      <AppShell>
        <AuthGuard>
          <section className="max-w-3xl">
            <Badge variant="outline" className="mb-4">
              Agent Chat UI
            </Badge>
            <h1 className="text-3xl font-semibold sm:text-4xl">Agent</h1>
            <p className="mt-3 text-muted-foreground">
              기존 RAG Answer 기능을 세션형 채팅 UX로 사용합니다. assistant 답변은 streaming으로 표시되며, multi-turn memory와 tool calling은 후속 단계입니다.
            </p>
          </section>

          <AgentChatLayout />
        </AuthGuard>
      </AppShell>
    </>
  );
}
