import { apiRequest } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";
import type {
  ChunkSearchRequest,
  ChunkSearchResponse,
} from "@/types/search";

export function searchChunks(request: ChunkSearchRequest) {
  return apiRequest<ApiResponse<ChunkSearchResponse>>("/api/search/chunks", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
