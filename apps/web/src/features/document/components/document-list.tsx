"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Download, Eye, FileText, Play, Trash2 } from "lucide-react";
import { useState } from "react";

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
  deleteDocument,
  downloadDocument,
  getDocuments,
  processDocument,
} from "@/features/document/api/document-api";
import { getApiErrorMessage } from "@/lib/api/client";
import type { Document as DocumentItem } from "@/types/document";

type DocumentListProps = {
  selectedDocumentId: string | null;
  onSelectDocument: (documentId: string) => void;
};

export function DocumentList({
  selectedDocumentId,
  onSelectDocument,
}: DocumentListProps) {
  const queryClient = useQueryClient();
  const [downloadError, setDownloadError] = useState<unknown>(null);

  const documentsQuery = useQuery({
    queryKey: ["documents"],
    queryFn: getDocuments,
    retry: false,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteDocument,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["documents"] });
    },
  });

  const processMutation = useMutation({
    mutationFn: processDocument,
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: ["documents"] });
      queryClient.invalidateQueries({
        queryKey: ["documents", response.document.id, "chunks"],
      });
      onSelectDocument(response.document.id);
    },
  });

  const documents = documentsQuery.data?.documents ?? [];
  const processingDocumentId = processMutation.isPending
    ? processMutation.variables
    : null;

  async function handleDownload(document: DocumentItem) {
    setDownloadError(null);

    try {
      const response = await downloadDocument(document.id);
      const url = window.URL.createObjectURL(response.blob);
      const link = window.document.createElement("a");
      link.href = url;
      link.download = response.filename ?? document.originalFilename;
      window.document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      setDownloadError(error);
    }
  }

  return (
    <section className="grid gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">문서 목록</h2>
        <Badge variant="outline">{documents.length}</Badge>
      </div>

      {documentsQuery.isError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(
              documentsQuery.error,
              "문서 목록을 불러오지 못했습니다.",
            )}
          </AlertDescription>
        </Alert>
      ) : null}

      {deleteMutation.isError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(
              deleteMutation.error,
              "문서를 삭제하지 못했습니다.",
            )}
          </AlertDescription>
        </Alert>
      ) : null}

      {processMutation.isError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(
              processMutation.error,
              "문서를 처리하지 못했습니다.",
            )}
          </AlertDescription>
        </Alert>
      ) : null}

      {downloadError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(downloadError, "문서를 다운로드하지 못했습니다.")}
          </AlertDescription>
        </Alert>
      ) : null}

      {documentsQuery.isLoading ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          문서 목록을 불러오는 중...
        </p>
      ) : null}

      {!documentsQuery.isLoading && documents.length === 0 ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          아직 업로드된 문서가 없습니다.
        </p>
      ) : null}

      {documents.map((document) => (
        <Card key={document.id} size="sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText aria-hidden="true" />
              <span className="break-all">{document.originalFilename}</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
              <dl className="grid gap-3 text-sm sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <dt className="text-muted-foreground">크기</dt>
                  <dd className="mt-1 font-medium">
                    {formatBytes(document.sizeBytes)}
                  </dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">Content Type</dt>
                  <dd className="mt-1 break-all font-medium">
                    {document.contentType ?? "-"}
                  </dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">상태</dt>
                  <dd className="mt-1">
                    <Badge variant={statusBadgeVariant(document.status)}>
                      {processingDocumentId === document.id
                        ? "PROCESSING"
                        : document.status}
                    </Badge>
                  </dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">업로드</dt>
                  <dd className="mt-1 font-medium">
                    {new Date(document.createdAt).toLocaleString()}
                  </dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">Chunks</dt>
                  <dd className="mt-1 font-medium">{document.chunkCount}</dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">처리 완료</dt>
                  <dd className="mt-1 font-medium">
                    {document.processedAt
                      ? new Date(document.processedAt).toLocaleString()
                      : "-"}
                  </dd>
                </div>
              </dl>

              {document.processingError ? (
                <Alert variant="destructive">
                  <AlertDescription>
                    {document.processingError}
                  </AlertDescription>
                </Alert>
              ) : null}

              <div className="flex flex-wrap gap-2 lg:justify-end">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => processMutation.mutate(document.id)}
                  disabled={
                    processMutation.isPending ||
                    document.status === "DELETED" ||
                    processingDocumentId === document.id
                  }
                >
                  <Play aria-hidden="true" />
                  {processingDocumentId === document.id ? "처리 중" : "Process"}
                </Button>
                {document.status === "PROCESSED" ? (
                  <Button
                    variant={
                      selectedDocumentId === document.id
                        ? "secondary"
                        : "outline"
                    }
                    size="sm"
                    onClick={() => onSelectDocument(document.id)}
                  >
                    <Eye aria-hidden="true" />
                    View Chunks
                  </Button>
                ) : null}
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleDownload(document)}
                >
                  <Download aria-hidden="true" />
                  다운로드
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => deleteMutation.mutate(document.id)}
                  disabled={deleteMutation.isPending}
                >
                  <Trash2 aria-hidden="true" />
                  삭제
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </section>
  );
}

function statusBadgeVariant(
  status: DocumentItem["status"],
): "destructive" | "secondary" | "outline" {
  if (status === "FAILED") {
    return "destructive";
  }

  if (status === "PROCESSED") {
    return "secondary";
  }

  return "outline";
}

function formatBytes(bytes: number) {
  if (bytes < 1024) {
    return `${bytes} B`;
  }

  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }

  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
