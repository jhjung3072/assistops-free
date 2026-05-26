import {
  Bot,
  ChartNoAxesCombined,
  CheckCircle2,
  Database,
  Server,
  type LucideIcon,
} from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { statusCards, type StatusCard } from "@/constants/project";

const statusIcons: Record<StatusCard["id"], LucideIcon> = {
  frontend: CheckCircle2,
  backend: Server,
  "local-ai": Bot,
  database: Database,
  monitoring: ChartNoAxesCombined,
};

function getBadgeVariant(status: StatusCard["status"]) {
  return status === "Ready" ? "default" : "secondary";
}

export function StatusSection() {
  return (
    <section aria-labelledby="status-heading">
      <h2 id="status-heading" className="mb-4 text-xl font-semibold">
        Current Status
      </h2>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        {statusCards.map((item) => {
          const Icon = statusIcons[item.id];

          return (
            <Card key={item.id}>
              <CardHeader>
                <CardTitle>{item.title}</CardTitle>
                <CardAction>
                  <Icon
                    className="size-4 text-muted-foreground"
                    aria-hidden="true"
                  />
                </CardAction>
              </CardHeader>
              <CardContent>
                <Badge variant={getBadgeVariant(item.status)}>
                  {item.status}
                </Badge>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </section>
  );
}
