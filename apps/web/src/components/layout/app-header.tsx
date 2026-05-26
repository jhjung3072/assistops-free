import Link from "next/link";

import { PROJECT_NAME } from "@/constants/project";

const navItems = [
  {
    label: "Home",
    href: "/",
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
        </nav>
      </div>
    </header>
  );
}
