import { apiRequest } from "@/lib/api/client";
import { buildQueryString } from "@/lib/api/query-string";
import type { ApiResponse } from "@/types/api";
import type {
  RagAnswerDetail,
  RagAnswerListResponse,
  RagAnswerRequest,
  RagAnswerResponse,
} from "@/types/rag";

export type RagAnswerListParams = {
  keyword?: string;
  model?: string;
  createdFrom?: string;
  createdTo?: string;
  page?: number;
  size?: number;
};

export function askRagQuestion(request: RagAnswerRequest) {
  return apiRequest<ApiResponse<RagAnswerResponse>>("/api/rag/answer", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getRagAnswers(params: RagAnswerListParams = {}) {
  return apiRequest<ApiResponse<RagAnswerListResponse>>(
    `/api/rag/answers${buildQueryString(params)}`,
  );
}

export function getRagAnswer(id: string) {
  return apiRequest<ApiResponse<RagAnswerDetail>>(`/api/rag/answers/${id}`);
}

export function deleteRagAnswer(id: string) {
  return apiRequest<void>(`/api/rag/answers/${id}`, {
    method: "DELETE",
  });
}
