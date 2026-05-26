"use client";

import { useQuery, useQueryClient } from "@tanstack/react-query";
import { BriefcaseBusiness, LogOut, UserRound } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { AuthGuard } from "@/features/auth/components/auth-guard";
import { getMe } from "@/features/auth/api/auth-api";
import { getWorkspaces } from "@/features/workspace/api/workspace-api";
import { ApiClientError, getApiErrorMessage } from "@/lib/api/client";
import { useAuthStore } from "@/stores/auth-store";

export default function DashboardPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isInitialized = useAuthStore((state) => state.isInitialized);
  const setUser = useAuthStore((state) => state.setUser);
  const clearUser = useAuthStore((state) => state.clearUser);

  const meQuery = useQuery({
    queryKey: ["auth", "me"],
    queryFn: getMe,
    enabled: isInitialized && isAuthenticated,
    retry: false,
  });

  const workspacesQuery = useQuery({
    queryKey: ["workspaces"],
    queryFn: getWorkspaces,
    enabled: isInitialized && isAuthenticated,
    retry: false,
  });

  useEffect(() => {
    if (meQuery.data) {
      setUser(meQuery.data);
    }
  }, [meQuery.data, setUser]);

  useEffect(() => {
    if (meQuery.error instanceof ApiClientError && meQuery.error.status === 401) {
      clearUser();
      queryClient.clear();
      router.replace("/login");
    }
  }, [clearUser, meQuery.error, queryClient, router]);

  function handleLogout() {
    clearUser();
    queryClient.clear();
    router.replace("/login");
  }

  const currentUser = meQuery.data ?? user;
  const workspaces = workspacesQuery.data ?? [];

  return (
    <>
      <AppHeader />
      <AppShell>
        <AuthGuard>
          <section className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div className="max-w-2xl">
              <Badge variant="outline" className="mb-4">
                Authenticated
              </Badge>
              <h1 className="text-3xl font-semibold sm:text-4xl">Dashboard</h1>
              <p className="mt-3 text-muted-foreground">
                로그인된 사용자와 연결된 workspace 목록을 확인합니다.
              </p>
            </div>
            <Button
              variant="outline"
              onClick={handleLogout}
            >
              <LogOut aria-hidden="true" />
              로그아웃
            </Button>
          </section>

          {meQuery.isError ? (
            <Alert variant="destructive">
              <AlertDescription>
                {getApiErrorMessage(
                  meQuery.error,
                  "사용자 정보를 불러오지 못했습니다.",
                )}
              </AlertDescription>
            </Alert>
          ) : null}

          <section className="grid gap-4 md:grid-cols-[minmax(0,0.8fr)_minmax(0,1.2fr)]">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <UserRound aria-hidden="true" />
                  현재 사용자
                </CardTitle>
              </CardHeader>
              <CardContent>
                {meQuery.isLoading && !currentUser ? (
                  <p className="text-sm text-muted-foreground">
                    사용자 정보를 불러오는 중...
                  </p>
                ) : currentUser ? (
                  <dl className="grid gap-3 text-sm">
                    <div>
                      <dt className="text-muted-foreground">이름</dt>
                      <dd className="mt-1 font-medium">{currentUser.name}</dd>
                    </div>
                    <div>
                      <dt className="text-muted-foreground">이메일</dt>
                      <dd className="mt-1 break-all font-medium">
                        {currentUser.email}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-muted-foreground">역할</dt>
                      <dd className="mt-1">
                        <Badge variant="secondary">{currentUser.role}</Badge>
                      </dd>
                    </div>
                  </dl>
                ) : (
                  <p className="text-sm text-muted-foreground">
                    사용자 정보가 아직 없습니다.
                  </p>
                )}
              </CardContent>
            </Card>

            <div className="grid gap-3">
              <div className="flex items-center justify-between gap-3">
                <h2 className="text-xl font-semibold">Workspaces</h2>
                <Badge variant="outline">{workspaces.length}</Badge>
              </div>

              {workspacesQuery.isError ? (
                <Alert variant="destructive">
                  <AlertDescription>
                    {getApiErrorMessage(
                      workspacesQuery.error,
                      "Workspace 목록을 불러오지 못했습니다.",
                    )}
                  </AlertDescription>
                </Alert>
              ) : null}

              {workspacesQuery.isLoading ? (
                <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
                  Workspace 목록을 불러오는 중...
                </p>
              ) : null}

              {!workspacesQuery.isLoading && workspaces.length === 0 ? (
                <p className="rounded-lg border bg-card px-4 py-3 text-sm text-muted-foreground">
                  표시할 workspace가 없습니다.
                </p>
              ) : null}

              {workspaces.map((workspace) => (
                <Card key={workspace.id} size="sm">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <BriefcaseBusiness aria-hidden="true" />
                      {workspace.name}
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <dl className="grid gap-2 text-sm text-muted-foreground sm:grid-cols-2">
                      <div>
                        <dt>Slug</dt>
                        <dd className="mt-1 font-medium text-foreground">
                          {workspace.slug}
                        </dd>
                      </div>
                      <div>
                        <dt>Created</dt>
                        <dd className="mt-1 font-medium text-foreground">
                          {new Date(workspace.createdAt).toLocaleDateString()}
                        </dd>
                      </div>
                    </dl>
                  </CardContent>
                </Card>
              ))}
            </div>
          </section>
        </AuthGuard>
      </AppShell>
    </>
  );
}
