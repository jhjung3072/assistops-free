"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { History, Trash2 } from "lucide-react";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  deleteRagAnswer,
  getRagAnswer,
  getRagAnswers,
} from "@/features/rag/api/rag-api";
import { getApiErrorMessage } from "@/lib/api/client";
import type { RagAnswerResponse } from "@/types/rag";

type RagAnswerHistoryProps = {
  selectedAnswerId: string | null;
  onSelectAnswer: (answer: RagAnswerResponse) => void;
  onDeleteSelected: (answerId: string) => void;
};

export function RagAnswerHistory({
  selectedAnswerId,
  onSelectAnswer,
  onDeleteSelected,
}: RagAnswerHistoryProps) {
  const queryClient = useQueryClient();

  const answersQuery = useQuery({
    queryKey: ["rag", "answers"],
    queryFn: getRagAnswers,
    retry: false,
  });

  const detailMutation = useMutation({
    mutationFn: getRagAnswer,
    onSuccess: onSelectAnswer,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteRagAnswer,
    onSuccess: (_data, answerId) => {
      queryClient.invalidateQueries({ queryKey: ["rag", "answers"] });
      onDeleteSelected(answerId);
    },
  });

  const answers = answersQuery.data?.answers ?? [];

  return (
    <section className="grid gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">최근 질문</h2>
        <Badge variant="outline">{answers.length}</Badge>
      </div>

      {answersQuery.isError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(
              answersQuery.error,
              "RAG 답변 이력을 불러오지 못했습니다.",
            )}
          </AlertDescription>
        </Alert>
      ) : null}

      {detailMutation.isError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(
              detailMutation.error,
              "RAG 답변 상세를 불러오지 못했습니다.",
            )}
          </AlertDescription>
        </Alert>
      ) : null}

      {deleteMutation.isError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(
              deleteMutation.error,
              "RAG 답변을 삭제하지 못했습니다.",
            )}
          </AlertDescription>
        </Alert>
      ) : null}

      {answersQuery.isLoading ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          답변 이력을 불러오는 중...
        </p>
      ) : null}

      {!answersQuery.isLoading && answers.length === 0 ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          아직 저장된 RAG 답변이 없습니다.
        </p>
      ) : null}

      {answers.map((answer) => (
        <Card
          key={answer.answerId}
          size="sm"
          className={
            selectedAnswerId === answer.answerId ? "border-primary" : undefined
          }
        >
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <History aria-hidden="true" />
              <button
                type="button"
                className="min-w-0 flex-1 truncate text-left"
                onClick={() => detailMutation.mutate(answer.answerId)}
              >
                {answer.question}
              </button>
            </CardTitle>
          </CardHeader>
          <CardContent className="grid gap-3">
            <p className="line-clamp-3 text-sm text-muted-foreground">
              {answer.answerPreview}
            </p>
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex flex-wrap gap-2">
                <Badge variant="secondary">{answer.model}</Badge>
                <Badge variant="outline">sources {answer.sourceCount}</Badge>
                {answer.totalMs != null ? (
                  <Badge variant="outline">
                    {formatDuration(answer.totalMs)}
                  </Badge>
                ) : null}
                <Badge variant="outline">
                  {new Date(answer.createdAt).toLocaleDateString()}
                </Badge>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={() => deleteMutation.mutate(answer.answerId)}
                disabled={deleteMutation.isPending}
              >
                <Trash2 aria-hidden="true" />
                삭제
              </Button>
            </div>
          </CardContent>
        </Card>
      ))}
    </section>
  );
}

function formatDuration(ms: number) {
  if (ms >= 1000) {
    return `${(ms / 1000).toFixed(1)}초`;
  }

  return `${ms}ms`;
}
