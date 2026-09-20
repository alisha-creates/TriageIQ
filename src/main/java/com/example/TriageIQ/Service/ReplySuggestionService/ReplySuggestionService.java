package com.example.TriageIQ.Service.ReplySuggestionService;

import com.example.TriageIQ.DTO.RequestDTO.EditAiReplyRequest;
import com.example.TriageIQ.DTO.ResponseDTO.AiReplySuggestionResponse;

public interface ReplySuggestionService {
    AiReplySuggestionResponse generateSuggestion(Long ticketId);

    AiReplySuggestionResponse getLatestPending(Long ticketId);

    AiReplySuggestionResponse editSuggestion(Long suggestionId, EditAiReplyRequest request, Long reviewedByUserId);

    void acceptAndSend(Long suggestionId, Long reviewedByUserId);

    void reject(Long suggestionId, Long reviewedByUserId);
}
