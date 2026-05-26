"use client";

import { useRouter } from "next/navigation";
import { type ReactNode, useEffect } from "react";

import { useAuthStore } from "@/stores/auth-store";

export function AuthGuard({ children }: { children: ReactNode }) {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isInitialized = useAuthStore((state) => state.isInitialized);
  const initializeAuth = useAuthStore((state) => state.initializeAuth);

  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  useEffect(() => {
    if (isInitialized && !isAuthenticated) {
      router.replace("/login");
    }
  }, [isAuthenticated, isInitialized, router]);

  if (!isInitialized) {
    return (
      <div className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
        인증 상태 확인 중...
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
        로그인 화면으로 이동 중...
      </div>
    );
  }

  return children;
}
