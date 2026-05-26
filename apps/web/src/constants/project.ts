export const PROJECT_NAME = "AssistOps Free";

export const PROJECT_DESCRIPTION =
  "로컬 LLM과 오픈소스 인프라 기반 AI 업무 자동화 플랫폼";

export const statusCards = [
  {
    id: "frontend",
    title: "Frontend",
    status: "Ready",
  },
  {
    id: "backend",
    title: "Backend",
    status: "Auth Ready",
  },
  {
    id: "local-ai",
    title: "Local AI",
    status: "Planned",
  },
  {
    id: "database",
    title: "Database",
    status: "Connected",
  },
  {
    id: "monitoring",
    title: "Monitoring",
    status: "Planned",
  },
] as const;

export const techStackGroups = [
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
] as const;

export const roadmapPhases = [
  "Phase 0: Monorepo & Frontend Foundation",
  "Phase 1: Spring Boot API Foundation",
  "Phase 2: Local Infrastructure with Docker Compose",
  "Phase 3: Auth & RBAC Foundation",
  "Phase 3.5: Frontend Auth Integration",
  "Phase 4: Document Upload & Storage",
  "Phase 5: RAG Pipeline with Ollama and pgvector",
  "Phase 6: Agent Chat UI",
  "Phase 7: Workflow Builder",
  "Phase 8: AI Release Copilot",
  "Phase 9: Monitoring & Observability",
  "Phase 10: Deployment & Portfolio Polish",
] as const;

export const architectureItems = {
  current: [
    "Next.js frontend",
    "Spring Boot API",
    "JWT Auth API",
    "Frontend Auth UI",
    "Dashboard 초기 화면",
    "Workspace 목록 조회",
  ],
  planned: [
    "Refresh token",
    "Workspace switcher",
    "Document upload",
    "RAG pipeline",
    "Ollama integration",
    "Workflow Builder",
    "Redis",
    "MinIO",
    "Nginx",
    "OpenTelemetry",
    "Prometheus",
    "Grafana",
    "Loki",
  ],
} as const;

export type StatusCard = (typeof statusCards)[number];
