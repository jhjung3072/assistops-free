import Link from "next/link";

import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { RegisterForm } from "@/features/auth/components/register-form";

export default function RegisterPage() {
  return (
    <>
      <AppHeader />
      <AppShell className="items-center">
        <section className="w-full max-w-md">
          <RegisterForm />
          <p className="mt-4 text-center text-sm text-muted-foreground">
            이미 계정이 있다면{" "}
            <Link className="font-medium text-foreground underline" href="/login">
              로그인
            </Link>
          </p>
        </section>
      </AppShell>
    </>
  );
}
