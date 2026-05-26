import { apiRequest } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";
import type { Workspace } from "@/types/workspace";

export function getWorkspaces() {
  return apiRequest<ApiResponse<Workspace[]>>("/api/workspaces");
}
