package com.example.TriageIQ.Service.ai.AiOrchestrationService;

import com.example.TriageIQ.Service.ai.model.AiDecision;
import com.example.TriageIQ.Service.ai.model.ReplySuggestionResult;
import com.example.TriageIQ.Service.ai.model.TicketClassificationResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiOrchestrationService implements AiOrchestrationService{
    @Override
    public AiDecision<TicketClassificationResult> classifyTicket(
            String subject, String description, List<String> availableCategoryNames) {

        String firstCategory = availableCategoryNames.isEmpty() ? "General" : availableCategoryNames.get(0);

        return AiDecision.ok(TicketClassificationResult.builder()
                .categoryName(firstCategory)
                .suggestedPriority("MEDIUM")
                .confidenceScore(0.5)
                .reasoning("Mock classification — app.ai.provider is set to 'mock'.")
                .rawResponse("{\"mock\": true}")
                .build());
    }

    @Override
    public AiDecision<ReplySuggestionResult> suggestReply(
            String ticketSubject, List<String> conversationHistory, String latestCustomerMessage) {

        return AiDecision.ok(ReplySuggestionResult.builder()
                .suggestedText("Thanks for reaching out about \"" + ticketSubject
                        + "\". A member of our team will follow up shortly with more details.")
                .rawResponse("{\"mock\": true}")
                .build());
    }
}
