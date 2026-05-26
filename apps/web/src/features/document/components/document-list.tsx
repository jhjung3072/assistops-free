"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ChevronLeft,
  ChevronRight,
  Download,
  Eye,
  FileText,
  Play,
  RotateCcw,
  Search,
  Sparkles,
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
  deleteDocument,
  downloadDocument,
  type DocumentListParams,
  embedDocument,
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
  const [filters, setFilters] = useState<DocumentListParams>({
    keyword: "",
    status: "",
    embeddingStatus: "",
    page: 0,
    size: 20,
  });
  const [draftFilters, setDraftFilters] =
    useState<DocumentListParams>(filters);

  const documentsQuery = useQuery({
    queryKey: ["documents", filters],
    queryFn: () => getDocuments(filters),
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

  const embedMutation = useMutation({
    mutationFn: embedDocument,
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: ["documents"] });
      queryClient.invalidateQueries({
        queryKey: ["documents", response.document.id, "chunks"],
      });
    },
  });

  const documents = documentsQuery.data?.documents ?? [];
  const page = documentsQuery.data?.page;
  const processingDocumentId = processMutation.isPending
    ? processMutation.variables
    : null;
  const embeddingDocumentId = embedMutation.isPending
    ? embedMutation.variables
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

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFilters({
      ...draftFilters,
      page: 0,
    });
  }

  function handleReset() {
    const nextFilters: DocumentListParams = {
      keyword: "",
      status: "",
      embeddingStatus: "",
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
        <h2 className="text-xl font-semibold">문서 목록</h2>
        <Badge variant="outline">{page?.totalElements ?? documents.length}</Badge>
      </div>

      <form
        onSubmit={handleSearch}
        className="grid gap-3 rounded-lg border bg-card p-4 sm:grid-cols-[minmax(0,1fr)_180px_220px_auto] sm:items-end"
      >
        <div className="grid gap-2">
          <Label htmlFor="document-keyword">Keyword</Label>
          <Input
            id="document-keyword"
            value={draftFilters.keyword ?? ""}
            onChange={(event) =>
              setDraftFilters((currentFilters) => ({
                ...currentFilters,
                keyword: event.target.value,
              }))
            }
            placeholder="파일명 검색"
          />
        </div>
        <div className="grid gap-2">
          <Label htmlFor="document-status">Status</Label>
          <select
            id="document-status"
            className="h-9 rounded-lg border border-input bg-background px-3 text-sm"
            value={draftFilters.status ?? ""}
            onChange={(event) =>
              setDraftFilters((currentFilters) => ({
                ...currentFilters,
                status: event.target.value as DocumentListParams["status"],
              }))
            }
          >
            <option value="">All</option>
            <option value="UPLOADED">UPLOADED</option>
            <option value="PROCESSING">PROCESSING</option>
            <option value="PROCESSED">PROCESSED</option>
            <option value="FAILED">FAILED</option>
          </select>
        </div>
        <div className="grid gap-2">
          <Label htmlFor="document-embedding-status">Embedding</Label>
          <select
            id="document-embedding-status"
            className="h-9 rounded-lg border border-input bg-background px-3 text-sm"
            value={draftFilters.embeddingStatus ?? ""}
            onChange={(event) =>
              setDraftFilters((currentFilters) => ({
                ...currentFilters,
                embeddingStatus: event.target
                  .value as DocumentListParams["embeddingStatus"],
              }))
            }
          >
            <option value="">All</option>
            <option value="NOT_EMBEDDED">NOT_EMBEDDED</option>
            <option value="EMBEDDING">EMBEDDING</option>
            <option value="EMBEDDED">EMBEDDED</option>
            <option value="EMBEDDING_FAILED">EMBEDDING_FAILED</option>
          </select>
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

      {embedMutation.isError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {getApiErrorMessage(
              embedMutation.error,
              "문서 embedding을 생성하지 못했습니다. Ollama 모델이 준비되어 있는지 확인해 주세요.",
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
          조건에 맞는 문서가 없습니다.
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
                <div>
                  <dt className="text-muted-foreground">Embedding</dt>
                  <dd className="mt-1">
                    <Badge
                      variant={embeddingStatusBadgeVariant(
                        document.embeddingStatus,
                      )}
                    >
                      {embeddingDocumentId === document.id
                        ? "EMBEDDING"
                        : document.embeddingStatus}
                    </Badge>
                  </dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">Embedded Chunks</dt>
                  <dd className="mt-1 font-medium">
                    {document.embeddedChunkCount}
                  </dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">Embedding 완료</dt>
                  <dd className="mt-1 font-medium">
                    {document.embeddedAt
                      ? new Date(document.embeddedAt).toLocaleString()
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

              {document.embeddingError ? (
                <Alert variant="destructive">
                  <AlertDescription>{document.embeddingError}</AlertDescription>
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
                    variant="outline"
                    size="sm"
                    onClick={() => embedMutation.mutate(document.id)}
                    disabled={
                      embedMutation.isPending ||
                      embeddingDocumentId === document.id
                    }
                  >
                    <Sparkles aria-hidden="true" />
                    {embeddingDocumentId === document.id
                      ? "Embedding 중"
                      : "Embed"}
                  </Button>
                ) : null}
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
              disabled={!page.hasPrevious || documentsQuery.isFetching}
            >
              <ChevronLeft aria-hidden="true" />
              이전
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(page.page + 1)}
              disabled={!page.hasNext || documentsQuery.isFetching}
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

function embeddingStatusBadgeVariant(
  status: DocumentItem["embeddingStatus"],
): "destructive" | "secondary" | "outline" {
  if (status === "EMBEDDING_FAILED") {
    return "destructive";
  }

  if (status === "EMBEDDED") {
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
