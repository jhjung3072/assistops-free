# Tech Stack

현재 상태는 실제 구현 또는 설정 반영 여부를 기준으로 구분합니다. `예정`은 목표 기술 스택에는 포함되지만 아직 기능 구현이나 운영 설정이 완료되지 않은 항목입니다.

| 영역 | 기술 | 사용 목적 | 현재 상태 |
| --- | --- | --- | --- |
| Frontend | Next.js App Router | 프론트엔드 라우팅과 화면 구성 | 사용 중 |
| Frontend | React | UI 컴포넌트 개발 | 사용 중 |
| Frontend | TypeScript | 정적 타입 기반 프론트엔드 개발 | 사용 중 |
| Frontend | Tailwind CSS | 유틸리티 기반 스타일링 | 사용 중 |
| Frontend | shadcn/ui | 재사용 가능한 UI 컴포넌트 기반 | 사용 중 |
| Frontend | Base UI | 현재 shadcn/ui preset의 headless UI 기반 | 사용 중 |
| Frontend | Radix UI | shadcn/ui 컴포넌트 확장 시 사용할 수 있는 headless UI primitive | 예정 |
| Frontend | TanStack Query | 서버 상태 관리와 API 캐싱 | 예정 |
| Frontend | Zustand | 클라이언트 상태 관리 | 예정 |
| Frontend | React Hook Form | 폼 상태 관리 | 예정 |
| Frontend | Zod | schema validation | 예정 |
| Frontend | React Flow | workflow builder 그래프 UI | 예정 |
| Frontend | Recharts | dashboard chart 시각화 | 예정 |
| Frontend | Playwright | E2E 테스트 | 예정 |
| Frontend | lucide-react | 아이콘 컴포넌트 | 사용 중 |
| Frontend | class-variance-authority | UI variant 스타일 구성 | 예정 |
| Frontend | clsx | 조건부 className 조합 | 사용 중 |
| Frontend | tailwind-merge | Tailwind className 병합 | 사용 중 |
| Backend | Java 21 | 백엔드 런타임 | 사용 중 |
| Backend | Spring Boot | 백엔드 API 애플리케이션 | 사용 중 |
| Backend | Spring Web | REST API 구현 | 사용 중 |
| Backend | Spring Boot Actuator | health와 운영 endpoint 기반 | 사용 중 |
| Backend | Validation | 요청 값 검증 기반 | 사용 중 |
| Backend | Lombok | Java boilerplate 감소 | 사용 중 |
| Backend | Spring Security | 인증과 인가 | 예정 |
| Backend | Spring AI | AI 연동 추상화 검토 | 예정 |
| Backend | Spring Data JPA | 관계형 데이터 접근 | 예정 |
| Backend | Querydsl | 타입 안전 동적 쿼리 | 예정 |
| Backend | Springdoc OpenAPI UI | API 문서화와 Swagger UI | 사용 중 |
| Backend | Backend DB integration | Spring Boot와 PostgreSQL 연결 | 예정 |
| AI | Ollama | 로컬 LLM 실행 | 로컬 인프라 구성 |
| AI | qwen2.5-coder 또는 llama3.2 | 로컬 LLM 후보 모델 | 검토 |
| AI | local embedding model | 문서 임베딩 생성 | 예정 |
| AI | RAG pipeline | 검색 증강 생성 흐름 | 예정 |
| AI | prompt versioning | 프롬프트 변경 이력 관리 | 예정 |
| AI | tool calling style internal actions | 내부 업무 액션 실행 구조 | 예정 |
| Database / Storage | PostgreSQL | 주요 업무 데이터 저장 | 로컬 인프라 구성 |
| Database / Storage | pgvector | 벡터 임베딩 저장과 유사도 검색 | 로컬 인프라 구성 |
| Database / Storage | Redis | 캐시와 세션 또는 작업 큐 보조 | 로컬 인프라 구성 |
| Database / Storage | MinIO | 문서 파일 객체 저장 | 로컬 인프라 구성 |
| Infra | Docker Engine | 로컬 컨테이너 실행 | 사용 중 |
| Infra | Docker Compose | 로컬 통합 실행 환경 | 사용 중 |
| Infra | Nginx | reverse proxy | 예정 |
| Infra | GitHub Actions | CI 자동화 | 예정 |
| Infra | Oracle Cloud Always Free 또는 local server | 무료 또는 로컬 배포 환경 | 검토 |
| Monitoring | OpenTelemetry | trace, metric, log 수집 표준 | 예정 |
| Monitoring | Prometheus | metric 저장과 조회 | 예정 |
| Monitoring | Grafana OSS | dashboard 시각화 | 예정 |
| Monitoring | Loki | log 저장과 조회 | 예정 |
