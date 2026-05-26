"use client";

import { LogIn, LogOut } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

import { Button, buttonVariants } from "@/components/ui/button";
import { PROJECT_NAME } from "@/constants/project";
import { useAuthStore } from "@/stores/auth-store";

const navItems = [
  {
    label: "Home",
    href: "/",
  },
  {
    label: "Dashboard",
    href: "/dashboard",
  },
  {
    label: "Documents",
    href: "/documents",
  },
  {
    label: "Search",
    href: "/search",
  },
  {
    label: "RAG",
    href: "/rag",
  },
  {
    label: "Agent",
    href: "/agent",
  },
  {
    label: "Roadmap",
    href: "/roadmap",
  },
  {
    label: "Architecture",
    href: "/architecture",
  },
] as const;

export function AppHeader() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isInitialized = useAuthStore((state) => state.isInitialized);
  const initializeAuth = useAuthStore((state) => state.initializeAuth);
  const clearUser = useAuthStore((state) => state.clearUser);

  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  function handleLogout() {
    clearUser();
    router.push("/login");
  }

  return (
    <header className="border-b bg-background">
      <div className="mx-auto flex min-h-14 w-full max-w-6xl flex-col items-start justify-center gap-2 px-6 py-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4 sm:px-8 lg:px-10">
        <Link href="/" className="text-sm font-semibold sm:text-base">
          {PROJECT_NAME}
        </Link>
        <nav
          aria-label="Primary navigation"
          className="flex flex-wrap items-center gap-1"
        >
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
            >
              {item.label}
            </Link>
          ))}
          {isInitialized && isAuthenticated ? (
            <Button variant="ghost" size="sm" onClick={handleLogout}>
              <LogOut aria-hidden="true" />
              Logout
            </Button>
          ) : (
            <Link
              href="/login"
              className={buttonVariants({ variant: "ghost", size: "sm" })}
            >
              <LogIn aria-hidden="true" />
              Login
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
