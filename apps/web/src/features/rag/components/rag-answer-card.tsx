"use client";

import { MessageSquareText } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { RagSourceList } from "@/features/rag/components/rag-source-list";
import type { RagAnswerResponse } from "@/types/rag";

type RagAnswerCardProps = {
  answer: RagAnswerResponse | null;
};

export function RagAnswerCard({ answer }: RagAnswerCardProps) {
  if (!answer) {
    return (
      <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
        질문을 입력하면 문서 기반 답변과 출처가 표시됩니다.
      </p>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <MessageSquareText aria-hidden="true" />
          RAG Answer
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-5">
        <dl className="grid gap-3 text-sm sm:grid-cols-3">
          <div>
            <dt className="text-muted-foreground">Model</dt>
            <dd className="mt-1 font-medium">{answer.model}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground">Top K</dt>
            <dd className="mt-1 font-medium">{answer.topK}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground">생성</dt>
            <dd className="mt-1 font-medium">
              {new Date(answer.createdAt).toLocaleString()}
            </dd>
          </div>
        </dl>

        {answer.promptTemplateName && answer.promptVersion ? (
          <div className="flex flex-wrap gap-2">
            <Badge variant="outline">
              Prompt {answer.promptTemplateName} v{answer.promptVersion}
            </Badge>
          </div>
        ) : null}

        {answer.latencyMetrics ? (
          <dl className="grid gap-2 text-sm sm:grid-cols-5">
            <MetricBadge
              label="총"
              value={formatDuration(answer.latencyMetrics.totalMs)}
            />
            <MetricBadge
              label="LLM 생성"
              value={formatDuration(answer.latencyMetrics.chatGenerationMs)}
            />
            <MetricBadge
              label="검색"
              value={formatDuration(
                sumMs(
                  answer.latencyMetrics.queryEmbeddingMs,
                  answer.latencyMetrics.vectorSearchMs,
                ),
              )}
            />
            <MetricBadge
              label="출처"
              value={`${answer.latencyMetrics.sourceCount ?? answer.sources.length}개`}
            />
            <MetricBadge
              label="Context"
              value={`${formatNumber(answer.latencyMetrics.promptContextCharCount)}자`}
            />
          </dl>
        ) : null}

        <div className="grid gap-2">
          <Badge variant="outline">Question</Badge>
          <p className="whitespace-pre-wrap rounded-md bg-muted p-3 text-sm leading-6">
            {answer.question}
          </p>
        </div>

        <div className="grid gap-2">
          <Badge variant="secondary">Answer</Badge>
          <p className="whitespace-pre-wrap rounded-md border bg-background p-3 text-sm leading-7">
            {answer.answer}
          </p>
        </div>

        <Separator />
        <RagSourceList sources={answer.sources} />
      </CardContent>
    </Card>
  );
}

function MetricBadge({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-muted-foreground">{label}</dt>
      <dd className="mt-1">
        <Badge variant="outline">{value}</Badge>
      </dd>
    </div>
  );
}

function sumMs(...values: Array<number | null | undefined>) {
  const knownValues = values.filter((value): value is number => value != null);

  if (knownValues.length === 0) {
    return null;
  }

  return knownValues.reduce((sum, value) => sum + value, 0);
}

function formatDuration(ms: number | null | undefined) {
  if (ms == null) {
    return "-";
  }

  if (ms >= 1000) {
    return `${(ms / 1000).toFixed(1)}초`;
  }

  return `${ms}ms`;
}

function formatNumber(value: number | null | undefined) {
  if (value == null) {
    return "-";
  }

  return value.toLocaleString();
}
