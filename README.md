# AssistOps Free

`AssistOps Free`는 유료 AI API나 관리형 클라우드 서비스에 의존하지 않고, 로컬 LLM과 오픈소스 인프라만으로 동작하는 AI 업무 자동화 플랫폼을 목표로 하는 포트폴리오 프로젝트입니다.

현재 단계는 **Querydsl Dynamic Filtering Foundation**입니다. Next.js 프론트엔드, Spring Boot API, Docker Compose 기반 로컬 인프라, PostgreSQL + pgvector 연결, Flyway/JPA 영속성 기반, JWT Bearer 인증 API, 프론트엔드 cookie token storage 기반 인증 화면, 문서 업로드/목록/다운로드/삭제/처리/embedding 화면, semantic chunk search 화면, RAG Q&A 화면, Agent Chat 세션 UI와 streaming 응답이 구성되어 있습니다. MinIO는 원본 문서 저장소로 연결되어 있고, Ollama는 `nomic-embed-text` embedding과 `llama3.2` chat answer generation에 연결되어 있습니다. 문서 목록, RAG 답변 이력, Agent Chat 세션 목록에는 Querydsl 기반 동적 검색/필터링/페이징이 추가되었습니다. Redis는 아직 애플리케이션 코드와 연결하지 않았습니다.

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
| Frontend           | Next.js App Router, React, TypeScript, Tailwind CSS, shadcn/ui                                                                                  | 사용 중   |
| Frontend           | TanStack Query, Zustand, React Hook Form, Zod                                                                                                   | 사용 중   |
| Frontend           | Radix UI, React Flow, Recharts, Playwright                                                                                                      | 예정      |
| Backend            | Java 21, Spring Boot, Spring Web, Spring Boot Actuator, Validation, Lombok, Springdoc OpenAPI UI                                                | 사용 중   |
| Backend            | Spring Data JPA, PostgreSQL Driver, Flyway                                                                                                      | 사용 중   |
| Backend            | Spring Security, JWT, BCrypt                                                                                                                    | 사용 중   |
| Backend            | Workspace membership 기반 RBAC foundation                                                                                                       | 기반 구성 |
| Backend            | Apache Tika 기반 문서 텍스트 추출, 문자 수 기반 chunking                                                                                        | 사용 중   |
| Backend            | Spring AI Ollama embedding/chat 연동                                                                                                            | 사용 중   |
| Backend            | Querydsl                                                                                                                                        | 동적 목록 조회에 사용 중 |
| AI                 | Ollama                                                                                                                                          | embedding/chat model 연동 사용 중 |
| AI                 | `nomic-embed-text` local embedding model                                                                                                        | 사용 중   |
| AI                 | `llama3.2` local chat model, RAG answer generation, source citation                                                                             | 사용 중   |
| AI                 | Agent Chat session UI, message persistence, SSE/fetch streaming                                                                                 | 사용 중   |
| AI                 | prompt versioning, tool calling style internal actions                                                                                          | 예정      |
| Database / Storage | PostgreSQL, pgvector                                                                                                                            | 문서/청크/embedding 저장과 vector search 사용 중 |
| Database / Storage | MinIO                                                                                                                                           | 원본 문서 저장소로 사용 중 |
| Database / Storage | Redis                                                                                                                                           | 로컬 인프라 구성, 앱 미연동 |
| Infra              | Docker Engine, Docker Compose, GitHub Actions                                                                                                   | 사용 중   |
| Infra              | Nginx, Oracle Cloud Always Free 또는 local server                                                                                                | 예정      |
| Monitoring         | OpenTelemetry, Prometheus, Grafana OSS, Loki                                                                                                    | 예정      |

## 현재 구현 상태

