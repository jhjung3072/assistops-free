# AI Knowledge Hub 테스트 문서

AI Knowledge Hub는 로컬 LLM과 오픈소스 인프라 기반의 AI 지식 관리 플랫폼입니다.

이 프로젝트는 Next.js, Spring Boot, PostgreSQL, pgvector, MinIO, Ollama를 사용합니다.

문서 업로드 기능은 원본 파일을 MinIO에 저장하고 문서 메타데이터를 PostgreSQL에 저장합니다.

문서 처리 기능은 업로드된 문서에서 텍스트를 추출하고 chunk 단위로 나누어 document_chunks 테이블에 저장합니다.

임베딩 기능은 각 chunk를 Ollama의 nomic-embed-text 모델로 벡터화하고 PostgreSQL pgvector 컬럼에 저장합니다.

검색 기능은 사용자의 query를 embedding으로 변환한 뒤 pgvector similarity search로 관련 chunk를 찾습니다.
