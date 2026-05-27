"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { FileSliders } from "lucide-react";
import { useMemo, useState } from "react";

import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { AuthGuard } from "@/features/auth/components/auth-guard";
import {
  activatePromptVersion,
  createPromptTemplate,
  createPromptVersion,
  deletePromptTemplate,
  getPromptTemplates,
  getPromptVersions,
} from "@/features/prompt/api/prompt-api";
import { PromptPreview } from "@/features/prompt/components/prompt-preview";
import { PromptTemplateForm } from "@/features/prompt/components/prompt-template-form";
import { PromptTemplateList } from "@/features/prompt/components/prompt-template-list";
import { PromptVersionForm } from "@/features/prompt/components/prompt-version-form";
import { PromptVersionList } from "@/features/prompt/components/prompt-version-list";
import { getApiErrorMessage } from "@/lib/api/client";
import type {
  PromptTemplate,
  PromptTemplateCreateRequest,
  PromptType,
  PromptVersion,
  PromptVersionCreateRequest,
} from "@/types/prompt";

export default function PromptsPage() {
  const queryClient = useQueryClient();
  const [typeFilter, setTypeFilter] = useState<PromptType | "">("");
  const [selectedPromptId, setSelectedPromptId] = useState<string | null>(null);
  const [selectedVersionId, setSelectedVersionId] = useState<string | null>(
    null,
  );

  const templatesQuery = useQuery({
    queryKey: ["prompts", "templates", typeFilter],
    queryFn: () => getPromptTemplates({ type: typeFilter }),
    retry: false,
  });

  const prompts = useMemo(
    () => templatesQuery.data?.prompts ?? [],
    [templatesQuery.data?.prompts],
  );
  const activePromptId =
    prompts.find((prompt) => prompt.id === selectedPromptId)?.id ??
    prompts[0]?.id ??
    null;
  const selectedPrompt = useMemo(
    () => prompts.find((prompt) => prompt.id === activePromptId) ?? null,
    [activePromptId, prompts],
  );

  const versionsQuery = useQuery({
    queryKey: ["prompts", "versions", activePromptId],
    queryFn: () => getPromptVersions(activePromptId ?? ""),
    enabled: Boolean(activePromptId),
    retry: false,
  });

  const selectedVersion = useMemo(() => {
    const versions = versionsQuery.data?.versions ?? [];

    return (
      versions.find((version) => version.id === selectedVersionId) ??
      versions.find((version) => version.active) ??
      versions[0] ??
      null
    );
  }, [selectedVersionId, versionsQuery.data?.versions]);

  const createTemplateMutation = useMutation({
    mutationFn: createPromptTemplate,
    onSuccess: (prompt) => {
      setSelectedPromptId(prompt.id);
      setSelectedVersionId(prompt.activeVersion?.id ?? null);
      queryClient.invalidateQueries({ queryKey: ["prompts", "templates"] });
    },
  });

  const deleteTemplateMutation = useMutation({
    mutationFn: deletePromptTemplate,
    onSuccess: (_data, promptId) => {
      if (selectedPromptId === promptId) {
        setSelectedPromptId(null);
        setSelectedVersionId(null);
      }
      queryClient.invalidateQueries({ queryKey: ["prompts", "templates"] });
      queryClient.removeQueries({ queryKey: ["prompts", "versions", promptId] });
    },
  });

  const createVersionMutation = useMutation({
    mutationFn: ({
      templateId,
      request,
    }: {
      templateId: string;
      request: PromptVersionCreateRequest;
    }) => createPromptVersion(templateId, request),
    onSuccess: (version) => {
      setSelectedVersionId(version.id);
      queryClient.invalidateQueries({ queryKey: ["prompts", "templates"] });
      queryClient.invalidateQueries({
        queryKey: ["prompts", "versions", version.promptTemplateId],
      });
    },
  });

  const activateVersionMutation = useMutation({
    mutationFn: ({
      templateId,
      version,
    }: {
      templateId: string;
      version: PromptVersion;
    }) => activatePromptVersion(templateId, version.id),
    onSuccess: (version) => {
      setSelectedVersionId(version.id);
      queryClient.invalidateQueries({ queryKey: ["prompts", "templates"] });
      queryClient.invalidateQueries({
        queryKey: ["prompts", "versions", version.promptTemplateId],
      });
    },
  });

  function handleCreateTemplate(request: PromptTemplateCreateRequest) {
    createTemplateMutation.mutate(request);
  }

  function handleSelectPrompt(prompt: PromptTemplate) {
    setSelectedPromptId(prompt.id);
    setSelectedVersionId(prompt.activeVersion?.id ?? null);
  }

  function handleCreateVersion(request: PromptVersionCreateRequest) {
    if (!activePromptId) {
      return;
    }

    createVersionMutation.mutate({
      templateId: activePromptId,
      request,
    });
  }

  function handleActivateVersion(version: PromptVersion) {
    if (!activePromptId) {
      return;
    }

    activateVersionMutation.mutate({
      templateId: activePromptId,
      version,
    });
  }

  const error =
    templatesQuery.error ??
    versionsQuery.error ??
    createTemplateMutation.error ??
    createVersionMutation.error ??
    activateVersionMutation.error ??
    deleteTemplateMutation.error;

  return (
    <>
      <AppHeader />
      <AppShell>
        <AuthGuard>
          <section className="max-w-3xl">
            <Badge variant="outline" className="mb-4">
              Prompt Versioning
            </Badge>
            <h1 className="flex items-center gap-2 text-3xl font-semibold sm:text-4xl">
              <FileSliders aria-hidden="true" />
              Prompts
            </h1>
            <p className="mt-3 text-muted-foreground">
              RAG와 Agent 답변 생성에 사용하는 prompt template과 version을 workspace 단위로 관리합니다.
            </p>
          </section>

          {error ? (
            <Alert variant="destructive">
              <AlertDescription>
                {getApiErrorMessage(error, "Prompt 요청을 처리하지 못했습니다.")}
              </AlertDescription>
            </Alert>
          ) : null}

          <section className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
            <div className="grid content-start gap-6">
              <PromptTemplateList
                prompts={prompts}
                selectedPromptId={activePromptId}
                typeFilter={typeFilter}
                onTypeFilterChange={(type) => {
                  setTypeFilter(type);
                  setSelectedPromptId(null);
                  setSelectedVersionId(null);
                }}
                onSelectPrompt={handleSelectPrompt}
                onDeletePrompt={(promptId) => deleteTemplateMutation.mutate(promptId)}
                isDeleting={deleteTemplateMutation.isPending}
              />
              <PromptTemplateForm
                onCreate={handleCreateTemplate}
                isPending={createTemplateMutation.isPending}
              />
            </div>

            <div className="grid content-start gap-6">
              <div className="grid gap-2">
                <h2 className="text-xl font-semibold">
                  {selectedPrompt?.name ?? "Template을 선택해 주세요"}
                </h2>
                {selectedPrompt ? (
                  <div className="flex flex-wrap gap-2">
                    <Badge variant="secondary">{selectedPrompt.type}</Badge>
                    {selectedPrompt.activeVersion ? (
                      <Badge variant="outline">
                        active v{selectedPrompt.activeVersion.version}
                      </Badge>
                    ) : null}
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">
                    Template을 선택하면 version 목록과 preview를 확인할 수 있습니다.
                  </p>
                )}
              </div>

              {versionsQuery.isLoading ? (
                <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
                  Prompt version을 불러오는 중...
                </p>
              ) : null}

              <section className="grid gap-6 lg:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
                <PromptVersionList
                  versions={versionsQuery.data?.versions ?? []}
                  selectedVersionId={selectedVersion?.id ?? null}
                  onSelectVersion={(version) => setSelectedVersionId(version.id)}
                  onActivateVersion={handleActivateVersion}
                  isActivating={activateVersionMutation.isPending}
                />
                <PromptPreview version={selectedVersion} />
              </section>

              <PromptVersionForm
                disabled={!activePromptId}
                isPending={createVersionMutation.isPending}
                onCreate={handleCreateVersion}
              />
            </div>
          </section>
        </AuthGuard>
      </AppShell>
    </>
  );
}