- `apps/web`: Next.js App Router 기반 프론트엔드 프로젝트 생성 완료
- `apps/web`: shadcn/ui 초기화 및 기본 UI 컴포넌트 일부 적용
- `apps/web`: 로그인, 회원가입, 인증 보호 dashboard, workspace 목록 조회 화면 구성
- `apps/web`: 문서 업로드, 문서 목록, 다운로드, 삭제 화면 구성
- `apps/web`: 문서 처리 버튼과 chunk 목록 확인 UI 구성
- `apps/web`: 문서 embedding 실행 버튼과 semantic chunk search 화면 구성
- `apps/web`: RAG Q&A 화면, 답변 출처 표시, 답변 이력 조회/삭제 UI 구성
- `apps/web`: Agent Chat 화면, 세션 목록, 메시지 목록, streaming 답변, 출처와 latency 표시 UI 구성
- `apps/web`: 문서/RAG 이력/Agent 세션 검색, 필터, 페이지네이션 UI 구성
- `apps/web`: cookie에서 accessToken을 읽어 Authorization header를 붙이는 fetch API client, TanStack Query, Zustand auth store 연동
- `apps/api`: Spring Boot API 초기 골격 및 `GET /api/health` 구현
- `apps/api`: PostgreSQL datasource, Flyway migration, JPA 기반 `workspaces` 조회 API 구성
- `apps/api`: Spring Security, JWT, BCrypt 기반 인증 API 구성
- `apps/api`: `users`, `workspace_members` 테이블과 workspace membership 기반 RBAC 최소 골격 구성
- `apps/api`: 문서 API, MinIO 원본 파일 저장, PostgreSQL 문서 메타데이터 저장 구성
- `apps/api`: Apache Tika 기반 문서 텍스트 추출, chunking, `document_chunks` 저장 구성
- `apps/api`: Spring AI Ollama 기반 chunk embedding 생성, pgvector 저장, semantic chunk search API 구성
- `apps/api`: Ollama chat model 기반 RAG answer API, 출처 저장, 답변 이력 조회/삭제 구성
- `apps/api`: Agent Chat session/message/source 저장 API와 streaming message API 구성. assistant 답변 생성은 기존 RAG Answer Service를 재사용
- `apps/api`: Querydsl 기반 문서 목록, RAG 답변 이력, Agent Chat 세션 목록 동적 조회 구성
- `docker-compose.yml`: PostgreSQL + pgvector, Redis, MinIO, Ollama 로컬 인프라 실행 구성
- `infra/postgres/init`: PostgreSQL 시작 시 pgvector extension 활성화 SQL 추가
- 루트 `pnpm-workspace.yaml`: `apps/web` workspace 등록
- 루트 `package.json`: 프론트엔드 실행, 빌드, 린트 스크립트 추가
- GitHub Actions: 프론트엔드 lint/build, API test/build 자동 검증 workflow 구성
- 프론트엔드 주요 라이브러리: TanStack Query, Zustand, React Hook Form, Zod를 인증 화면에 적용
- 문서: 프로젝트 목표 아키텍처, 로드맵, 기술 스택, 작성 규칙 정리

아직 구현하지 않은 영역:

- refresh token 저장소와 token rotation
- HttpOnly Cookie 또는 BFF 기반 운영용 인증 구조
- 사용자별 workspace filtering
- workspace switching UI
- 세부 RBAC policy
- Spring Boot와 Redis 연결
- WebSocket 기반 양방향 realtime 처리
- Redis Pub/Sub 또는 queue 기반 비동기 처리
- multi-turn context memory
- tool calling
- Workflow Builder
- OpenTelemetry, Prometheus, Grafana, Loki 관측성

## 로컬 실행 방법

루트 디렉터리에서 실행합니다.

```bash
pnpm install
pnpm infra:up
pnpm dev:api
pnpm dev:web
```

API 확인:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health
```

검증 명령어:

```bash
pnpm lint:web
pnpm build:web
pnpm test:api
pnpm build:api
```

프론트엔드 API base URL은 `apps/web/.env.example`을 기준으로 설정합니다.

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

실제 `apps/web/.env.local`은 커밋하지 않습니다.

확인 가능한 화면:

| Path | 설명 |
| --- | --- |
| `/login` | 로그인 화면 |
| `/register` | 회원가입 화면 |
| `/dashboard` | 인증된 사용자 dashboard와 workspace 목록 |
| `/documents` | 인증된 사용자 문서 업로드와 문서 목록 |
| `/search` | embedding된 chunk semantic search |
| `/rag` | 문서 기반 RAG Q&A와 답변 이력 |
| `/agent` | 세션형 Agent Chat UI와 streaming 답변 |

Health API 확인:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health
```

