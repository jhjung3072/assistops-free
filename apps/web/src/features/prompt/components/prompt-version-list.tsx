"use client";

import { CheckCircle2, GitBranch, PlayCircle } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { PromptVersion } from "@/types/prompt";

type PromptVersionListProps = {
  versions: PromptVersion[];
  selectedVersionId: string | null;
  onSelectVersion: (version: PromptVersion) => void;
  onActivateVersion: (version: PromptVersion) => void;
  isActivating: boolean;
};

export function PromptVersionList({
  versions,
  selectedVersionId,
  onSelectVersion,
  onActivateVersion,
  isActivating,
}: PromptVersionListProps) {
  return (
    <section className="grid gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">Versions</h2>
        <Badge variant="outline">{versions.length}</Badge>
      </div>

      {versions.length === 0 ? (
        <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
          아직 생성된 prompt version이 없습니다.
        </p>
      ) : null}

      <div className="grid gap-3">
        {versions.map((version) => (
          <Card
            key={version.id}
            size="sm"
            className={
              selectedVersionId === version.id ? "border-primary" : undefined
            }
          >
            <CardHeader>
              <CardTitle className="flex flex-wrap items-center gap-2">
                <GitBranch aria-hidden="true" />
                <button
                  type="button"
                  className="min-w-0 flex-1 text-left"
                  onClick={() => onSelectVersion(version)}
                >
                  Version {version.version}
                </button>
                {version.active ? (
                  <Badge variant="secondary">
                    <CheckCircle2 aria-hidden="true" />
                    active
                  </Badge>
                ) : null}
              </CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3">
              <p className="line-clamp-3 text-sm text-muted-foreground">
                {version.userPromptTemplate}
              </p>
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex flex-wrap gap-2">
                  <Badge variant="outline">
                    {new Date(version.createdAt).toLocaleDateString()}
                  </Badge>
                  {version.model ? (
                    <Badge variant="outline">{version.model}</Badge>
                  ) : null}
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onActivateVersion(version)}
                  disabled={version.active || isActivating}
                >
                  <PlayCircle aria-hidden="true" />
                  Activate
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
