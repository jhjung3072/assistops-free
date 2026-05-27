# AI Knowledge Hub 테스트 문서

AI Knowledge Hub는 유료 AI API 없이 로컬 LLM과 오픈소스 인프라를 사용하는 AI 지식 관리 플랫폼입니다.

이 프로젝트의 프론트엔드는 Next.js App Router, React, TypeScript, Tailwind CSS, shadcn/ui를 사용합니다.

백엔드는 Java 21과 Spring Boot를 사용하며, 인증은 JWT Bearer Token 기반입니다.

원본 문서 파일은 MinIO에 저장되고, 문서 메타데이터와 chunk 데이터는 PostgreSQL에 저장됩니다.

pgvector는 문서 chunk의 embedding vector를 저장하고 유사도 검색을 수행하는 역할을 합니다.

Ollama는 로컬에서 embedding model과 chat model을 실행합니다.

RAG 기능은 사용자의 질문을 embedding으로 변환하고, 관련 문서 chunk를 검색한 뒤, 검색된 chunk를 context로 사용해 답변을 생성합니다.

현재 AI Knowledge Hub는 문서 업로드, 텍스트 추출, chunking, embedding, semantic search, RAG answer generation을 목표로 합니다.
