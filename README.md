# AssistOps Free

`AssistOps Free`는 유료 AI API나 관리형 클라우드 서비스에 의존하지 않고, 로컬 LLM과 오픈소스 인프라만으로 동작하는 AI 업무 자동화 플랫폼을 목표로 하는 포트폴리오 프로젝트입니다.

현재 단계는 **repo 기반 정리와 Next.js 프론트엔드 초기 세팅**입니다. Spring Boot 백엔드, 로컬 AI, 데이터베이스, 인프라, 관측성 구성은 아직 구현하지 않았으며 향후 단계에서 추가할 예정입니다.

## 프로젝트 목표

- Next.js App Router 기반 프론트엔드 구축
- Spring Boot 기반 백엔드 API 구축
- Ollama 기반 로컬 LLM 연동
- PostgreSQL + pgvector 기반 RAG 파이프라인 구현
- Redis, MinIO를 활용한 캐시 및 파일 저장소 구성
- Docker Compose 기반 로컬 실행 환경 구성
- GitHub Actions 기반 CI 구성
- OpenTelemetry, Prometheus, Grafana, Loki 기반 관측성 구성
- 문서와 README는 한국어로 작성
- 코드, 폴더명, 파일명, 변수명, API endpoint, 설정 키는 영어로 유지

## 전체 기술 스택

| 영역               | 기술                                                                                                                                            | 현재 상태 |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------- | --------- |
| Frontend           | Next.js App Router, React, TypeScript, Tailwind CSS                                                                                             | 사용 중   |
| Frontend           | shadcn/ui, Radix UI, TanStack Query, Zustand, React Hook Form, Zod, React Flow, Recharts, Playwright                                            | 예정      |
| Backend            | Java 21, Spring Boot, Spring Security, Spring AI, Spring Data JPA, Querydsl, Springdoc OpenAPI                                                  | 예정      |
| AI                 | Ollama, qwen2.5-coder 또는 llama3.2 계열 로컬 모델, local embedding model, RAG pipeline, prompt versioning, tool calling style internal actions | 예정      |
| Database / Storage | PostgreSQL, pgvector, Redis, MinIO                                                                                                              | 예정      |
| Infra              | Docker Engine, Docker Compose, Nginx, GitHub Actions, Oracle Cloud Always Free 또는 local server                                                | 예정      |
| Monitoring         | OpenTelemetry, Prometheus, Grafana OSS, Loki                                                                                                    | 예정      |

## 현재 구현 상태

- `apps/web`: Next.js App Router 기반 프론트엔드 프로젝트 생성 완료
- 루트 `pnpm-workspace.yaml`: `apps/web` workspace 등록
- 루트 `package.json`: 프론트엔드 실행, 빌드, 린트 스크립트 추가
- 프론트엔드 주요 라이브러리: 의존성 설치 완료, 실제 기능 적용은 예정
- 문서: 프로젝트 목표 아키텍처, 로드맵, 기술 스택, 작성 규칙 정리

아직 구현하지 않은 영역:

- `apps/api` Spring Boot 백엔드
- Docker Compose 로컬 인프라
- Ollama 연동
- PostgreSQL, pgvector, Redis, MinIO 설정
- 인증, 권한, RAG, Agent UI, Workflow Builder
- OpenTelemetry, Prometheus, Grafana, Loki 관측성

## 로컬 실행 방법

루트 디렉터리에서 실행합니다.

```bash
pnpm install
pnpm dev:web
```

검증 명령어:

```bash
pnpm lint:web
pnpm build:web
```

현재는 프론트엔드만 실행됩니다. 백엔드, 데이터베이스, 로컬 AI, Docker Compose 기반 실행은 향후 구현 예정입니다.

## 향후 구현 예정 기능

- Spring Boot API 프로젝트 생성 및 기본 구조 정리
- 인증 및 RBAC 기반 사용자 권한 모델
- 문서 업로드 및 MinIO 저장
- PostgreSQL + pgvector 기반 임베딩 저장소
- Ollama 기반 로컬 LLM 질의 응답
- RAG 기반 문서 검색 및 답변 생성
- Agent Chat UI
- React Flow 기반 Workflow Builder
- AI Release Copilot
- GitHub Actions CI
- OpenTelemetry, Prometheus, Grafana, Loki 기반 관측성
- Docker Compose 기반 전체 로컬 실행 환경

## 무료/오픈소스 기반으로 설계한 이유

이 프로젝트는 포트폴리오와 학습 목적을 동시에 갖고 있습니다. 유료 AI API나 관리형 클라우드 서비스에 기대지 않고도 실제 업무 자동화 플랫폼의 구조를 설계하고 운영할 수 있음을 보여주는 것이 핵심입니다.

로컬 LLM, 오픈소스 데이터베이스, 자체 구성 가능한 관측성 도구를 사용하면 비용 부담 없이 아키텍처, 운영, 성능, 보안 관점의 의사결정을 직접 경험할 수 있습니다.

## 유료 서비스 미사용 원칙

- 유료 AI API를 필수 의존성으로 두지 않습니다.
- 관리형 벡터 데이터베이스, 관리형 LLM, 관리형 관측성 SaaS를 필수 구성으로 두지 않습니다.
- 로컬 환경 또는 무료 서버에서 재현 가능한 실행 방식을 우선합니다.
- 외부 서비스 연동이 필요해지는 경우에도 대체 가능한 오픈소스 경로를 먼저 검토합니다.
