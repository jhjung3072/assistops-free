"use client";

import { GitBranchPlus } from "lucide-react";
import { type FormEvent, useState } from "react";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import type { PromptVersionCreateRequest } from "@/types/prompt";

type PromptVersionFormProps = {
  disabled: boolean;
  isPending: boolean;
  onCreate: (request: PromptVersionCreateRequest) => void;
};

export function PromptVersionForm({
  disabled,
  isPending,
  onCreate,
}: PromptVersionFormProps) {
  const [systemPrompt, setSystemPrompt] = useState("");
  const [userPromptTemplate, setUserPromptTemplate] = useState("");
  const [contextTemplate, setContextTemplate] = useState("");
  const [model, setModel] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!systemPrompt.trim() || !userPromptTemplate.trim()) {
      return;
    }

    onCreate({
      systemPrompt: systemPrompt.trim(),
      userPromptTemplate: userPromptTemplate.trim(),
      contextTemplate: contextTemplate.trim() || undefined,
      model: model.trim() || undefined,
    });
    setSystemPrompt("");
    setUserPromptTemplate("");
    setContextTemplate("");
    setModel("");
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>새 Version</CardTitle>
      </CardHeader>
      <CardContent>
        <form className="grid gap-4" onSubmit={handleSubmit}>
          <div className="grid gap-2">
            <Label htmlFor="version-system">System Prompt</Label>
            <Textarea
              id="version-system"
              value={systemPrompt}
              onChange={(event) => setSystemPrompt(event.target.value)}
              disabled={disabled}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="version-user">User Prompt Template</Label>
            <Textarea
              id="version-user"
              value={userPromptTemplate}
              onChange={(event) => setUserPromptTemplate(event.target.value)}
              placeholder="Context:\n{{context}}\n\nQuestion:\n{{question}}\n\nAnswer:"
              disabled={disabled}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="version-context">Context Template</Label>
            <Textarea
              id="version-context"
              value={contextTemplate}
              onChange={(event) => setContextTemplate(event.target.value)}
              placeholder="[{{index}}] {{documentName}} / chunkIndex={{chunkIndex}}\n{{content}}"
              disabled={disabled}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="version-model">Model</Label>
            <Input
              id="version-model"
              value={model}
              onChange={(event) => setModel(event.target.value)}
              placeholder="비워두면 기본 OLLAMA_CHAT_MODEL 사용"
              disabled={disabled}
            />
          </div>
          <p className="text-xs text-muted-foreground">
            RAG_ANSWER와 AGENT_CHAT prompt는{" "}
            <code>{"{{context}}"}</code>, <code>{"{{question}}"}</code>을 포함해야 합니다.
          </p>
          <Button
            type="submit"
            disabled={disabled || isPending || !systemPrompt.trim() || !userPromptTemplate.trim()}
          >
            <GitBranchPlus aria-hidden="true" />
            Version 생성
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
