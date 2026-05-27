export const PROJECT_NAME = "AI Knowledge Hub";

export const PROJECT_DESCRIPTION =
  "AI Knowledge Hub는 문서, 메모, 링크, 대화를 한곳에 모으고 로컬 LLM과 RAG를 통해 검색·요약·질의응답을 제공하는 지식 관리 플랫폼입니다.";

export const statusCards = [
  {
    id: "frontend",
    title: "Knowledge Library",
    status: "Ready",
  },
  {
    id: "backend",
    title: "Document Upload",
    status: "Ready",
  },
  {
    id: "local-ai",
    title: "Semantic Search",
    status: "Ready",
  },
  {
    id: "database",
    title: "RAG Q&A",
    status: "Ready",
  },
  {
    id: "monitoring",
    title: "Agent Chat",
    status: "Ready",
  },
  {
    id: "prompts",
    title: "Prompt Versioning",
    status: "Ready",
  },
] as const;

export const techStackGroups = [
  {
    title: "Frontend",
    description: "Next.js App Router, React, TypeScript, Tailwind CSS, shadcn/ui",
  },
  {
    title: "Backend",
    description: "Java 21, Spring Boot, Spring Security, Spring AI",
  },
  {
    title: "AI",
    description: "Ollama, local embeddings, semantic search, RAG Q&A, Agent Chat",
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
  "Phase 0: Rebranding to AI Knowledge Hub",
  "Phase 1: Knowledge Library Foundation",
  "Phase 2: Notes & Links",
  "Phase 3: Collections & Tags",
  "Phase 4: Summary Generation",
  "Phase 5: Sharing & Permissions",
  "Phase 6: Monitoring & Deployment",
] as const;

export const architectureItems = {
  current: [
    "Next.js frontend",
    "Spring Boot API",
    "JWT Auth API",
    "Frontend Auth UI",
    "Document Upload",
    "MinIO original file storage",
    "Document parsing and chunking",
    "Ollama embedding generation",
    "pgvector semantic search",
    "RAG Q&A",
    "Agent Chat",
    "Prompt Versioning",
    "Querydsl dynamic filtering",
  ],
  planned: [
    "Notes",
    "Links",
    "Collections",
    "Knowledge tags",
    "Summary generation",
    "Sharing and permissions 고도화",
    "Refresh token 또는 HttpOnly Cookie auth",
    "Workspace switcher",
    "Monitoring",
    "Production deployment",
  ],
} as const;

export type StatusCard = (typeof statusCards)[number];
