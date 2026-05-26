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
            Current Phase: Frontend Auth Integration
          </Badge>
          <h1 className="text-3xl font-semibold sm:text-4xl">개발 로드맵</h1>
          <p className="mt-4 text-muted-foreground">
            현재 단계는 백엔드 JWT 인증 API를 프론트엔드 화면과 연결하는
            단계입니다. refresh token과 세부 RBAC UI는 후속 단계에서 다룹니다.
          </p>
        </section>

        <section aria-label="Roadmap phases" className="grid gap-3">
          {roadmapPhases.map((phase, index) => (
            <Card key={phase}>
              <CardHeader>
                <CardTitle className="flex flex-wrap items-center gap-2">
                  {phase}
                  {index === 4 ? <Badge>현재 단계</Badge> : null}
                </CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {index < 4
                  ? "기본 기반을 구성한 단계입니다."
                  : index === 4
                    ? "로그인, 회원가입, 인증 보호 dashboard를 연결하는 단계입니다."
                  : "향후 구현 예정 단계입니다."}
              </CardContent>
            </Card>
          ))}
        </section>
      </AppShell>
    </>
  );
}
