package com.assistops.api.rag.generation;

import com.assistops.api.rag.search.ChunkSearchResult;
import java.util.List;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OllamaChatService implements RagGenerationService {

	private final ChatModel chatModel;
	private final RagPromptBuilder ragPromptBuilder;
	private final RagProperties properties;

	public OllamaChatService(ChatModel chatModel, RagPromptBuilder ragPromptBuilder, RagProperties properties) {
		this.chatModel = chatModel;
		this.ragPromptBuilder = ragPromptBuilder;
		this.properties = properties;
	}

	@Override
	public RagGenerationResult generateAnswer(String question, List<ChunkSearchResult> sources) {
		long promptBuildStart = System.nanoTime();
		RagPromptBuilder.RagPrompt prompt = ragPromptBuilder.build(question, sources);
		long promptBuildMs = elapsedMs(promptBuildStart);

		if (!prompt.hasContext()) {
			return new RagGenerationResult(
				"제공된 문서만으로는 답변하기 어렵습니다.",
				promptBuildMs,
				0,
				0
			);
		}

		try {
			long chatGenerationStart = System.nanoTime();
			ChatResponse response = chatModel.call(new Prompt(prompt.text(), chatOptions()));
			long chatGenerationMs = elapsedMs(chatGenerationStart);
			Generation result = response.getResult();
			String answer = result == null || result.getOutput() == null
				? null
				: result.getOutput().getText();

			if (!StringUtils.hasText(answer)) {
				throw new RagGenerationException("RAG answer result is empty.");
			}

			return new RagGenerationResult(
				answer.trim(),
				promptBuildMs,
				chatGenerationMs,
				prompt.contextCharCount()
			);
		}
		catch (RagGenerationException exception) {
			throw exception;
		}
		catch (RuntimeException exception) {
			throw new RagGenerationException(
				"Failed to generate RAG answer with Ollama. Check that Ollama is running and the chat model is pulled.",
				exception
			);
		}
	}

	@Override
	public String modelName() {
		return properties.chatModel();
	}

	private OllamaChatOptions chatOptions() {
		return OllamaChatOptions.builder()
			.model(properties.chatModel())
			.numPredict(properties.numPredict())
			.temperature(properties.temperature())
			.topP(properties.topP())
			.keepAlive(properties.keepAlive())
			.build();
	}

	private long elapsedMs(long startedAt) {
		return (System.nanoTime() - startedAt) / 1_000_000L;
	}
}
