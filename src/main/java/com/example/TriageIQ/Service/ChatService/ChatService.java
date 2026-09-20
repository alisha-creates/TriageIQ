package com.example.TriageIQ.Service.ChatService;

import com.example.TriageIQ.DTO.RequestDTO.SendMessageRequest;
import com.example.TriageIQ.DTO.ResponseDTO.ChatMessageResponse;
import com.example.TriageIQ.Entity.Enum.Role;

import java.util.List;

public interface ChatService {
    ChatMessageResponse sendMessage(
            Long ticketId, SendMessageRequest request, Long senderUserId, Role senderRole);

    List<ChatMessageResponse> getMessages(Long ticketId, Long requestingUserId, Role requestingUserRole);

    ChatMessageResponse postSystemMessage(Long ticketId, String content);
}
