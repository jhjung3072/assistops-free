"use client";

import { useState } from "react";

import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import { AuthGuard } from "@/features/auth/components/auth-guard";
import { DocumentChunkList } from "@/features/document/components/document-chunk-list";
import { DocumentList } from "@/features/document/components/document-list";
import { DocumentUploadForm } from "@/features/document/components/document-upload-form";

export default function DocumentsPage() {
  const [selectedDocumentId, setSelectedDocumentId] = useState<string | null>(
    null,
  );

  return (
    <>
      <AppHeader />
      <AppShell>
        <AuthGuard>
          <section className="max-w-3xl">
            <Badge variant="outline" className="mb-4">
              Storage
            </Badge>
            <h1 className="text-3xl font-semibold sm:text-4xl">Documents</h1>
            <p className="mt-3 text-muted-foreground">
              원본 문서를 업로드하고 저장된 문서 메타데이터를 확인합니다.
            </p>
          </section>

          <DocumentUploadForm />
          <DocumentList
            selectedDocumentId={selectedDocumentId}
            onSelectDocument={setSelectedDocumentId}
          />
          <DocumentChunkList documentId={selectedDocumentId} />
        </AuthGuard>
      </AppShell>
    </>
  );
}
