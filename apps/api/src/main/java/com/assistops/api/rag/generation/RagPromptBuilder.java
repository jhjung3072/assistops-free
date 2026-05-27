package com.assistops.api.rag.generation;

import com.assistops.api.prompt.DefaultPromptContent;
import com.assistops.api.prompt.PromptVersion;
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

	public RagPrompt build(String question, List<ChunkSearchResult> sources, PromptVersion promptVersion) {
		ContextBuildResult context = buildContext(sources, promptVersion);
		String systemPrompt = replaceCommonPlaceholders(
			defaultIfBlank(promptVersion.getSystemPrompt(), DefaultPromptContent.SYSTEM_PROMPT),
			question,
			context.text()
		);
		String userPromptTemplate = defaultIfBlank(
			promptVersion.getUserPromptTemplate(),
			DefaultPromptContent.USER_PROMPT_TEMPLATE
		);
		String userPrompt = replaceCommonPlaceholders(userPromptTemplate, question, context.text());

		return new RagPrompt(systemPrompt.trim() + "\n\n" + userPrompt.trim(), context.contextCharCount());
	}

	private ContextBuildResult buildContext(List<ChunkSearchResult> sources, PromptVersion promptVersion) {
		String contextTemplate = defaultIfBlank(
			promptVersion.getContextTemplate(),
			DefaultPromptContent.CONTEXT_TEMPLATE
		);
		StringBuilder contextBuilder = new StringBuilder();
		int remainingContextChars = properties.contextTotalMaxChars();
		int contextCharCount = 0;

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

			contextBuilder.append(renderContext(contextTemplate, source, index + 1, content))
				.append('\n');
		}

		return new ContextBuildResult(contextBuilder.toString().trim(), contextCharCount);
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

	private String renderContext(String template, ChunkSearchResult source, int index, String content) {
		return template
			.replace("{{index}}", String.valueOf(index))
			.replace("{{documentName}}", source.documentName())
			.replace("{{chunkIndex}}", String.valueOf(source.chunkIndex()))
			.replace("{{content}}", content)
			.trim();
	}

	private String replaceCommonPlaceholders(String template, String question, String context) {
		return template
			.replace("{{context}}", context)
			.replace("{{question}}", question)
			.replace("{{language}}", "한국어");
	}

	private String defaultIfBlank(String value, String fallback) {
		return StringUtils.hasText(value) ? value : fallback;
	}

	public record RagPrompt(String text, int contextCharCount) {

		public boolean hasContext() {
			return contextCharCount > 0;
		}
	}

	private record ContextBuildResult(String text, int contextCharCount) {
	}
}
