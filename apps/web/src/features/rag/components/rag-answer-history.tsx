"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ChevronLeft,
  ChevronRight,
  History,
  RotateCcw,
  Search,
  Trash2,
} from "lucide-react";
import { type FormEvent, useState } from "react";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  deleteRagAnswer,
  getRagAnswer,
  getRagAnswers,
  type RagAnswerListParams,
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
  const [filters, setFilters] = useState<RagAnswerListParams>({
    keyword: "",
    model: "",
    page: 0,
    size: 20,
  });
  const [draftFilters, setDraftFilters] =
    useState<RagAnswerListParams>(filters);

  const answersQuery = useQuery({
    queryKey: ["rag", "answers", filters],
    queryFn: () => getRagAnswers(filters),
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
  const page = answersQuery.data?.page;

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFilters({
      ...draftFilters,
      page: 0,
    });
  }

  function handleReset() {
    const nextFilters: RagAnswerListParams = {
      keyword: "",
      model: "",
      page: 0,
      size: 20,
    };

    setDraftFilters(nextFilters);
    setFilters(nextFilters);
  }

  function handlePageChange(nextPage: number) {
    setFilters((currentFilters) => ({
      ...currentFilters,
      page: Math.max(0, nextPage),
    }));
  }

  return (
    <section className="grid gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">최근 질문</h2>
        <Badge variant="outline">{page?.totalElements ?? answers.length}</Badge>
      </div>

      <form
        onSubmit={handleSearch}
        className="grid gap-3 rounded-lg border bg-card p-4 sm:grid-cols-[minmax(0,1fr)_180px_auto] sm:items-end"
      >
        <div className="grid gap-2">
          <Label htmlFor="rag-history-keyword">Keyword</Label>
          <Input
            id="rag-history-keyword"
            value={draftFilters.keyword ?? ""}
            onChange={(event) =>
              setDraftFilters((currentFilters) => ({
                ...currentFilters,
                keyword: event.target.value,
              }))
            }
            placeholder="질문 또는 답변 검색"
          />
        </div>
        <div className="grid gap-2">
          <Label htmlFor="rag-history-model">Model</Label>
          <Input
            id="rag-history-model"
            value={draftFilters.model ?? ""}
            onChange={(event) =>
              setDraftFilters((currentFilters) => ({
                ...currentFilters,
                model: event.target.value,
              }))
            }
            placeholder="llama3.2"
          />
        </div>
        <div className="flex flex-wrap gap-2">
          <Button type="submit">
            <Search aria-hidden="true" />
            검색
          </Button>
          <Button type="button" variant="outline" onClick={handleReset}>
            <RotateCcw aria-hidden="true" />
            초기화
          </Button>
        </div>
      </form>

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
          조건에 맞는 RAG 답변이 없습니다.
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

      {page ? (
        <div className="flex flex-col gap-3 rounded-lg border bg-card px-4 py-3 text-sm sm:flex-row sm:items-center sm:justify-between">
          <span className="text-muted-foreground">
            총 {page.totalElements.toLocaleString()}개 · {page.page + 1} /{" "}
            {Math.max(page.totalPages, 1)} 페이지
          </span>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(page.page - 1)}
              disabled={!page.hasPrevious || answersQuery.isFetching}
            >
              <ChevronLeft aria-hidden="true" />
              이전
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(page.page + 1)}
              disabled={!page.hasNext || answersQuery.isFetching}
            >
              다음
              <ChevronRight aria-hidden="true" />
            </Button>
          </div>
        </div>
      ) : null}
    </section>
  );
}

function formatDuration(ms: number) {
  if (ms >= 1000) {
    return `${(ms / 1000).toFixed(1)}초`;
  }

  return `${ms}ms`;
}
