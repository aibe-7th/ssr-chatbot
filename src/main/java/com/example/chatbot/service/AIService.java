package com.example.chatbot.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.ThinkingLevel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// Google GenAI SDK 호출을 담당하는 서비스이다.
// API 키는 환경변수를 우선 사용하고, 없으면 프로젝트의 .env 파일에서 읽는다.
public class AIService {
    public static final List<String> ALLOWED_MODELS = List.of(
            "gemma-4-26b-a4b-it",
            "gemma-4-31b-it",
            "gemini-3.1-flash-lite"
    );
    public static final String DEFAULT_MODEL = ALLOWED_MODELS.get(0);

    private static final String API_KEY_NAME = "GEMINI_API_KEY";
    private static final String API_KEY_PLACEHOLDER = "your_api_key_here";
    private static final int MAX_OUTPUT_TOKENS = 512;
    private static final String SYSTEM_INSTRUCTION = "답변은 핵심만 간결하게 작성한다. 필요한 경우 짧은 목록을 사용하되 전체 답변은 너무 길어지지 않게 제한한다.";

    public String answer(String model, String prompt) throws IOException {
        String selectedModel = normalizeModel(model);
        String apiKey = readApiKey()
                .orElseThrow(() -> new IllegalStateException(API_KEY_NAME + "가 설정되지 않았습니다. .env 또는 환경변수에 API 키를 입력하세요."));

        try (Client client = Client.builder().apiKey(apiKey).build()) {
            GenerateContentResponse response = client.models.generateContent(selectedModel, prompt, generationConfig());
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

    private Optional<String> readApiKey() throws IOException {
        String envValue = System.getenv(API_KEY_NAME);
        if (hasApiKey(envValue)) {
            return Optional.of(envValue.trim());
        }

        Optional<Path> envPath = findDotEnv();
        if (envPath.isEmpty()) {
            return Optional.empty();
        }

        for (String line : Files.readAllLines(envPath.get())) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            int separatorIndex = trimmed.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }

            String key = trimmed.substring(0, separatorIndex).trim();
            String value = trimmed.substring(separatorIndex + 1).trim();
            if (API_KEY_NAME.equals(key) && hasApiKey(value)) {
                return Optional.of(stripQuotes(value));
            }
        }

        return Optional.empty();
    }

    private Optional<Path> findDotEnv() {
        Optional<Path> fromWorkingDirectory = findDotEnvFrom(Path.of(System.getProperty("user.dir")));
        if (fromWorkingDirectory.isPresent()) {
            return fromWorkingDirectory;
        }

        try {
            Path classpath = Path.of(AIService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return findDotEnvFrom(classpath);
        } catch (URISyntaxException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<Path> findDotEnvFrom(Path start) {
        Path current = Files.isRegularFile(start) ? start.getParent() : start;
        while (current != null) {
            Path candidate = current.resolve(".env");
            if (Files.isRegularFile(candidate)) {
                return Optional.of(candidate);
            }
            current = current.getParent();
        }
        return Optional.empty();
    }

    private boolean hasApiKey(String value) {
        if (value == null) {
            return false;
        }
        String normalized = stripQuotes(value.trim());
        return !normalized.isBlank() && !API_KEY_PLACEHOLDER.equals(normalized);
    }

    private String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
