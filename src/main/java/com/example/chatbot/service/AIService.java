package com.example.chatbot.service;

import com.example.chatbot.model.Message;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.ThinkingLevel;

import java.util.List;

// Google GenAI SDK 호출을 담당하는 서비스이다.
// API 키는 실행 환경의 GEMINI_API_KEY 환경변수에서 읽는다.
public class AIService {
    public static final List<String> ALLOWED_MODELS = List.of(
            "gemma-4-26b-a4b-it",
            "gemma-4-31b-it",
            "gemini-3.1-flash-lite"
    );
    public static final String DEFAULT_MODEL = ALLOWED_MODELS.get(0);

    private static final String API_KEY_NAME = "GEMINI_API_KEY";
    private static final int MAX_OUTPUT_TOKENS = 512;
    private static final int MAX_HISTORY_MESSAGES = 10;
    private static final String SYSTEM_INSTRUCTION = "답변은 핵심만 간결하게 작성한다. 필요한 경우 짧은 목록을 사용하되 전체 답변은 너무 길어지지 않게 제한한다.";

    public String answer(String model, List<Message> messages) {
        String selectedModel = normalizeModel(model);
        String apiKey = readApiKey();

        try (Client client = Client.builder().apiKey(apiKey).build()) {
            GenerateContentResponse response = client.models.generateContent(selectedModel, recentContents(messages), generationConfig());
            String text = response.text();
            if (text == null || text.isBlank()) {
                return "응답 내용이 비어 있습니다.";
            }
            return text.trim();
        }
    }

    public String normalizeModel(String model) {
        if (model != null && ALLOWED_MODELS.contains(model)) {
            return model;
        }
        return DEFAULT_MODEL;
    }

    private GenerateContentConfig generationConfig() {
        return GenerateContentConfig.builder()
                .maxOutputTokens(MAX_OUTPUT_TOKENS)
                .thinkingConfig(ThinkingConfig.builder()
                        .includeThoughts(false)
                        .thinkingLevel(ThinkingLevel.Known.MINIMAL)
                        .build())
                .systemInstruction(Content.builder()
                        .parts(Part.builder().text(SYSTEM_INSTRUCTION).build())
                        .build())
                .build();
    }

    private List<Content> recentContents(List<Message> messages) {
        int fromIndex = Math.max(0, messages.size() - MAX_HISTORY_MESSAGES);
        List<Message> recentMessages = messages.subList(fromIndex, messages.size());
        if (!recentMessages.isEmpty() && !"user".equals(recentMessages.get(0).role())) {
            recentMessages = recentMessages.subList(1, recentMessages.size());
        }

        return recentMessages.stream()
                .map(this::toContent)
                .toList();
    }

    private Content toContent(Message message) {
        String role = "user".equals(message.role()) ? "user" : "model";
        return Content.builder()
                .role(role)
                .parts(Part.builder().text(message.text()).build())
                .build();
    }

    private String readApiKey() {
        String envValue = System.getenv(API_KEY_NAME);
        if (envValue == null || envValue.isBlank()) {
            throw new IllegalStateException(API_KEY_NAME + " 환경변수를 설정하세요.");
        }
        return envValue.trim();
    }
}
