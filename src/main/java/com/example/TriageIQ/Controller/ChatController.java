package com.example.TriageIQ.Controller;

import com.example.TriageIQ.DTO.RequestDTO.SendMessageRequest;
import com.example.TriageIQ.DTO.ResponseDTO.ChatMessageResponse;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Security.CurrentUserProvider;
import com.example.TriageIQ.Service.ChatService.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/{ticketId}/messages")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long ticketId) {
        User user = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(chatService.getMessages(ticketId, user.getId(), user.getRole()));
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessageRest(
            @PathVariable Long ticketId, @Valid @RequestBody SendMessageRequest request) {
        User user = currentUserProvider.getCurrentUser();
        ChatMessageResponse response = chatService.sendMessage(ticketId, request, user.getId(), user.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @MessageMapping("/chat.send/{ticketId}")
    public void sendMessageStomp(
            @DestinationVariable Long ticketId,
            @Payload SendMessageRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = currentUserProvider.getCurrentUser();
        chatService.sendMessage(ticketId, request, user.getId(), user.getRole());
    }
}
