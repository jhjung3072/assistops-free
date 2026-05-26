package com.assistops.api.rag.generation;

import com.assistops.api.rag.search.ChunkSearchResult;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RagPromptBuilder {

	private static final String INSUFFICIENT_CONTEXT_ANSWER = "제공된 문서만으로는 답변하기 어렵습니다.";

	private final RagProperties properties;

	public RagPromptBuilder(RagProperties properties) {
		this.properties = properties;
	}

	public RagPrompt build(String question, List<ChunkSearchResult> sources) {
		StringBuilder builder = new StringBuilder();

		builder.append("""
			답변 규칙:
			- 제공된 Context만 근거로 한국어로 답하세요.
			- 추측하지 마세요.
			- 근거가 부족하면 정확히 "제공된 문서만으로는 답변하기 어렵습니다."라고 답하세요.

			Context:
			""");

		int contextCharCount = 0;
		int remainingContextChars = properties.contextTotalMaxChars();

		for (int index = 0; index < sources.size(); index++) {
			if (remainingContextChars <= 0) {
				break;
			}

			ChunkSearchResult source = sources.get(index);
			String content = limitedContent(source.content(), remainingContextChars);
			if (!StringUtils.hasText(content)) {
				continue;
			}

			contextCharCount += content.length();
			remainingContextChars -= content.length();

			builder.append("\n[")
				.append(index + 1)
				.append("] ")
				.append(source.documentName())
				.append(" / chunkIndex=")
				.append(source.chunkIndex())
				.append("\n")
				.append(content)
				.append('\n');
		}

		builder.append("\nQuestion:\n")
			.append(question)
			.append("\n\nAnswer:");

		return new RagPrompt(builder.toString(), contextCharCount);
	}

	private String limitedContent(String content, int remainingContextChars) {
		if (!StringUtils.hasText(content) || remainingContextChars <= 0) {
			return "";
		}

		int limit = Math.min(properties.contextChunkMaxChars(), remainingContextChars);
		String normalized = content.trim();

		if (normalized.length() <= limit) {
			return normalized;
		}

		return normalized.substring(0, limit).trim();
	}

	public record RagPrompt(String text, int contextCharCount) {

		public boolean hasContext() {
			return contextCharCount > 0;
		}
	}
}
