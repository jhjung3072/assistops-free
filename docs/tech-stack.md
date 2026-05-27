# Tech Stack

현재 상태는 실제 구현 또는 설정 반영 여부를 기준으로 구분합니다.

| 영역 | 기술 | 사용 목적 | 현재 상태 |
| --- | --- | --- | --- |
| Frontend | Next.js App Router | 프론트엔드 라우팅과 화면 구성 | 사용 중 |
| Frontend | React | UI 컴포넌트 개발 | 사용 중 |
| Frontend | TypeScript | 정적 타입 기반 개발 | 사용 중 |
| Frontend | Tailwind CSS | 유틸리티 기반 스타일링 | 사용 중 |
| Frontend | shadcn/ui | 재사용 가능한 UI 컴포넌트 | 사용 중 |
| Frontend | TanStack Query | API 요청 상태 관리 | 사용 중 |
| Frontend | Zustand | 사용자 인증 상태 관리 | 사용 중 |
| Frontend | React Hook Form | 로그인/회원가입 폼 상태 관리 | 사용 중 |
| Frontend | Zod | 입력값 검증 | 사용 중 |
| Frontend | Frontend Cookie Token Storage | accessToken을 browser cookie에 저장하고 Authorization header로 전달 | 사용 중 |
| Backend | Java 21 | 백엔드 런타임 | 사용 중 |
| Backend | Spring Boot | 백엔드 API 애플리케이션 | 사용 중 |
| Backend | Spring Security | stateless API 인증과 인가 | 사용 중 |
| Backend | JWT Bearer Auth | accessToken 기반 API 인증 | 사용 중 |
| Backend | Spring Data JPA | 관계형 데이터 접근 | 사용 중 |
| Backend | Querydsl | 동적 목록 조회 | 사용 중 |
| Backend | Flyway | 데이터베이스 schema migration | 사용 중 |
| Backend | Springdoc OpenAPI | Swagger UI 문서화 | 사용 중 |
| Backend | MinIO Java SDK | 문서 원본 파일 저장/다운로드/삭제 | 사용 중 |
| Backend | Apache Tika | PDF/TXT/MD 텍스트 추출 | 사용 중 |
| Backend | Spring AI Ollama | 로컬 Ollama embedding/chat model 호출 | 사용 중 |
| Database | PostgreSQL | 주요 데이터 저장 | 사용 중 |
| Database | pgvector | chunk embedding 저장과 유사도 검색 | 사용 중 |
| Database | Native SQL for pgvector | vector update와 similarity search | 사용 중 |
| Storage | MinIO | 문서 원본 object storage | 사용 중 |
| AI | Ollama | 로컬 embedding/chat model 실행 | 사용 중 |
| AI | nomic-embed-text | 기본 local embedding model | 사용 중 |
| AI | llama3.2 | 기본 local chat model | 사용 중 |
| AI | RAG Q&A | 검색 결과 기반 답변 생성 | 사용 중 |
| AI | Agent Chat | 세션형 Q&A와 streaming response | 사용 중 |
| AI | Prompt Versioning | prompt template/version과 사용 이력 추적 | 사용 중 |
| Infra | Docker Compose | 로컬 통합 실행 환경 | 사용 중 |
| Infra | GitHub Actions | lint/build/test CI | 사용 중 |
| Infra | Redis | cache/session/queue 후보 | 예정, 앱 미연동 |
| Monitoring | OpenTelemetry | trace/metric/log 수집 표준 | 예정 |
| Monitoring | Prometheus/Grafana/Loki | metric/log 저장과 시각화 | 예정 |

제품 범위에서 제외된 그래프 편집 의존성은 제거합니다.
