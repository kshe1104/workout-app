package com.workout.app.domain.chat;

import com.workout.app.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@AuthenticationPrincipal CustomUserPrincipal user, @RequestBody ChatRequest request) {
        String reply = chatService.chat(request.message());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    public record ChatRequest(String message){}
    public record ChatResponse(String reply){}
}
