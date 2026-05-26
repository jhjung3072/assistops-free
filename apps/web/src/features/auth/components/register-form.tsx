"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { UserPlus } from "lucide-react";
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
import { register } from "@/features/auth/api/auth-api";
import { getApiErrorMessage } from "@/lib/api/client";
import { useAuthStore } from "@/stores/auth-store";

const registerSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, "이름을 입력해 주세요.")
    .max(120, "이름은 120자 이하여야 합니다."),
  email: z.string().trim().email("올바른 이메일을 입력해 주세요."),
  password: z
    .string()
    .min(8, "비밀번호는 8자 이상이어야 합니다.")
    .max(128, "비밀번호는 128자 이하여야 합니다."),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export function RegisterForm() {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);

  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: "",
      email: "",
      password: "",
    },
  });

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (response) => {
      setAuth(response.accessToken, response.user);
      router.push("/dashboard");
    },
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>회원가입</CardTitle>
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
                  "회원가입에 실패했습니다. 입력값을 확인해 주세요.",
                )}
              </AlertDescription>
            </Alert>
          ) : null}

          <div className="grid gap-2">
            <Label htmlFor="register-name">이름</Label>
            <Input
              id="register-name"
              type="text"
              autoComplete="name"
              aria-invalid={Boolean(form.formState.errors.name)}
              {...form.register("name")}
            />
            {form.formState.errors.name ? (
              <p className="text-sm text-destructive">
                {form.formState.errors.name.message}
              </p>
            ) : null}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="register-email">이메일</Label>
            <Input
              id="register-email"
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
            <Label htmlFor="register-password">비밀번호</Label>
            <Input
              id="register-password"
              type="password"
              autoComplete="new-password"
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
            <UserPlus aria-hidden="true" />
            {mutation.isPending ? "가입 중" : "회원가입"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
