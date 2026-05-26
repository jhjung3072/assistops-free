import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { techStackGroups } from "@/constants/project";

export function TechStackSection() {
  return (
    <section aria-labelledby="stack-heading">
      <h2 id="stack-heading" className="mb-4 text-xl font-semibold">
        기술 스택 요약
      </h2>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {techStackGroups.map((group) => (
          <Card key={group.title}>
            <CardHeader>
              <CardTitle>{group.title}</CardTitle>
              <CardDescription>{group.description}</CardDescription>
            </CardHeader>
          </Card>
        ))}
      </div>
    </section>
  );
}
