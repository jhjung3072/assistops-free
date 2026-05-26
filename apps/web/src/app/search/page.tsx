"use client";

import { useState } from "react";

import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import { AuthGuard } from "@/features/auth/components/auth-guard";
import { ChunkSearchForm } from "@/features/search/components/chunk-search-form";
import { ChunkSearchResults } from "@/features/search/components/chunk-search-results";
import type { ChunkSearchResponse } from "@/types/search";

export default function SearchPage() {
  const [searchResponse, setSearchResponse] =
    useState<ChunkSearchResponse | null>(null);

  return (
    <>
      <AppHeader />
      <AppShell>
        <AuthGuard>
          <section className="max-w-3xl">
            <Badge variant="outline" className="mb-4">
              Vector Search
            </Badge>
            <h1 className="text-3xl font-semibold sm:text-4xl">Search</h1>
            <p className="mt-3 text-muted-foreground">
              embedding이 생성된 문서 chunk를 query와 가까운 순서로 검색합니다.
            </p>
          </section>

          <ChunkSearchForm onSearchComplete={setSearchResponse} />
          <ChunkSearchResults response={searchResponse} />
        </AuthGuard>
      </AppShell>
    </>
  );
}
