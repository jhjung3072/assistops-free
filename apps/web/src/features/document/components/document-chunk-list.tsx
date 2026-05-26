"use client";

import { useQuery } from "@tanstack/react-query";
import { Layers } from "lucide-react";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { getDocumentChunks } from "@/features/document/api/document-api";
import { getApiErrorMessage } from "@/lib/api/client";

type DocumentChunkListProps = {
  documentId: string | null;
};

export function DocumentChunkList({ documentId }: DocumentChunkListProps) {
  const chunksQuery = useQuery({
    queryKey: ["documents", documentId, "chunks"],
    queryFn: () => getDocumentChunks(documentId as string),
    enabled: Boolean(documentId),
    retry: false,
  });

  if (!documentId) {
    return (
      <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
        chunk를 확인할 문서를 선택해 주세요.
      </p>
    );
  }

  const chunks = chunksQuery.data?.chunks ?? [];

  return (
    <section className="grid gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">Chunks</h2>
        <Badge variant="outline">{chunks.length}</Badge>
      </div>

      {chunksQuery.isError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(
              chunksQuery.error,
              "chunk 목록을 불러오지 못했습니다.",
            )}
          </AlertDescription>
        </Alert>
      ) : null}

      {chunksQuery.isLoading ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          chunk 목록을 불러오는 중...
        </p>
      ) : null}

      {!chunksQuery.isLoading && chunks.length === 0 ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          표시할 chunk가 없습니다. 문서를 먼저 처리해 주세요.
        </p>
      ) : null}

      {chunks.map((chunk) => (
        <Card key={chunk.id} size="sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Layers aria-hidden="true" />
              Chunk {chunk.chunkIndex + 1}
            </CardTitle>
          </CardHeader>
          <CardContent className="grid gap-3">
            <dl className="grid gap-3 text-sm sm:grid-cols-3">
              <div>
                <dt className="text-muted-foreground">문자 수</dt>
                <dd className="mt-1 font-medium">{chunk.charCount}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">추정 token</dt>
                <dd className="mt-1 font-medium">{chunk.tokenCount ?? "-"}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">생성</dt>
                <dd className="mt-1 font-medium">
                  {new Date(chunk.createdAt).toLocaleString()}
                </dd>
              </div>
            </dl>
            <pre className="max-h-48 overflow-auto whitespace-pre-wrap rounded-md bg-muted p-3 text-sm leading-6">
              {previewContent(chunk.content)}
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
