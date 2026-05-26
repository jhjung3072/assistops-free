"use client";

import { useMutation } from "@tanstack/react-query";
import { Search } from "lucide-react";
import type { FormEvent } from "react";
import { useState } from "react";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { searchChunks } from "@/features/search/api/search-api";
import { getApiErrorMessage } from "@/lib/api/client";
import type { ChunkSearchResponse } from "@/types/search";

type ChunkSearchFormProps = {
  onSearchComplete: (response: ChunkSearchResponse) => void;
};

export function ChunkSearchForm({ onSearchComplete }: ChunkSearchFormProps) {
  const [query, setQuery] = useState("");
  const [topK, setTopK] = useState(5);

  const mutation = useMutation({
    mutationFn: searchChunks,
    onSuccess: onSearchComplete,
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const normalizedQuery = query.trim();
    if (!normalizedQuery) {
      return;
    }

    mutation.mutate({
      query: normalizedQuery,
      topK,
    });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Semantic Search</CardTitle>
      </CardHeader>
      <CardContent>
        <form className="grid gap-4" onSubmit={handleSubmit}>
          {mutation.isError ? (
            <Alert variant="destructive">
              <AlertDescription>
                {getApiErrorMessage(
                  mutation.error,
                  "검색에 실패했습니다. Ollama embedding model이 준비되어 있는지 확인해 주세요.",
                )}
              </AlertDescription>
            </Alert>
          ) : null}

          <div className="grid gap-2">
            <Label htmlFor="chunk-search-query">Query</Label>
            <Input
              id="chunk-search-query"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="문서 내용과 관련된 질문이나 키워드를 입력하세요"
            />
          </div>

          <div className="grid gap-2 sm:max-w-40">
            <Label htmlFor="chunk-search-top-k">Top K</Label>
            <Input
              id="chunk-search-top-k"
              type="number"
              min={1}
              max={20}
              value={topK}
              onChange={(event) => setTopK(Number(event.target.value))}
            />
          </div>

          <Button
            type="submit"
            size="lg"
            disabled={mutation.isPending || !query.trim()}
          >
            <Search aria-hidden="true" />
            {mutation.isPending ? "검색 중" : "Search"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
