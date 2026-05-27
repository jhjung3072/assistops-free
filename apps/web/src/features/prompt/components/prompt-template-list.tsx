"use client";

import { FileText, Trash2 } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { PromptTemplate, PromptType } from "@/types/prompt";

type PromptTemplateListProps = {
  prompts: PromptTemplate[];
  selectedPromptId: string | null;
  typeFilter: PromptType | "";
  onTypeFilterChange: (type: PromptType | "") => void;
  onSelectPrompt: (prompt: PromptTemplate) => void;
  onDeletePrompt: (promptId: string) => void;
  isDeleting: boolean;
};

export function PromptTemplateList({
  prompts,
  selectedPromptId,
  typeFilter,
  onTypeFilterChange,
  onSelectPrompt,
  onDeletePrompt,
  isDeleting,
}: PromptTemplateListProps) {
  return (
    <section className="grid content-start gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">Prompt Templates</h2>
        <Badge variant="outline">{prompts.length}</Badge>
      </div>

      <select
        className="h-9 rounded-lg border border-input bg-background px-3 text-sm"
        value={typeFilter}
        onChange={(event) => onTypeFilterChange(event.target.value as PromptType | "")}
      >
        <option value="">All Types</option>
        <option value="RAG_ANSWER">RAG_ANSWER</option>
        <option value="AGENT_CHAT">AGENT_CHAT</option>
      </select>

      {prompts.length === 0 ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          아직 등록된 prompt template이 없습니다.
        </p>
      ) : null}

      <div className="grid gap-3">
        {prompts.map((prompt) => (
          <Card
            key={prompt.id}
            size="sm"
            className={selectedPromptId === prompt.id ? "border-primary" : undefined}
          >
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText aria-hidden="true" />
                <button
                  type="button"
                  className="min-w-0 flex-1 truncate text-left"
                  onClick={() => onSelectPrompt(prompt)}
                >
                  {prompt.name}
                </button>
              </CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3">
              <div className="flex flex-wrap gap-2">
                <Badge variant="secondary">{prompt.type}</Badge>
                {prompt.activeVersion ? (
                  <Badge variant="outline">v{prompt.activeVersion.version} active</Badge>
                ) : null}
              </div>
              {prompt.description ? (
                <p className="line-clamp-2 text-sm text-muted-foreground">
                  {prompt.description}
                </p>
              ) : null}
              <Button
                variant="outline"
                size="sm"
                onClick={() => onDeletePrompt(prompt.id)}
                disabled={isDeleting}
              >
                <Trash2 aria-hidden="true" />
                삭제
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
