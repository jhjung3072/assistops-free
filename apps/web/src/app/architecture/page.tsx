import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { architectureItems } from "@/constants/project";

export default function ArchitecturePage() {
  return (
    <>
      <AppHeader />
      <AppShell>
        <section className="max-w-3xl">
          <Badge variant="outline" className="mb-5">
            Target Architecture
          </Badge>
          <h1 className="text-3xl font-semibold sm:text-4xl">목표 아키텍처</h1>
          <p className="mt-4 text-muted-foreground">
            현재는 Next.js 프론트엔드, Spring Boot API, JWT 인증, dashboard
            초기 화면, workspace 조회 흐름까지 연결되어 있습니다.
          </p>
        </section>

        <div className="grid gap-4 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>현재 구현</CardTitle>
            </CardHeader>
            <CardContent>
              <ul className="grid gap-2 text-sm text-muted-foreground">
                {architectureItems.current.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>향후 구현 예정</CardTitle>
            </CardHeader>
            <CardContent>
              <ul className="grid gap-2 text-sm text-muted-foreground">
                {architectureItems.planned.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </CardContent>
          </Card>
        </div>
      </AppShell>
    </>
  );
}
