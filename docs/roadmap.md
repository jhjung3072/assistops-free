# Roadmap

`AI Knowledge Hub`는 팀 내부 지식을 한곳에 모으고, 로컬 LLM과 RAG로 검색·요약·질의응답을 제공하는 지식 관리 플랫폼을 목표로 합니다.

## Phase 0: Rebranding to AI Knowledge Hub

진행 중입니다.

- 제품명과 문서 방향을 AI Knowledge Hub로 정리
- 자동화 중심 문구와 화면 제거
- Documents, Search, RAG, Agent, Prompts 기능 유지
- 기존 자동화 관련 애플리케이션 코드 제거

## Phase 1: Knowledge Library Foundation

현재 구현된 문서 기반 기능을 Knowledge Library 중심으로 정리합니다.

- 문서 업로드와 원본 저장
- 문서 목록과 상태 관리
- 문서 처리, chunking, embedding
- semantic search
- RAG Q&A
- Agent Chat
- Prompt Versioning

## Phase 2: Notes & Links

예정입니다.

- 짧은 메모 저장
- 외부 링크 저장
- 메모/링크를 문서와 같은 검색 대상으로 확장

## Phase 3: Collections & Tags

예정입니다.

- 지식 항목 collection 구성
- tag 기반 탐색
- workspace별 지식 분류

## Phase 4: Summary Generation

예정입니다.

- 문서/메모/링크 요약
- collection 단위 요약
- 요약 결과 이력 저장

## Phase 5: Sharing & Permissions

예정입니다.

- workspace switcher
- 사용자별 workspace filtering 고도화
- 세부 RBAC policy
- 공유 링크 또는 내부 공유 정책 검토

## Phase 6: Monitoring & Deployment

예정입니다.

- OpenTelemetry 기반 관측성
- Prometheus/Grafana/Loki 구성
- 운영용 인증 보안 개선
- production deployment 정리

## 구현된 기반

- 인증
- 문서 업로드와 MinIO 저장
- 텍스트 추출과 chunking
- embedding과 pgvector semantic search
- RAG Answer API와 Q&A UI
- Agent Chat session UI와 streaming response
- Prompt Versioning
- Querydsl dynamic filtering

## 아직 구현하지 않은 영역

- Notes
- Links
- Collections
- Knowledge Tags
- Summary generation
- Sharing/permissions 고도화
- Monitoring
- Production deployment
