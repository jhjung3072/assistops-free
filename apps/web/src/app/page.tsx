import {
  Bot,
  ChartNoAxesCombined,
  CheckCircle2,
  Clock3,
  Database,
  Layers3,
  Network,
  Server,
  type LucideIcon,
} from "lucide-react";

import { cn } from "@/lib/utils";

const statusItems: Array<{
  label: string;
  value: string;
  icon: LucideIcon;
  tone: string;
}> = [
  {
    label: "Frontend",
    value: "Ready",
    icon: CheckCircle2,
    tone: "border-emerald-200 bg-emerald-50 text-emerald-700",
  },
  {
    label: "Backend",
    value: "Planned",
    icon: Server,
    tone: "border-sky-200 bg-sky-50 text-sky-700",
  },
  {
    label: "Local AI",
    value: "Planned",
    icon: Bot,
    tone: "border-violet-200 bg-violet-50 text-violet-700",
  },
  {
    label: "Database",
    value: "Planned",
    icon: Database,
    tone: "border-amber-200 bg-amber-50 text-amber-700",
  },
  {
    label: "Monitoring",
    value: "Planned",
    icon: ChartNoAxesCombined,
    tone: "border-rose-200 bg-rose-50 text-rose-700",
  },
];

const stackGroups = [
  {
    title: "Frontend",
    description: "Next.js App Router, React, TypeScript, Tailwind CSS",
  },
  {
    title: "Backend",
    description: "Java 21, Spring Boot, Spring Security, Spring AI",
  },
  {
    title: "AI",
    description: "Ollama, local LLM, embeddings, RAG pipeline",
  },
  {
    title: "Database / Storage",
    description: "PostgreSQL, pgvector, Redis, MinIO",
  },
  {
    title: "Infra",
    description: "Docker Compose, Nginx, GitHub Actions",
  },
  {
    title: "Monitoring",
    description: "OpenTelemetry, Prometheus, Grafana, Loki",
  },
];

export default function Home() {
  return (
    <main className="min-h-screen bg-slate-50 text-slate-950">
      <section className="mx-auto flex w-full max-w-6xl flex-col gap-12 px-6 py-12 sm:px-8 lg:px-10">
        <div className="max-w-3xl">
          <div className="mb-5 flex items-center gap-2 text-sm font-medium text-slate-600">
            <Network className="h-4 w-4" aria-hidden="true" />
            Local-first AI operations
          </div>
          <h1 className="text-4xl font-semibold leading-tight sm:text-5xl">
            AssistOps Free
          </h1>
          <p className="mt-5 max-w-2xl text-lg leading-8 text-slate-700">
            로컬 LLM과 오픈소스 인프라 기반 AI 업무 자동화 플랫폼
          </p>
        </div>

        <section aria-labelledby="status-heading">
          <div className="mb-4 flex items-center gap-2">
            <Clock3 className="h-5 w-5 text-slate-500" aria-hidden="true" />
            <h2 id="status-heading" className="text-xl font-semibold">
              Current Status
            </h2>
          </div>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
            {statusItems.map((item) => {
              const Icon = item.icon;

              return (
                <article
                  key={item.label}
                  className={cn(
                    "rounded-lg border p-5 shadow-sm",
                    item.tone,
                  )}
                >
                  <Icon className="mb-5 h-6 w-6" aria-hidden="true" />
                  <h3 className="text-base font-semibold">{item.label}</h3>
                  <p className="mt-2 text-sm font-medium">{item.value}</p>
                </article>
              );
            })}
          </div>
        </section>

        <section aria-labelledby="stack-heading">
          <div className="mb-4 flex items-center gap-2">
            <Layers3 className="h-5 w-5 text-slate-500" aria-hidden="true" />
            <h2 id="stack-heading" className="text-xl font-semibold">
              기술 스택 요약
            </h2>
          </div>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {stackGroups.map((group) => (
              <article
                key={group.title}
                className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm"
              >
                <h3 className="text-base font-semibold text-slate-950">
                  {group.title}
                </h3>
                <p className="mt-3 text-sm leading-6 text-slate-600">
                  {group.description}
                </p>
              </article>
            ))}
          </div>
        </section>
      </section>
    </main>
  );
}
