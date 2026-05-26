import { AppHeader } from "@/components/layout/app-header";
import { AppShell } from "@/components/layout/app-shell";
import { Separator } from "@/components/ui/separator";
import { HeroSection } from "@/features/landing/components/hero-section";
import { StatusSection } from "@/features/landing/components/status-section";
import { TechStackSection } from "@/features/landing/components/tech-stack-section";

export default function Home() {
  return (
    <>
      <AppHeader />
      <AppShell>
        <HeroSection />
        <Separator />
        <StatusSection />
        <TechStackSection />
      </AppShell>
    </>
  );
}
