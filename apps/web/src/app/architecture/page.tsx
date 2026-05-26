import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const currentItems = ["Next.js frontend"] as const;

const plannedItems = [
  "Spring Boot API",
  "PostgreSQL",
  "pgvector",
  "Redis",
  "MinIO",
  "Ollama",
  "Docker Compose",
  "Monitoring",
] as const;

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
            현재는 프론트엔드 기반만 구현되어 있으며, 백엔드와 로컬 인프라,
            관측성 구성은 향후 단계에서 추가할 예정입니다.
          </p>
        </section>

        <div className="grid gap-4 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>현재 구현</CardTitle>
            </CardHeader>
            <CardContent>
              <ul className="grid gap-2 text-sm text-muted-foreground">
                {currentItems.map((item) => (
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
                {plannedItems.map((item) => (
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
