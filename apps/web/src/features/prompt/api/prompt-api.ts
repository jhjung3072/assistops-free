import { apiRequest } from "@/lib/api/client";
import { buildQueryString } from "@/lib/api/query-string";
import type { ApiResponse } from "@/types/api";
import type {
  PromptTemplate,
  PromptTemplateCreateRequest,
  PromptTemplateListResponse,
  PromptType,
  PromptVersion,
  PromptVersionCreateRequest,
  PromptVersionListResponse,
} from "@/types/prompt";

export type PromptTemplateListParams = {
  type?: PromptType | "";
};

export function createPromptTemplate(request: PromptTemplateCreateRequest) {
  return apiRequest<ApiResponse<PromptTemplate>>("/api/prompts", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getPromptTemplates(params: PromptTemplateListParams = {}) {
  return apiRequest<ApiResponse<PromptTemplateListResponse>>(
    `/api/prompts${buildQueryString(params)}`,
  );
}

export function getPromptTemplate(id: string) {
  return apiRequest<ApiResponse<PromptTemplate>>(`/api/prompts/${id}`);
}

export function getActivePromptTemplate(type: PromptType) {
  return apiRequest<ApiResponse<PromptTemplate>>(
    `/api/prompts/active${buildQueryString({ type })}`,
  );
}

export function deletePromptTemplate(id: string) {
  return apiRequest<void>(`/api/prompts/${id}`, {
    method: "DELETE",
  });
}

export function createPromptVersion(
  templateId: string,
  request: PromptVersionCreateRequest,
) {
  return apiRequest<ApiResponse<PromptVersion>>(
    `/api/prompts/${templateId}/versions`,
    {
      method: "POST",
      body: JSON.stringify(request),
    },
  );
}

export function getPromptVersions(templateId: string) {
  return apiRequest<ApiResponse<PromptVersionListResponse>>(
    `/api/prompts/${templateId}/versions`,
  );
}

export function activatePromptVersion(templateId: string, versionId: string) {
  return apiRequest<ApiResponse<PromptVersion>>(
    `/api/prompts/${templateId}/versions/${versionId}/activate`,
    {
      method: "POST",
    },
  );
}