Swagger UI 확인:

```text
http://localhost:8080/swagger-ui/index.html
```

Auth API 수동 확인:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"local@example.com","password":"password123","name":"Local User"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"local@example.com","password":"password123"}'

curl http://localhost:8080/api/auth/me \
  -H 'Authorization: Bearer <accessToken>'

curl http://localhost:8080/api/workspaces \
  -H 'Authorization: Bearer <accessToken>'

curl -X POST http://localhost:8080/api/documents \
  -H 'Authorization: Bearer <accessToken>' \
  -F 'file=@./sample.pdf'

curl -X POST http://localhost:8080/api/documents/<documentId>/process \
  -H 'Authorization: Bearer <accessToken>'

curl http://localhost:8080/api/documents/<documentId>/chunks \
  -H 'Authorization: Bearer <accessToken>'

curl -X POST http://localhost:8080/api/documents/<documentId>/embed \
  -H 'Authorization: Bearer <accessToken>'

curl -X POST http://localhost:8080/api/search/chunks \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"query":"문서에서 찾을 내용","topK":5}'

curl -X POST http://localhost:8080/api/rag/answer \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"question":"문서 기반으로 답변해줘","topK":5}'

curl http://localhost:8080/api/rag/answers \
  -H 'Authorization: Bearer <accessToken>'

curl -X POST http://localhost:8080/api/agent/sessions \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"title":"새 채팅"}'

curl http://localhost:8080/api/agent/sessions \
  -H 'Authorization: Bearer <accessToken>'

curl -X POST http://localhost:8080/api/agent/sessions/<sessionId>/messages \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"content":"문서 기반으로 답변해줘","topK":3}'

curl -N -X POST http://localhost:8080/api/agent/sessions/<sessionId>/messages/stream \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Accept: text/event-stream' \
  -H 'Content-Type: application/json' \
  -d '{"content":"문서 기반으로 답변해줘","topK":3}'
