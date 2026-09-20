package com.example.TriageIQ.Service.ai.AiOrchestrationService;

import com.example.TriageIQ.Service.ai.model.AiDecision;
import com.example.TriageIQ.Service.ai.model.ReplySuggestionResult;
import com.example.TriageIQ.Service.ai.model.TicketClassificationResult;

import java.util.List;

public interface AiOrchestrationService {
    AiDecision<TicketClassificationResult> classifyTicket(
            String subject, String description, List<String> availableCategoryNames);

    AiDecision<ReplySuggestionResult> suggestReply(
            String ticketSubject, List<String> conversationHistory, String latestCustomerMessage);
}
