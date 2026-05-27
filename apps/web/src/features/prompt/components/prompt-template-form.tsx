"use client";

import { Plus } from "lucide-react";
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
import type { PromptTemplateCreateRequest, PromptType } from "@/types/prompt";

type PromptTemplateFormProps = {
  onCreate: (request: PromptTemplateCreateRequest) => void;
  isPending: boolean;
};

export function PromptTemplateForm({ onCreate, isPending }: PromptTemplateFormProps) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [type, setType] = useState<PromptType>("RAG_ANSWER");
  const [systemPrompt, setSystemPrompt] = useState("");
  const [userPromptTemplate, setUserPromptTemplate] = useState("");
  const [contextTemplate, setContextTemplate] = useState("");
  const [model, setModel] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!name.trim()) {
      return;
    }

    onCreate({
      name: name.trim(),
      description: description.trim() || undefined,
      type,
      systemPrompt: systemPrompt.trim() || undefined,
      userPromptTemplate: userPromptTemplate.trim() || undefined,
      contextTemplate: contextTemplate.trim() || undefined,
      model: model.trim() || undefined,
    });
    setName("");
    setDescription("");
    setSystemPrompt("");
    setUserPromptTemplate("");
    setContextTemplate("");
    setModel("");
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Template 생성</CardTitle>
      </CardHeader>
      <CardContent>
        <form className="grid gap-4" onSubmit={handleSubmit}>
          <div className="grid gap-2">
            <Label htmlFor="prompt-name">Name</Label>
            <Input
              id="prompt-name"
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="예: Support RAG Prompt"
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="prompt-type">Type</Label>
            <select
              id="prompt-type"
              className="h-9 rounded-lg border border-input bg-background px-3 text-sm"
              value={type}
              onChange={(event) => setType(event.target.value as PromptType)}
            >
              <option value="RAG_ANSWER">RAG_ANSWER</option>
              <option value="AGENT_CHAT">AGENT_CHAT</option>
            </select>
          </div>
          <div className="grid gap-2">
            <Label htmlFor="prompt-description">Description</Label>
            <Input
              id="prompt-description"
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="용도 메모"
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="prompt-system">System Prompt</Label>
            <Textarea
              id="prompt-system"
              value={systemPrompt}
              onChange={(event) => setSystemPrompt(event.target.value)}
              placeholder="비워두면 기본 prompt가 생성됩니다."
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="prompt-user">User Prompt Template</Label>
            <Textarea
              id="prompt-user"
              value={userPromptTemplate}
              onChange={(event) => setUserPromptTemplate(event.target.value)}
              placeholder="Context:\n{{context}}\n\nQuestion:\n{{question}}\n\nAnswer:"
            />
            <p className="text-xs text-muted-foreground">
              <code>{"{{context}}"}</code>, <code>{"{{question}}"}</code>,{" "}
              <code>{"{{language}}"}</code> placeholder를 사용할 수 있습니다.
            </p>
          </div>
          <div className="grid gap-2">
            <Label htmlFor="prompt-context">Context Template</Label>
            <Textarea
              id="prompt-context"
              value={contextTemplate}
              onChange={(event) => setContextTemplate(event.target.value)}
              placeholder="[{{index}}] {{documentName}} / chunkIndex={{chunkIndex}}\n{{content}}"
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="prompt-model">Model</Label>
            <Input
              id="prompt-model"
              value={model}
              onChange={(event) => setModel(event.target.value)}
              placeholder="비워두면 기본 OLLAMA_CHAT_MODEL 사용"
            />
          </div>
          <Button type="submit" disabled={isPending || !name.trim()}>
            <Plus aria-hidden="true" />
            생성
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