```

주요 Auth endpoint:

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `POST` | `/api/auth/register` | 회원가입 후 JWT accessToken을 JSON body로 반환 |
| `POST` | `/api/auth/login` | email/password 로그인 후 JWT accessToken을 JSON body로 반환 |
| `GET` | `/api/auth/me` | 현재 인증된 사용자 정보 반환 |

백엔드 테스트:

```bash
pnpm test:api
```

현재 백엔드는 PostgreSQL 연결, `workspaces` 조회 API, JWT 기반 인증 API, 문서 업로드/처리/embedding/search/RAG answer API, Agent Chat API, Agent Chat streaming API, Querydsl 기반 동적 목록 조회까지 구성되어 있습니다. `/api/workspaces`는 인증된 사용자만 접근할 수 있지만, 사용자별 workspace filtering은 다음 단계에서 구현할 예정입니다. refresh token, 세부 RBAC policy, Redis queue, WebSocket, multi-turn context memory는 아직 구현하지 않았습니다.

현재 인증은 백엔드가 JWT accessToken을 JSON 응답 body로 내려주고, 프론트엔드가 해당 토큰을 `assistops_access_token` browser cookie에 저장하는 방식입니다. 프론트엔드는 accessToken을 `localStorage`나 `sessionStorage`에 저장하지 않습니다. API 요청 시 cookie에서 token을 읽어 `Authorization: Bearer <token>` header를 추가하고, 로그인 상태 복원은 `GET /api/auth/me` 응답으로 판단합니다.

현재 cookie는 프론트엔드 JavaScript가 읽고 쓰는 cookie이므로 `HttpOnly`가 아닙니다. 개발 환경 기본값은 `SameSite=Lax`, `Secure=false`, `Path=/`, `Max-Age=3600`입니다. 운영 수준 보안에서는 HttpOnly Cookie 또는 BFF 패턴, refresh token, token rotation, XSS 방어, CSRF 방어를 추가로 검토해야 합니다.

## 문서 업로드

인증된 사용자는 `/documents` 화면에서 문서를 업로드하고 관리할 수 있습니다. 원본 파일은 MinIO bucket `assistops-documents`에 저장하고, 파일명, content type, 크기, 상태, workspace 정보 같은 메타데이터는 PostgreSQL `documents` 테이블에 저장합니다.

지원 파일:

| 확장자 | Content Type |
| --- | --- |
| `.pdf` | `application/pdf` |
| `.txt` | `text/plain` |
| `.md` | `text/markdown` 또는 일부 브라우저의 `application/octet-stream` |

최대 파일 크기는 10MB입니다. `/documents` 화면의 `Process` 버튼을 누르면 API가 MinIO 원본 파일을 읽고 Apache Tika로 텍스트를 추출한 뒤, RAG 준비를 위한 chunk를 PostgreSQL `document_chunks` 테이블에 저장합니다. 현재 chunking은 문자 수 기준이며 기본 chunk size는 1000자, overlap은 150자입니다. `tokenCount`는 정확한 tokenizer가 아니라 `content.length / 4` 기준의 추정값입니다.

`Embed` 버튼을 누르면 `document_chunks.content`를 Ollama embedding model로 vector화하고 `document_chunks.embedding vector(768)`에 저장합니다. 기본 모델은 `nomic-embed-text`이며, Nomic 모델 카드 기준 기본 embedding dimension은 768입니다. `/search` 화면은 query를 같은 embedding model로 vector화한 뒤 pgvector cosine distance 기준으로 가까운 chunk를 반환합니다.

Ollama embedding/chat model은 처음 실행 전에 준비해야 합니다.

```bash
docker exec -it assistops-ollama ollama pull nomic-embed-text
docker exec -it assistops-ollama ollama pull llama3.2
```

모델이 없거나 Ollama가 아직 준비되지 않았다면 `POST /api/documents/{id}/embed`, `POST /api/search/chunks`, `POST /api/rag/answer`가 실패할 수 있습니다. 로컬 머신 성능과 모델 크기에 따라 답변 생성 시간이 느릴 수 있습니다.

`/rag` 화면에서는 질문을 입력하면 semantic search 결과 chunk를 context로 사용해 Ollama local chat model이 한국어 답변을 생성합니다. 답변과 출처 chunk는 PostgreSQL `rag_answers`, `rag_answer_sources` 테이블에 저장되며, 최근 답변 이력 조회와 삭제를 지원합니다.

RAG 답변 API는 성능 확인을 위해 단계별 latency metrics를 응답과 `rag_answers` row에 저장합니다. backend log에도 아래와 같은 summary가 남습니다.

```text
RAG answer latency: totalMs=8200, queryEmbeddingMs=350, vectorSearchMs=45, promptBuildMs=5, chatGenerationMs=7600, answerPersistMs=80, sourceCount=3, promptContextCharCount=2100, answerCharCount=450, model=llama3.2, questionLength=42
```

로컬 RAG 기본 성능 설정:

| 설정 | 기본값 | 설명 |
| --- | --- | --- |
| `RAG_DEFAULT_TOP_K` | `3` | RAG answer 기본 검색 chunk 수 |
| `RAG_MIN_TOP_K` / `RAG_MAX_TOP_K` | `1` / `8` | RAG answer topK 허용 범위 |
| `RAG_CONTEXT_CHUNK_MAX_CHARS` | `800` | prompt에 넣는 chunk별 최대 문자 수 |
| `RAG_CONTEXT_TOTAL_MAX_CHARS` | `3000` | prompt 전체 context 최대 문자 수 |
| `OLLAMA_CHAT_NUM_PREDICT` | `256` | 답변 생성 최대 token 수. 낮을수록 빠르지만 짧아질 수 있음 |
| `OLLAMA_CHAT_TEMPERATURE` | `0.2` | 답변 안정성을 위한 낮은 temperature |
| `OLLAMA_CHAT_TOP_P` | `0.9` | sampling top-p |
| `OLLAMA_CHAT_KEEP_ALIVE` | `30m` | 모델을 메모리에 유지해 재로딩 비용 감소 |

첫 요청은 모델 로딩 때문에 느릴 수 있습니다. 더 빠른 로컬 테스트가 필요하면 작은 chat model을 사용할 수 있습니다.

```bash
docker exec -it assistops-ollama ollama pull llama3.2:1b
OLLAMA_CHAT_MODEL=llama3.2:1b pnpm dev:api
```

`/rag`는 질문 하나에 대한 단발성 Q&A와 답변 이력 관리 화면입니다. `/agent`는 같은 RAG Answer 기능을 세션형 채팅 UI로 저장하고 다시 확인하는 화면입니다. `/agent`에서는 `POST /api/agent/sessions/{id}/messages/stream`을 기본으로 사용해 assistant 답변을 SSE/fetch stream으로 점진 표시합니다. streaming은 체감 속도 개선 목적이며, 첫 요청은 Ollama cold start 때문에 여전히 느릴 수 있습니다. 현재 `/agent`는 메시지를 저장하지만 이전 메시지를 LLM context로 다시 넣는 multi-turn memory는 아직 구현하지 않았습니다.

Agent Chat streaming event는 `text/event-stream` 형식입니다.

| Event | 역할 |
| --- | --- |
| `metadata` | session id, user message id, model 전달 |
| `source` | 검색된 source chunk 전달 |
| `token` | assistant 답변 token/chunk 전달 |
| `latency` | total, search, generation, persist latency 전달 |
| `done` | assistant message id, rag answer id 전달 |
| `error` | streaming 중 오류 전달 |

현재 단계에서는 WebSocket, Redis Pub/Sub, queue 기반 비동기 처리, multi-turn conversation, Agent Chat 장기 메모리, tool calling, Workflow Builder는 아직 구현하지 않았습니다.

주요 Document endpoint:

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `POST` | `/api/documents` | multipart/form-data 문서 업로드 |
| `GET` | `/api/documents` | 접근 가능한 workspace의 문서 목록 조회. `keyword`, `status`, `embeddingStatus`, `createdFrom`, `createdTo`, `page`, `size` 지원 |
| `GET` | `/api/documents/{id}` | 문서 메타데이터 상세 조회 |
| `GET` | `/api/documents/{id}/download` | 원본 파일 다운로드 |
| `DELETE` | `/api/documents/{id}` | 문서 soft delete 및 MinIO object 삭제 |
| `POST` | `/api/documents/{id}/process` | 텍스트 추출과 chunk 생성 |
| `GET` | `/api/documents/{id}/chunks` | 문서 chunk 목록 조회 |
| `POST` | `/api/documents/{id}/embed` | 문서 chunk embedding 생성과 pgvector 저장 |
| `POST` | `/api/search/chunks` | query 기반 유사 chunk 검색 |
| `POST` | `/api/rag/answer` | 유사 chunk를 context로 RAG 답변 생성 |
| `GET` | `/api/rag/answers` | RAG 답변 이력 조회. `keyword`, `model`, `createdFrom`, `createdTo`, `page`, `size` 지원 |
| `GET` | `/api/rag/answers/{id}` | RAG 답변 상세와 출처 조회 |
| `DELETE` | `/api/rag/answers/{id}` | RAG 답변 삭제 |
| `POST` | `/api/agent/sessions` | Agent Chat session 생성 |
| `GET` | `/api/agent/sessions` | 현재 사용자의 Agent Chat session 목록 조회. `keyword`, `createdFrom`, `createdTo`, `page`, `size` 지원 |
| `GET` | `/api/agent/sessions/{id}` | Agent Chat session 상세, 메시지, 출처 조회 |
| `POST` | `/api/agent/sessions/{id}/messages` | 사용자 메시지 저장 후 RAG Answer Service로 assistant 답변 생성 |
| `POST` | `/api/agent/sessions/{id}/messages/stream` | 사용자 메시지 저장 후 assistant 답변을 `text/event-stream`으로 전송 |
| `DELETE` | `/api/agent/sessions/{id}` | Agent Chat session과 메시지 삭제 |

## 동적 목록 조회

문서 목록, RAG 답변 이력, Agent Chat 세션 목록은 Querydsl 기반 동적 조건 조회를 사용합니다. 응답은 기존 배열 필드인 `documents`, `answers`, `sessions`를 유지하면서 `page` 메타데이터를 함께 반환합니다.

공통 pagination 기본값:

| 항목 | 값 |
| --- | --- |
| 기본 `page` | `0` |
| 기본 `size` | `20` |
| 최대 `size` | `100` |
| 정렬 | 문서/RAG 답변은 `createdAt desc`, Agent 세션은 `updatedAt desc` |

`size`가 100보다 크면 100으로 clamp합니다. 날짜 필터는 ISO-8601 date-time 문자열을 사용하며, 예시는 `2026-05-01T00:00:00`입니다. 서버에서는 UTC 기준으로 비교합니다. 형식이 잘못된 날짜나 enum 값은 400 응답을 반환합니다.

예시:

```bash
curl 'http://localhost:8080/api/documents?keyword=release&status=PROCESSED&page=0&size=20' \
  -H 'Authorization: Bearer <accessToken>'

