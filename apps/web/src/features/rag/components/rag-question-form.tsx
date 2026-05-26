"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Send } from "lucide-react";
import type { FormEvent } from "react";
import { useState } from "react";

import { Alert, AlertDescription } from "@/components/ui/alert";
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
import { askRagQuestion } from "@/features/rag/api/rag-api";
import { getApiErrorMessage } from "@/lib/api/client";
import type { RagAnswerResponse } from "@/types/rag";

type RagQuestionFormProps = {
  onAnswer: (answer: RagAnswerResponse) => void;
};

export function RagQuestionForm({ onAnswer }: RagQuestionFormProps) {
  const queryClient = useQueryClient();
  const [question, setQuestion] = useState("");
  const [topK, setTopK] = useState(3);

  const mutation = useMutation({
    mutationFn: askRagQuestion,
    onSuccess: (answer) => {
      onAnswer(answer);
      queryClient.invalidateQueries({ queryKey: ["rag", "answers"] });
    },
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const normalizedQuestion = question.trim();
    if (!normalizedQuestion) {
      return;
    }

    mutation.mutate({
      question: normalizedQuestion,
      topK,
    });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>문서 기반 질문</CardTitle>
      </CardHeader>
      <CardContent>
        <form className="grid gap-4" onSubmit={handleSubmit}>
          {mutation.isError ? (
            <Alert variant="destructive">
              <AlertDescription>
                {getApiErrorMessage(
                  mutation.error,
                  "답변 생성에 실패했습니다. Ollama chat model이 준비되어 있는지 확인해 주세요.",
                )}
              </AlertDescription>
            </Alert>
          ) : null}

          <div className="grid gap-2">
            <Label htmlFor="rag-question">Question</Label>
            <Textarea
              id="rag-question"
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="업로드한 문서에 대해 질문해 보세요"
            />
          </div>

          <div className="grid gap-2 sm:max-w-40">
            <Label htmlFor="rag-top-k">Top K</Label>
            <Input
              id="rag-top-k"
              type="number"
              min={1}
              max={8}
              value={topK}
              onChange={(event) => setTopK(Number(event.target.value))}
            />
            <p className="text-xs text-muted-foreground">
              topK가 높을수록 더 많은 문서를 참고하지만 응답이 느려질 수 있습니다.
            </p>
          </div>

          <Button
            type="submit"
            size="lg"
            disabled={mutation.isPending || !question.trim()}
          >
            <Send aria-hidden="true" />
            {mutation.isPending ? "답변 생성 중" : "Ask"}
          </Button>

          {mutation.isPending ? (
            <p className="text-sm text-muted-foreground">
              관련 문서를 검색한 뒤 로컬 LLM이 답변을 생성하고 있습니다. 첫 요청은 모델 로딩 때문에 더 오래 걸릴 수 있습니다.
            </p>
          ) : null}
        </form>
      </CardContent>
    </Card>
  );
}
