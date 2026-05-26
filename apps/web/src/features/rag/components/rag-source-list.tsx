"use client";

import { FileText } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { RagAnswerSource } from "@/types/rag";

type RagSourceListProps = {
  sources: RagAnswerSource[];
};

export function RagSourceList({ sources }: RagSourceListProps) {
  if (sources.length === 0) {
    return (
      <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
        표시할 출처 chunk가 없습니다.
      </p>
    );
  }

  return (
    <section className="grid gap-3">
      <div className="flex items-center justify-between gap-3">
        <h3 className="text-lg font-semibold">출처</h3>
        <Badge variant="outline">{sources.length}</Badge>
      </div>

      {sources.map((source) => (
        <Card key={source.id} size="sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText aria-hidden="true" />
              <span className="break-all">{source.documentName}</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="grid gap-3">
            <dl className="grid gap-3 text-sm sm:grid-cols-3">
              <div>
                <dt className="text-muted-foreground">Chunk</dt>
                <dd className="mt-1 font-medium">{source.chunkIndex + 1}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Score</dt>
                <dd className="mt-1 font-medium">
                  {source.score == null ? "-" : source.score.toFixed(4)}
                </dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Document ID</dt>
                <dd className="mt-1 break-all font-medium">
                  {source.documentId}
                </dd>
              </div>
            </dl>
            <pre className="max-h-48 overflow-auto whitespace-pre-wrap rounded-md bg-muted p-3 text-sm leading-6">
              {previewContent(source.content)}
            </pre>
          </CardContent>
        </Card>
      ))}
    </section>
  );
}

function previewContent(content: string) {
  if (content.length <= 1_200) {
    return content;
  }

  return `${content.slice(0, 1_200)}...`;
}
