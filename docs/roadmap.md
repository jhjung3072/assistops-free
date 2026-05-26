# Roadmap

`AssistOps Free`는 한 번에 전체 기능을 구현하지 않고, 포트폴리오로 설명 가능한 단위별로 개발합니다. 각 Phase는 현재 상태와 목표를 분리해 기록합니다.

## Phase 0: Monorepo & Frontend Foundation

기본 기반 구성을 마친 단계입니다.

- 루트 workspace 구성
- `apps/web` Next.js App Router 프로젝트 정리
- Frontend App Foundation 구성
- README와 docs 기반 문서화
- 기본 랜딩 화면 구성
- 프론트엔드 주요 라이브러리 설치
- GitHub Actions Web CI 구성

## Phase 1: Spring Boot API Foundation

기본 기반 구성을 마친 단계입니다.

- `apps/api` Spring Boot 프로젝트 생성
- Java 21 기반 개발 환경 구성
- `GET /api/health` health API 구현
- Spring Boot 테스트 기반 구성
- validation과 최소 전역 예외 처리 구성
- Springdoc OpenAPI 세부 문서화 정리 예정

## Phase 2: Local Infrastructure with Docker Compose

기본 기반 구성을 마친 단계입니다.

- Docker Compose 기반 PostgreSQL, Redis, MinIO, Ollama 실행
- 로컬 개발용 환경 변수 정리
- PostgreSQL pgvector extension 초기화 스크립트 구성
- Spring Boot DB 연결은 Backend Persistence Foundation에서 진행
- RAG pipeline과 Spring AI 연동은 후속 Phase에서 구현 예정

## Phase 2.5: Backend Persistence Foundation

기본 기반 구성을 마친 단계입니다.

- Spring Boot datasource와 PostgreSQL 연결
- Spring Data JPA, PostgreSQL Driver, Flyway 추가
- `workspaces` 기본 테이블과 seed data migration 구성
- `Workspace` entity와 repository 기반 조회 API 구성
- health API에 database 상태 포함
- Redis, MinIO, Ollama, Spring AI, RAG pipeline 연동은 후속 Phase에서 구현 예정

## Phase 3: Auth & RBAC Foundation

기본 기반 구성을 마친 단계입니다.

- Spring Security 기반 stateless 인증 구조 구성
- JWT accessToken JSON body 발급과 Authorization Bearer header 인증 구성
- BCrypt password hashing 구성
- `users` 테이블과 JPA entity/repository 구성
- `workspace_members` 테이블과 workspace membership 역할 골격 구성
- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` 구현
- 회원가입 시 seed `Default Workspace`에 `MEMBER` membership 연결
- 인증/인가 테스트 추가
- refresh token, 사용자별 workspace filtering, 세부 RBAC policy는 후속 작업으로 유지

## Phase 3.5: Frontend Auth Integration

현재 진행 중인 단계입니다.

- 로그인 화면 구현
- 회원가입 화면 구현
- cookie token storage 기반 API client 구성
- Zustand 기반 사용자 상태와 `/api/auth/me` 인증 상태 복원 구성
- TanStack Query 기반 현재 사용자 조회와 workspace 목록 조회 구성
- 인증 보호 dashboard 구현
- AppHeader의 Dashboard/Login/Logout 진입점 구성
- localStorage token 저장 방식에서 browser cookie 저장 방식으로 개선
- 현재 cookie는 JavaScript가 읽고 쓰는 non-HttpOnly cookie
- refresh token, HttpOnly Cookie 또는 BFF 인증 구조, 세부 RBAC UI, workspace switching은 후속 작업으로 유지

## Phase 4: Document Upload & Storage

향후 구현 예정입니다.

- 문서 업로드 UI
- MinIO 객체 저장
- 문서 메타데이터 관리
- 업로드 상태와 처리 이력 관리

## Phase 5: RAG Pipeline with Ollama and pgvector

향후 구현 예정입니다.

- local embedding model 연동
- 문서 chunking 및 embedding 저장
- PostgreSQL + pgvector 유사도 검색
- Ollama 기반 답변 생성
- prompt versioning 구조

## Phase 6: Agent Chat UI

향후 구현 예정입니다.

- 업무 질의용 채팅 화면
- RAG 출처 표시
- internal action 실행 결과 표시
- 대화 이력 관리

## Phase 7: Workflow Builder

향후 구현 예정입니다.

- React Flow 기반 워크플로우 편집 화면
- 트리거, 조건, 액션 노드 모델
- 실행 로그와 실패 재시도 구조

## Phase 8: AI Release Copilot

향후 구현 예정입니다.

- 변경 이력 요약
- 릴리스 노트 초안 생성
- PR 또는 commit 기반 요약 흐름 검토

## Phase 9: Monitoring & Observability

향후 구현 예정입니다.

- OpenTelemetry instrumentation
- Prometheus metric 수집
- Loki log 수집
- Grafana dashboard 구성

## Phase 10: Deployment & Portfolio Polish

향후 구현 예정입니다.

- GitHub Actions CI 고도화
- Nginx reverse proxy 구성
- Oracle Cloud Always Free 또는 local server 배포 검토
- 포트폴리오용 스크린샷, 아키텍처 설명, 시연 흐름 정리
