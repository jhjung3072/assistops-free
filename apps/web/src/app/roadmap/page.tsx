import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { roadmapPhases } from "@/constants/project";

export default function RoadmapPage() {
  return (
    <>
      <AppHeader />
      <AppShell>
        <section className="max-w-3xl">
          <Badge variant="outline" className="mb-5">
            Current Phase: Rebranding
          </Badge>
          <h1 className="text-3xl font-semibold sm:text-4xl">개발 로드맵</h1>
          <p className="mt-4 text-muted-foreground">
            현재는 기존 자동화 중심 방향을 지식 관리 플랫폼인 AI Knowledge Hub로
            정리하는 단계입니다. Notes, Links, Collections는 후속 단계에서 다룹니다.
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
                  ? "제품명, 문서, 화면 문구를 AI Knowledge Hub 방향으로 정리합니다."
                  : index === 1
                    ? "현재 구현된 문서 업로드, 검색, RAG, Agent, Prompt 기반을 Knowledge Library로 다듬는 단계입니다."
                    : "향후 구현 예정 단계입니다."}
              </CardContent>
            </Card>
          ))}
        </section>
      </AppShell>
    </>
  );
}
