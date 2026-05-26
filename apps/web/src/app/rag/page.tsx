"use client";

import { useState } from "react";

import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import { AuthGuard } from "@/features/auth/components/auth-guard";
import { RagAnswerCard } from "@/features/rag/components/rag-answer-card";
import { RagAnswerHistory } from "@/features/rag/components/rag-answer-history";
import { RagQuestionForm } from "@/features/rag/components/rag-question-form";
import type { RagAnswerResponse } from "@/types/rag";

export default function RagPage() {
  const [answer, setAnswer] = useState<RagAnswerResponse | null>(null);

  function handleDeleteSelected(answerId: string) {
    if (answer?.answerId === answerId) {
      setAnswer(null);
    }
  }

  return (
    <>
      <AppHeader />
      <AppShell>
        <AuthGuard>
          <section className="max-w-3xl">
            <Badge variant="outline" className="mb-4">
              RAG Q&A
            </Badge>
            <h1 className="text-3xl font-semibold sm:text-4xl">RAG</h1>
            <p className="mt-3 text-muted-foreground">
              semantic search 결과를 근거로 Ollama local chat model이 답변을 생성합니다.
            </p>
          </section>

          <section className="grid gap-6 lg:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
            <div className="grid content-start gap-6">
              <RagQuestionForm onAnswer={setAnswer} />
              <RagAnswerCard answer={answer} />
            </div>
            <RagAnswerHistory
              selectedAnswerId={answer?.answerId ?? null}
              onSelectAnswer={setAnswer}
              onDeleteSelected={handleDeleteSelected}
            />
          </section>
        </AuthGuard>
      </AppShell>
    </>
  );
}
