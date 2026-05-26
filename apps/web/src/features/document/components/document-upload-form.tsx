"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Upload } from "lucide-react";
import { type ChangeEvent, type FormEvent, useRef, useState } from "react";

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
import { uploadDocument } from "@/features/document/api/document-api";
import { getApiErrorMessage } from "@/lib/api/client";

const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

export function DocumentUploadForm() {
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: uploadDocument,
    onSuccess: (response) => {
      setSuccessMessage(`${response.document.originalFilename} 업로드 완료`);
      setSelectedFile(null);
      setValidationError(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
      queryClient.invalidateQueries({ queryKey: ["documents"] });
    },
  });

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setSuccessMessage(null);

    if (!file) {
      setSelectedFile(null);
      setValidationError(null);
      return;
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
      setSelectedFile(null);
      setValidationError("파일 크기는 10MB 이하여야 합니다.");
      return;
    }

    setSelectedFile(file);
    setValidationError(null);
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSuccessMessage(null);

    if (!selectedFile) {
      setValidationError("업로드할 파일을 선택해 주세요.");
      return;
    }

    mutation.mutate({ file: selectedFile });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>문서 업로드</CardTitle>
      </CardHeader>
      <CardContent>
        <form className="grid gap-4" onSubmit={handleSubmit}>
          <div className="grid gap-2">
            <Label htmlFor="document-file">파일</Label>
            <Input
              ref={fileInputRef}
              id="document-file"
              type="file"
              accept=".pdf,.txt,.md,application/pdf,text/plain,text/markdown"
              onChange={handleFileChange}
            />
            <p className="text-sm text-muted-foreground">
              PDF, TXT, MD 파일을 10MB 이하로 업로드할 수 있습니다.
            </p>
          </div>

          {validationError ? (
            <Alert variant="destructive">
              <AlertDescription>{validationError}</AlertDescription>
            </Alert>
          ) : null}

          {mutation.isError ? (
            <Alert variant="destructive">
              <AlertDescription>
                {getApiErrorMessage(
                  mutation.error,
                  "문서를 업로드하지 못했습니다.",
                )}
              </AlertDescription>
            </Alert>
          ) : null}

          {successMessage ? (
            <Alert>
              <AlertDescription>{successMessage}</AlertDescription>
            </Alert>
          ) : null}

          <Button type="submit" disabled={mutation.isPending}>
            <Upload aria-hidden="true" />
            {mutation.isPending ? "업로드 중" : "업로드"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
