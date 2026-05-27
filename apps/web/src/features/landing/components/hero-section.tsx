import { FileText, Network, Route, UserPlus } from "lucide-react";
import Link from "next/link";

import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import { PROJECT_DESCRIPTION, PROJECT_NAME } from "@/constants/project";

export function HeroSection() {
  return (
    <section className="max-w-3xl">
      <Badge variant="outline" className="mb-5">
        <Network aria-hidden="true" />
        Local-first knowledge hub
      </Badge>
      <h1 className="text-4xl leading-tight font-semibold sm:text-5xl">
        {PROJECT_NAME}
      </h1>
      <p className="mt-5 max-w-2xl text-lg leading-8 text-muted-foreground">
        {PROJECT_DESCRIPTION}
      </p>
      <div className="mt-7 flex flex-wrap gap-3">
        <Link href="/register" className={buttonVariants({ size: "lg" })}>
          <UserPlus aria-hidden="true" />
          Get Started
        </Link>
        <Link
          href="/roadmap"
          className={buttonVariants({ variant: "outline", size: "lg" })}
        >
          <Route aria-hidden="true" />
          View Roadmap
        </Link>
        <Link
          href="/architecture"
          className={buttonVariants({ variant: "outline", size: "lg" })}
        >
          <FileText aria-hidden="true" />
          View Architecture
        </Link>
      </div>
    </section>
  );
}
