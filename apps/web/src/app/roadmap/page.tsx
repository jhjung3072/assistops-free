import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const roadmapPhases = [
  "Phase 0: Monorepo & Frontend Foundation",
  "Phase 1: Spring Boot API Foundation",
  "Phase 2: Local Infrastructure with Docker Compose",
  "Phase 3: Auth & RBAC",
  "Phase 4: Document Upload & Storage",
  "Phase 5: RAG Pipeline with Ollama and pgvector",
  "Phase 6: Agent Chat UI",
  "Phase 7: Workflow Builder",
  "Phase 8: AI Release Copilot",
  "Phase 9: Monitoring & Observability",
  "Phase 10: Deployment & Portfolio Polish",
] as const;

export default function RoadmapPage() {
  return (
    <>
      <AppHeader />
      <AppShell>
        <section className="max-w-3xl">
          <Badge variant="outline" className="mb-5">
            Current Phase: Phase 0
          </Badge>
          <h1 className="text-3xl font-semibold sm:text-4xl">개발 로드맵</h1>
          <p className="mt-4 text-muted-foreground">
            현재 단계는 Phase 0: Monorepo & Frontend Foundation입니다. 이후
            단계는 아직 구현 예정입니다.
          </p>
        </section>

        <section aria-label="Roadmap phases" className="grid gap-3">
          {roadmapPhases.map((phase, index) => (
            <Card key={phase}>
              <CardHeader>
                <CardTitle className="flex flex-wrap items-center gap-2">
                  {phase}
                  {index === 0 ? <Badge>현재 단계</Badge> : null}
                </CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {index === 0
                  ? "프론트엔드 앱 기반과 문서화 구조를 정리하는 단계입니다."
                  : "향후 구현 예정 단계입니다."}
              </CardContent>
            </Card>
          ))}
        </section>
      </AppShell>
    </>
  );
}
