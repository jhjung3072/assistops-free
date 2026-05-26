"use client";

import { FileSearch } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { ChunkSearchResponse } from "@/types/search";

type ChunkSearchResultsProps = {
  response: ChunkSearchResponse | null;
};

export function ChunkSearchResults({ response }: ChunkSearchResultsProps) {
  if (!response) {
    return (
      <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
        검색어를 입력하면 embedding vector와 가까운 chunk를 표시합니다.
      </p>
    );
  }

  if (response.results.length === 0) {
    return (
      <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
        검색 결과가 없습니다. 문서를 처리하고 embedding을 먼저 생성해 주세요.
      </p>
    );
  }

  return (
    <section className="grid gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">검색 결과</h2>
        <Badge variant="outline">{response.results.length}</Badge>
      </div>

      {response.results.map((result) => (
        <Card key={result.chunkId} size="sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileSearch aria-hidden="true" />
              <span className="break-all">{result.documentName}</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="grid gap-3">
            <dl className="grid gap-3 text-sm sm:grid-cols-4">
              <div>
                <dt className="text-muted-foreground">Chunk</dt>
                <dd className="mt-1 font-medium">{result.chunkIndex + 1}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Score</dt>
                <dd className="mt-1 font-medium">
                  {result.score.toFixed(4)}
                </dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Distance</dt>
                <dd className="mt-1 font-medium">
                  {result.distance.toFixed(4)}
                </dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Model</dt>
                <dd className="mt-1 break-all font-medium">
                  {result.embeddingModel ?? "-"}
                </dd>
              </div>
            </dl>
            <pre className="max-h-56 overflow-auto whitespace-pre-wrap rounded-md bg-muted p-3 text-sm leading-6">
              {previewContent(result.content)}
            </pre>
          </CardContent>
        </Card>
      ))}
    </section>
  );
}

function previewContent(content: string) {
  if (content.length <= 1_500) {
    return content;
  }

  return `${content.slice(0, 1_500)}...`;
}
