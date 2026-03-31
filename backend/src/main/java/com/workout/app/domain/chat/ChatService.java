package com.workout.app.domain.chat;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String SYSTEM_PROMPT = """
            당신은 전문 헬스 트레이너입니다.
            운동 방법, 자세, 루틴, 영양, 부상 예방 등 운동 관련 질문에만 답변하세요.
            운동과 관련 없는 질문은 정중히 거절하세요.
            답변은 친절하고 명확하게 한국어로 해주세요.
            """;

    public String chat(String userMessage) {

        WebClient webClient = WebClient.create("https://generativelanguage.googleapis.com");

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))
                ),
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", userMessage)))
                )
        );

        Map response = webClient.post()
                .uri("/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map> candidates = (List<Map>) response.get("candidates");
        Map content = (Map) candidates.get(0).get("content");
        List<Map> parts = (List<Map>) content.get("parts");
        return (String) parts.get(0).get("text");
    }
}
