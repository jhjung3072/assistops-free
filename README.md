# AI Knowledge Hub

`AI Knowledge Hub`는 문서, 메모, 링크, 대화를 한곳에 모으고 로컬 LLM과 RAG를 통해 검색·요약·질의응답을 제공하는 지식 관리 플랫폼입니다.

이 프로젝트는 유료 AI API나 관리형 클라우드 서비스에 의존하지 않고, Docker Compose 기반 PostgreSQL + pgvector, MinIO, Ollama를 사용해 로컬에서 재현 가능한 지식 허브를 목표로 합니다.

## 핵심 기능

- JWT 기반 인증과 workspace membership 기반 권한 골격
- 문서 업로드, MinIO 원본 저장, PostgreSQL 문서 메타데이터 저장
- Apache Tika 기반 PDF/TXT/MD 텍스트 추출
- 문서 chunking과 PostgreSQL `document_chunks` 저장
- Ollama `nomic-embed-text` 기반 embedding 생성
- pgvector 기반 semantic chunk search
- Ollama `llama3.2` 기반 RAG Q&A와 source citation
- Agent Chat 세션, 메시지 이력, streaming response
- Prompt Template/Version 관리와 RAG/Agent prompt traceability
- Querydsl 기반 문서, RAG 답변 이력, Agent 세션 동적 필터링

## 제품 범위 정리

AI Knowledge Hub는 지식 수집, 검색, 답변, 출처 추적을 중심으로 합니다. 기존 자동화 중심의 Workflow Builder, Workflow Run Simulation, Release Copilot 방향은 현재 제품 범위에서 제외했습니다.

이번 단계에서는 기존 자동화 API/UI 코드를 제거했지만, 이미 존재하는 Flyway migration 파일은 삭제하지 않았고 테이블 drop migration도 추가하지 않았습니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Frontend | Next.js App Router, React, TypeScript, Tailwind CSS, shadcn/ui |
| State/Data | TanStack Query, Zustand, React Hook Form, Zod |
| Backend | Java 21, Spring Boot, Spring Security, Spring Data JPA, Querydsl |
| Database | PostgreSQL, pgvector, Flyway |
| Storage | MinIO |
| AI | Ollama, Spring AI Ollama, Apache Tika |
| Infra | Docker Compose, GitHub Actions |

pgvector similarity search와 embedding vector update는 PostgreSQL 특화 연산이므로 native SQL/JDBC 기반을 유지합니다. 단순 CRUD는 Spring Data JPA, 동적 목록 조회는 Querydsl을 사용합니다.

## 로컬 실행

```bash
pnpm install
pnpm infra:up
pnpm dev:api
pnpm dev:web
```

브라우저에서 확인할 수 있는 주요 화면:

- `http://localhost:3000/login`
- `http://localhost:3000/dashboard`
- `http://localhost:3000/documents`
- `http://localhost:3000/search`
- `http://localhost:3000/rag`
- `http://localhost:3000/agent`
- `http://localhost:3000/prompts`

API health check:

- `GET http://localhost:8080/api/health`
- `GET http://localhost:8080/actuator/health`

## Ollama 모델 준비

embedding과 답변 생성을 사용하려면 Ollama 컨테이너에 모델을 준비해야 합니다.

```bash
docker exec -it assistops-ollama ollama pull nomic-embed-text
docker exec -it assistops-ollama ollama pull llama3.2
```

모델이 없으면 embedding 또는 RAG/Agent 답변 생성 API가 실패할 수 있습니다. 로컬 머신 성능에 따라 첫 요청과 답변 생성 시간이 달라질 수 있습니다.

## 인증 방식

백엔드는 로그인/회원가입 성공 시 JWT `accessToken`을 JSON 응답 body로 발급합니다. 프론트엔드는 이 값을 `localStorage`가 아니라 JavaScript가 읽고 쓰는 browser cookie에 저장하고, API 요청마다 cookie에서 token을 읽어 `Authorization: Bearer <token>` header를 추가합니다.

이 cookie는 `HttpOnly`가 아닙니다. 운영 수준 보안에서는 HttpOnly Cookie 또는 BFF 패턴, refresh token, XSS/CSRF 방어를 추가로 검토해야 합니다.

## 문서 처리 흐름

1. `/documents`에서 PDF/TXT/MD 파일을 업로드합니다.
2. 원본 파일은 MinIO bucket에 저장됩니다.
3. 문서 메타데이터는 PostgreSQL `documents` 테이블에 저장됩니다.
4. `Process`를 실행하면 Apache Tika로 텍스트를 추출하고 chunk로 나눕니다.
5. `Embed`를 실행하면 chunk content를 Ollama embedding model로 vector화해 pgvector에 저장합니다.
6. `/search`는 query embedding과 pgvector cosine distance로 관련 chunk를 검색합니다.
7. `/rag`와 `/agent`는 검색된 chunk를 context로 사용해 근거 기반 답변을 생성합니다.

## 현재 구현된 기능

- 인증
- 문서 업로드와 MinIO 저장
- 텍스트 추출
- chunking
- embedding
- pgvector semantic search
- RAG Q&A
- Agent Chat
- Prompt Versioning
- Querydsl filtering

## 아직 구현하지 않은 기능

- Notes
- Links
- Collections
- Knowledge Tags
- Summary generation
- Sharing/permissions 고도화
- Monitoring
- Production deployment

## 로컬 인프라 주의

PostgreSQL은 로컬 포트 충돌 방지를 위해 host port `15432`를 사용합니다. Docker named volume은 최초 초기화 이후 `POSTGRES_USER`, `POSTGRES_DB` 변경을 자동 반영하지 않습니다.

개발 환경에서 데이터 삭제가 가능할 때만 아래 명령으로 volume을 초기화하세요. 운영 DB에는 사용하면 안 됩니다.

```bash
pnpm infra:reset
```

## 검증 명령

```bash
pnpm test:api
pnpm build:api
pnpm lint:web
pnpm build:web
```
