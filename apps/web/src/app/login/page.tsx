import Link from "next/link";

import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { LoginForm } from "@/features/auth/components/login-form";

export default function LoginPage() {
  return (
    <>
      <AppHeader />
      <AppShell className="items-center">
        <section className="w-full max-w-md">
          <LoginForm />
          <p className="mt-4 text-center text-sm text-muted-foreground">
            아직 계정이 없다면{" "}
            <Link className="font-medium text-foreground underline" href="/register">
              회원가입
            </Link>
          </p>
        </section>
      </AppShell>
    </>
  );
}
