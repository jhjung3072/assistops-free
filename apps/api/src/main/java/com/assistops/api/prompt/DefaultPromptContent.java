package com.assistops.api.prompt;

public final class DefaultPromptContent {

	public static final String SYSTEM_PROMPT = """
		답변 규칙:
		- 제공된 Context만 근거로 {{language}}로 답하세요.
		- 추측하지 마세요.
		- 근거가 부족하면 정확히 "제공된 문서만으로는 답변하기 어렵습니다."라고 답하세요.
		""";

	public static final String USER_PROMPT_TEMPLATE = """
		Context:
		{{context}}

		Question:
		{{question}}

		Answer:
		""";

	public static final String CONTEXT_TEMPLATE = """
		[{{index}}] {{documentName}} / chunkIndex={{chunkIndex}}
		{{content}}
		""";

	private DefaultPromptContent() {
	}

	public static String defaultName(PromptType type) {
		return switch (type) {
			case RAG_ANSWER -> "Default RAG Answer Prompt";
			case AGENT_CHAT -> "Default Agent Chat Prompt";
		};
	}

	public static String defaultDescription(PromptType type) {
		return switch (type) {
			case RAG_ANSWER -> "RAG Q&A에서 사용하는 기본 문서 근거 답변 prompt입니다.";
			case AGENT_CHAT -> "Agent Chat에서 사용하는 기본 문서 근거 답변 prompt입니다.";
		};
	}
}
