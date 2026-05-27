"use client";

import { Eye } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { PromptVersion } from "@/types/prompt";

type PromptPreviewProps = {
  version: PromptVersion | null;
};

export function PromptPreview({ version }: PromptPreviewProps) {
  if (!version) {
    return (
      <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
        Version을 선택하면 prompt preview가 표시됩니다.
      </p>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex flex-wrap items-center gap-2">
          <Eye aria-hidden="true" />
          Prompt Preview
          <Badge variant="outline">v{version.version}</Badge>
          {version.active ? <Badge variant="secondary">active</Badge> : null}
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4">
        <PreviewBlock title="System Prompt" value={version.systemPrompt} />
        <PreviewBlock
          title="User Prompt Template"
          value={version.userPromptTemplate}
        />
        <PreviewBlock
          title="Context Template"
          value={version.contextTemplate ?? "기본 context template 사용"}
        />
        <div className="flex flex-wrap gap-2 text-sm">
          <Badge variant="outline">
            model {version.model ?? "default OLLAMA_CHAT_MODEL"}
          </Badge>
          <Badge variant="outline">
            created {new Date(version.createdAt).toLocaleString()}
          </Badge>
        </div>
      </CardContent>
    </Card>
  );
}

function PreviewBlock({ title, value }: { title: string; value: string }) {
  return (
    <div className="grid gap-2">
      <h3 className="text-sm font-medium">{title}</h3>
      <pre className="max-h-64 overflow-auto whitespace-pre-wrap rounded-md border bg-muted p-3 text-xs leading-5 text-muted-foreground">
        {value}
      </pre>
    </div>
  );
}
