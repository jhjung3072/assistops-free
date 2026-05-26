"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { LogIn } from "lucide-react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { z } from "zod";

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
import { login } from "@/features/auth/api/auth-api";
import { getApiErrorMessage } from "@/lib/api/client";
import { useAuthStore } from "@/stores/auth-store";

const loginSchema = z.object({
  email: z.string().trim().email("올바른 이메일을 입력해 주세요."),
  password: z.string().min(1, "비밀번호를 입력해 주세요."),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginForm() {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (response) => {
      setAuth(response.accessToken, response.user);
      router.push("/dashboard");
    },
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>로그인</CardTitle>
      </CardHeader>
      <CardContent>
        <form
          className="grid gap-4"
          onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
        >
          {mutation.isError ? (
            <Alert variant="destructive">
              <AlertDescription>
                {getApiErrorMessage(
                  mutation.error,
                  "로그인에 실패했습니다. 입력값을 확인해 주세요.",
                )}
              </AlertDescription>
            </Alert>
          ) : null}

          <div className="grid gap-2">
            <Label htmlFor="login-email">이메일</Label>
            <Input
              id="login-email"
              type="email"
              autoComplete="email"
              aria-invalid={Boolean(form.formState.errors.email)}
              {...form.register("email")}
            />
            {form.formState.errors.email ? (
              <p className="text-sm text-destructive">
                {form.formState.errors.email.message}
              </p>
            ) : null}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="login-password">비밀번호</Label>
            <Input
              id="login-password"
              type="password"
              autoComplete="current-password"
              aria-invalid={Boolean(form.formState.errors.password)}
              {...form.register("password")}
            />
            {form.formState.errors.password ? (
              <p className="text-sm text-destructive">
                {form.formState.errors.password.message}
              </p>
            ) : null}
          </div>

          <Button type="submit" size="lg" disabled={mutation.isPending}>
            <LogIn aria-hidden="true" />
            {mutation.isPending ? "로그인 중" : "로그인"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
