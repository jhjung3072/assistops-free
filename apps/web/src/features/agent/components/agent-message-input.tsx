"use client";

import { Send } from "lucide-react";
import type { FormEvent } from "react";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

type AgentMessageInputProps = {
  disabled: boolean;
  isSending: boolean;
  onSendMessage: (content: string, topK: number) => void;
};

export function AgentMessageInput({
  disabled,
  isSending,
  onSendMessage,
}: AgentMessageInputProps) {
  const [content, setContent] = useState("");
  const [topK, setTopK] = useState(3);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const normalizedContent = content.trim();
    if (!normalizedContent || disabled || isSending) {
      return;
    }

    onSendMessage(normalizedContent, topK);
    setContent("");
  }

  return (
    <form className="grid gap-3 rounded-lg border bg-card p-4" onSubmit={handleSubmit}>
      <div className="grid gap-2">
        <Label htmlFor="agent-message">Message</Label>
        <Textarea
          id="agent-message"
          value={content}
          onChange={(event) => setContent(event.target.value)}
          placeholder="업로드하고 embedding한 문서에 대해 질문해 보세요"
          disabled={disabled || isSending}
        />
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div className="grid gap-2 sm:max-w-36">
          <Label htmlFor="agent-top-k">Top K</Label>
          <Input
            id="agent-top-k"
            type="number"
            min={1}
            max={8}
            value={topK}
            onChange={(event) => setTopK(Number(event.target.value))}
            disabled={disabled || isSending}
          />
        </div>
        <Button
          type="submit"
          size="lg"
          disabled={disabled || isSending || !content.trim()}
        >
          <Send aria-hidden="true" />
          {isSending ? "답변 생성 중" : "전송"}
        </Button>
      </div>
      <p className="text-xs text-muted-foreground">
        topK 기본값은 3입니다. 높일수록 더 많은 문서를 참고하지만 로컬 LLM 응답이 느려질 수 있습니다.
      </p>
    </form>
  );
}
