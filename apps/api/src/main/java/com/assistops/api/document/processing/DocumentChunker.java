package com.assistops.api.document.processing;

import com.assistops.api.global.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DocumentChunker {

	private static final int CHUNK_SIZE = 1_000;
	private static final int CHUNK_OVERLAP = 150;

	public List<Chunk> chunk(String text) {
		if (!StringUtils.hasText(text)) {
			throw new BadRequestException("Document text is empty.");
		}

		String normalizedText = text.trim();
		List<Chunk> chunks = new ArrayList<>();
		int start = 0;

		while (start < normalizedText.length()) {
			int end = Math.min(start + CHUNK_SIZE, normalizedText.length());

			if (end < normalizedText.length()) {
				end = findNaturalBoundary(normalizedText, start, end);
			}

			String content = normalizedText.substring(start, end).trim();

			if (StringUtils.hasText(content)) {
				chunks.add(new Chunk(chunks.size(), content, estimateTokenCount(content)));
			}

			if (end >= normalizedText.length()) {
				break;
			}

			start = Math.max(0, end - CHUNK_OVERLAP);

			while (start < normalizedText.length() && Character.isWhitespace(normalizedText.charAt(start))) {
				start++;
			}
		}

		if (chunks.isEmpty()) {
			throw new BadRequestException("No chunks were generated from document text.");
		}

		return chunks;
	}

	private int findNaturalBoundary(String text, int start, int fallbackEnd) {
		int minimumEnd = start + (CHUNK_SIZE / 2);
		int paragraphBreak = text.lastIndexOf("\n\n", fallbackEnd);

		if (paragraphBreak >= minimumEnd) {
			return paragraphBreak;
		}

		int lineBreak = text.lastIndexOf('\n', fallbackEnd);

		if (lineBreak >= minimumEnd) {
			return lineBreak;
		}

		int sentenceBreak = Math.max(
			Math.max(text.lastIndexOf(". ", fallbackEnd), text.lastIndexOf("? ", fallbackEnd)),
			text.lastIndexOf("! ", fallbackEnd)
		);

		if (sentenceBreak >= minimumEnd) {
			return sentenceBreak + 1;
		}

		return fallbackEnd;
	}

	private int estimateTokenCount(String content) {
		// 정확한 tokenizer는 다음 embedding 단계에서 도입한다. 현재는 문자 수 / 4로 대략 추정한다.
		return Math.max(1, (int) Math.ceil(content.length() / 4.0));
	}

	public record Chunk(
		int chunkIndex,
		String content,
		int tokenCount
	) {
	}
}
