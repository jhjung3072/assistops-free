"use client";

import {
  ChevronLeft,
  ChevronRight,
  MessageSquarePlus,
  RotateCcw,
  Search,
  Trash2,
} from "lucide-react";
import { type FormEvent, useState } from "react";

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
import type { PageResponse } from "@/types/api";
import type { AgentChatSessionSummary } from "@/types/agent";
import type { AgentSessionListParams } from "@/features/agent/api/agent-api";

type AgentSessionListProps = {
  sessions: AgentChatSessionSummary[];
  page: PageResponse<AgentChatSessionSummary> | null;
  filters: AgentSessionListParams;
  selectedSessionId: string | null;
  isLoading: boolean;
  isFetching: boolean;
  isCreating: boolean;
  isDeleting: boolean;
  onCreateSession: () => void;
  onSelectSession: (sessionId: string) => void;
  onDeleteSession: (sessionId: string) => void;
  onFilterChange: (filters: AgentSessionListParams) => void;
};

export function AgentSessionList({
  sessions,
  page,
  filters,
  selectedSessionId,
  isLoading,
  isFetching,
  isCreating,
  isDeleting,
  onCreateSession,
  onSelectSession,
  onDeleteSession,
  onFilterChange,
}: AgentSessionListProps) {
  const [draftFilters, setDraftFilters] =
    useState<AgentSessionListParams>(filters);

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onFilterChange({
      ...draftFilters,
      page: 0,
    });
  }

  function handleReset() {
    const nextFilters: AgentSessionListParams = {
      keyword: "",
      page: 0,
      size: 20,
    };

    setDraftFilters(nextFilters);
    onFilterChange(nextFilters);
  }

  function handlePageChange(nextPage: number) {
    onFilterChange({
      ...filters,
      page: Math.max(0, nextPage),
    });
  }

  return (
    <section className="grid content-start gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">채팅 세션</h2>
        <Badge variant="outline">{page?.totalElements ?? sessions.length}</Badge>
      </div>

      <Button onClick={onCreateSession} disabled={isCreating}>
        <MessageSquarePlus aria-hidden="true" />
        새 채팅
      </Button>

      <form onSubmit={handleSearch} className="grid gap-2 rounded-lg border bg-card p-3">
        <Label htmlFor="agent-session-keyword">Keyword</Label>
        <Input
          id="agent-session-keyword"
          value={draftFilters.keyword ?? ""}
          onChange={(event) =>
            setDraftFilters((currentFilters) => ({
              ...currentFilters,
              keyword: event.target.value,
            }))
          }
          placeholder="세션 제목 검색"
        />
        <div className="flex flex-wrap gap-2">
          <Button type="submit" size="sm">
            <Search aria-hidden="true" />
            검색
          </Button>
          <Button type="button" variant="outline" size="sm" onClick={handleReset}>
            <RotateCcw aria-hidden="true" />
            초기화
          </Button>
        </div>
      </form>

      {isLoading ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          세션 목록을 불러오는 중...
        </p>
      ) : null}

      {!isLoading && sessions.length === 0 ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          조건에 맞는 채팅 세션이 없습니다.
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

      {page ? (
        <div className="grid gap-2 rounded-lg border bg-card px-3 py-3 text-sm">
          <span className="text-muted-foreground">
            총 {page.totalElements.toLocaleString()}개 · {page.page + 1} /{" "}
            {Math.max(page.totalPages, 1)} 페이지
          </span>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(page.page - 1)}
              disabled={!page.hasPrevious || isFetching}
            >
              <ChevronLeft aria-hidden="true" />
              이전
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(page.page + 1)}
              disabled={!page.hasNext || isFetching}
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
