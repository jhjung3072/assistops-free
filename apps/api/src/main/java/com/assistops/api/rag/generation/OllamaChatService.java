package com.assistops.api.rag.generation;

import com.assistops.api.rag.search.ChunkSearchResult;
import java.util.List;
import java.util.function.Consumer;
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
	public RagGenerationResult generateAnswerStream(
		String question,
		List<ChunkSearchResult> sources,
		Consumer<String> tokenConsumer
	) {
		long promptBuildStart = System.nanoTime();
		RagPromptBuilder.RagPrompt prompt = ragPromptBuilder.build(question, sources);
		long promptBuildMs = elapsedMs(promptBuildStart);

		if (!prompt.hasContext()) {
			String answer = "제공된 문서만으로는 답변하기 어렵습니다.";
			tokenConsumer.accept(answer);
			return new RagGenerationResult(answer, promptBuildMs, 0, 0);
		}

		try {
			StringBuilder answer = new StringBuilder();
			long chatGenerationStart = System.nanoTime();

			chatModel.stream(new Prompt(prompt.text(), chatOptions()))
				.map(this::extractText)
				.filter(StringUtils::hasText)
				.doOnNext(token -> {
					answer.append(token);
					tokenConsumer.accept(token);
				})
				.blockLast();

			long chatGenerationMs = elapsedMs(chatGenerationStart);
			String normalizedAnswer = answer.toString().trim();

			if (!StringUtils.hasText(normalizedAnswer)) {
				throw new RagGenerationException("RAG answer result is empty.");
			}

			return new RagGenerationResult(
				normalizedAnswer,
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
				"Failed to stream RAG answer with Ollama. Check that Ollama is running and the chat model is pulled.",
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

	private String extractText(ChatResponse response) {
		Generation result = response.getResult();
		return result == null || result.getOutput() == null
			? null
			: result.getOutput().getText();
	}

	private long elapsedMs(long startedAt) {
		return (System.nanoTime() - startedAt) / 1_000_000L;
	}
}
