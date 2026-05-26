import { apiRequest } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";
import type {
  RagAnswerDetail,
  RagAnswerListResponse,
  RagAnswerRequest,
  RagAnswerResponse,
} from "@/types/rag";

export function askRagQuestion(request: RagAnswerRequest) {
  return apiRequest<ApiResponse<RagAnswerResponse>>("/api/rag/answer", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getRagAnswers() {
  return apiRequest<ApiResponse<RagAnswerListResponse>>("/api/rag/answers");
}

export function getRagAnswer(id: string) {
  return apiRequest<ApiResponse<RagAnswerDetail>>(`/api/rag/answers/${id}`);
}

export function deleteRagAnswer(id: string) {
  return apiRequest<void>(`/api/rag/answers/${id}`, {
    method: "DELETE",
  });
}
