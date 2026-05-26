import { apiRequest } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from "@/types/auth";

export function register(request: RegisterRequest) {
  return apiRequest<ApiResponse<AuthResponse>>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function login(request: LoginRequest) {
  return apiRequest<ApiResponse<AuthResponse>>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getMe() {
  return apiRequest<ApiResponse<User>>("/api/auth/me");
}