curl 'http://localhost:8080/api/rag/answers?keyword=운영&model=llama&page=0&size=20' \
  -H 'Authorization: Bearer <accessToken>'

curl 'http://localhost:8080/api/agent/sessions?keyword=릴리스&page=0&size=20' \
  -H 'Authorization: Bearer <accessToken>'
```

데이터 접근 역할은 다음처럼 구분합니다.

| 방식 | 사용 영역 |
| --- | --- |
| Spring Data JPA Repository | 단순 CRUD, ID 기반 조회, 저장/삭제 |
| Querydsl | keyword, status, 기간, pagination이 필요한 동적 목록 조회 |
| native SQL/JDBC | pgvector similarity search, embedding vector update 같은 PostgreSQL 특화 연산 |

pgvector cosine distance 검색과 embedding vector 저장은 Querydsl로 대체하지 않고 native SQL/JDBC 기반을 유지합니다.

Querydsl Q-class는 Gradle annotation processor가 `apps/api/build/generated/sources/annotationProcessor/java/main` 아래에 생성합니다. `build/` 디렉터리는 git ignore 대상이므로 생성된 Q-class는 커밋하지 않습니다.

## 로컬 인프라 실행 방법

루트 디렉터리에서 Docker Compose 기반 로컬 인프라를 실행합니다.

```bash
pnpm infra:up
pnpm infra:ps
pnpm infra:logs
pnpm infra:down
```

개발용 PostgreSQL volume을 완전히 삭제하고 처음부터 다시 초기화해야 할 때만 아래 명령을 사용합니다.

```bash
pnpm infra:reset
```

`pnpm infra:reset`은 내부적으로 `docker compose down -v && docker compose up -d`를 실행합니다. 이 명령은 Docker named volume의 PostgreSQL 데이터를 삭제하므로 로컬 개발 데이터가 사라집니다. 실제 운영 DB나 보존해야 하는 데이터가 있는 환경에서는 절대 사용하지 않습니다.

개발용 접속 정보:

| 서비스 | 접속 정보 |
| --- | --- |
| PostgreSQL | `localhost:15432`, database `assistops`, user `assistops`, password `assistops` |
| Redis | `localhost:6379` |
| MinIO API | `http://localhost:9000`, access key `assistops`, secret key `assistops123` |
| MinIO Console | `http://localhost:9001` |
| Ollama | `http://localhost:11434`, embedding model `nomic-embed-text`, chat model `llama3.2` |

