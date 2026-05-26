"use client";

import { FileText } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import type { AgentChatMessageSource } from "@/types/agent";

type AgentSourceListProps = {
  sources: AgentChatMessageSource[];
};

export function AgentSourceList({ sources }: AgentSourceListProps) {
  if (sources.length === 0) {
    return null;
  }

  return (
    <div className="grid gap-2">
      <div className="flex items-center gap-2 text-sm font-medium">
        <FileText aria-hidden="true" className="size-4" />
        출처
        <Badge variant="outline">{sources.length}</Badge>
      </div>
      <div className="grid gap-2">
        {sources.map((source) => (
          <details
            key={source.id}
            className="rounded-md border bg-background px-3 py-2 text-sm"
          >
            <summary className="cursor-pointer break-all font-medium">
              {source.documentName} · chunk {source.chunkIndex + 1}
              {source.score == null ? "" : ` · score ${source.score.toFixed(4)}`}
            </summary>
            <pre className="mt-2 max-h-40 overflow-auto whitespace-pre-wrap rounded-md bg-muted p-3 leading-6 text-muted-foreground">
              {previewContent(source.content)}
            </pre>
          </details>
        ))}
      </div>
    </div>
  );
}

function previewContent(content: string) {
  if (content.length <= 1_000) {
    return content;
  }

  return `${content.slice(0, 1_000)}...`;
}
