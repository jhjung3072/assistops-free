import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

export function AppShell({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <main className="min-h-screen bg-background text-foreground">
      <div
        className={cn(
          "mx-auto flex w-full max-w-6xl flex-col gap-10 px-6 py-10 sm:px-8 lg:px-10",
          className,
        )}
      >
        {children}
      </div>
    </main>
  );
}