개발용 계정과 비밀번호는 `.env.example`에 예시로만 제공합니다. 이 값은 로컬 개발용 기본값이며 운영용으로 사용하지 않습니다. 실제 `.env` 파일은 커밋하지 않습니다.
PostgreSQL 컨테이너 최초 초기화에는 `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`가 사용되고, Spring Boot datasource에는 `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`가 사용됩니다. 로컬 기본값은 모두 `assistops`로 맞춰져 있습니다.
Docker Compose PostgreSQL은 로컬 PostgreSQL과 충돌하지 않도록 기본 host port를 `15432`로 사용합니다. 필요하면 `.env`의 `DB_PORT` 값으로 Docker Compose PostgreSQL의 host port를 조정할 수 있습니다.
JWT와 MinIO 개발용 secret도 `.env.example`에 예시로만 제공합니다. 운영 환경에서는 `JWT_SECRET`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`를 실제 비밀값으로 반드시 override해야 합니다.

현재 Spring Boot API는 PostgreSQL, MinIO, Spring AI Ollama embedding/chat 호출에 연결되어 있습니다. Redis client와 queue 기반 비동기 처리는 후속 단계에서 추가할 예정입니다.

## Troubleshooting

### `FATAL: role "assistops" does not exist`

`pnpm dev:api` 실행 중 Flyway가 PostgreSQL에 연결하지 못하고 `FATAL: role "assistops" does not exist`가 발생하면, Docker named volume이 예전 PostgreSQL 설정으로 이미 초기화되어 있을 가능성이 큽니다.

PostgreSQL Docker image는 data directory가 비어 있을 때만 `POSTGRES_USER`, `POSTGRES_DB`, `/docker-entrypoint-initdb.d` 초기화 스크립트를 적용합니다. 이미 named volume이 만들어진 뒤에는 `docker-compose.yml`이나 `.env`의 `POSTGRES_USER` 값을 변경해도 기존 DB role에는 반영되지 않습니다.

로컬 개발 데이터 삭제가 가능하다면 아래 중 하나로 volume을 초기화합니다.

```bash
pnpm infra:down
docker compose down -v
pnpm infra:up
```

또는 한 번에 실행합니다.

```bash
pnpm infra:reset
```

주의: `docker compose down -v`와 `pnpm infra:reset`은 PostgreSQL 데이터를 삭제합니다. 실제 운영 DB, 공유 개발 DB, 보존해야 하는 로컬 데이터에는 사용하지 않습니다.

reset 후에도 같은 오류가 계속된다면 로컬 머신의 다른 PostgreSQL이 Spring Boot datasource port를 먼저 사용 중인지 확인합니다.

```bash
lsof -nP -iTCP:15432 -sTCP:LISTEN
```

Docker 컨테이너 내부에서는 `assistops` role이 존재하지만 `localhost:15432` 접속만 실패한다면, 다른 프로세스가 Docker PostgreSQL을 가리고 있을 수 있습니다. 이 경우 로컬 `.env`에서 `DB_PORT`를 다른 값으로 바꾸고 인프라와 API를 같은 포트로 실행합니다.

```bash
DB_PORT=25432 pnpm infra:reset
DB_PORT=25432 pnpm dev:api
```

로컬 `.env`를 사용할 경우에도 실제 `.env` 파일은 커밋하지 않습니다.

## 향후 구현 예정 기능

- refresh token과 token rotation
- HttpOnly Cookie 또는 BFF 기반 운영용 인증 개선
- XSS/CSRF 방어 강화
- 사용자별 workspace filtering
- workspace switching UI
- 세부 RBAC policy
- Redis queue 기반 비동기 embedding/answer 처리
- WebSocket 기반 양방향 realtime 처리
- Redis Pub/Sub 기반 이벤트 fan-out
- multi-turn context memory
- Agent tool calling
- RAG prompt evaluation과 답변 품질 개선
- React Flow 기반 Workflow Builder
- AI Release Copilot
- GitHub Actions CI 고도화
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
