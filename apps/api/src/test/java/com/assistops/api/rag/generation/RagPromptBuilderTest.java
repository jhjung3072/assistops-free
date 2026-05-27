package com.assistops.api.rag.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.assistops.api.prompt.DefaultPromptContent;
import com.assistops.api.prompt.PromptVersion;
import com.assistops.api.rag.search.ChunkSearchResult;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RagPromptBuilderTest {

	@Test
	void buildLimitsChunkAndTotalContextLength() {
		RagPromptBuilder builder = new RagPromptBuilder(new RagProperties(
			"llama3.2",
			3,
			1,
			8,
			10,
			15,
			256,
			0.2,
			0.9,
			"30m"
		));
		ChunkSearchResult first = result("abcdefghijklmnop");
		ChunkSearchResult second = result("qrstuvwxyz123456");

		RagPromptBuilder.RagPrompt prompt = builder.build("질문", List.of(first, second), defaultPromptVersion());

		assertThat(prompt.contextCharCount()).isEqualTo(15);
		assertThat(prompt.text()).contains("abcdefghij");
		assertThat(prompt.text()).contains("qrstu");
		assertThat(prompt.text()).doesNotContain("klmnop");
		assertThat(prompt.text()).doesNotContain("vwxyz");
	}

	private ChunkSearchResult result(String content) {
		return new ChunkSearchResult(
			UUID.randomUUID(),
			UUID.randomUUID(),
			"document.txt",
			UUID.randomUUID(),
			0,
			content,
			0.9,
			0.1,
			"nomic-embed-text"
		);
	}

	private PromptVersion defaultPromptVersion() {
		return new PromptVersion(
			UUID.randomUUID(),
			1,
			DefaultPromptContent.SYSTEM_PROMPT,
			DefaultPromptContent.USER_PROMPT_TEMPLATE,
			DefaultPromptContent.CONTEXT_TEMPLATE,
			null,
			UUID.randomUUID()
		);
	}
}
