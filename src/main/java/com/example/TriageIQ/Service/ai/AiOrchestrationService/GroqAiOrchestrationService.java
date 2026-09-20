package com.example.TriageIQ.Service.ai.AiOrchestrationService;

import com.example.TriageIQ.Exception.AiServiceException;
import com.example.TriageIQ.Exception.InvalidAiResponseException;
import com.example.TriageIQ.Service.ai.ClassificationResponseParser;
import com.example.TriageIQ.Service.ai.PromptTemplates;
import com.example.TriageIQ.Service.ai.model.AiDecision;
import com.example.TriageIQ.Service.ai.model.ReplySuggestionResult;
import com.example.TriageIQ.Service.ai.model.TicketClassificationResult;
import com.example.TriageIQ.Validation.AiResponseValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "groq")
public class GroqAiOrchestrationService implements AiOrchestrationService{
    private final ChatClient chatClient;
    private final ClassificationResponseParser classificationParser;
    private final AiResponseValidator responseValidator;

    @Override
    public AiDecision<TicketClassificationResult> classifyTicket(
            String subject, String description, List<String> availableCategoryNames) {

        if (availableCategoryNames.isEmpty()) {
            return AiDecision.failure("No active categories available to classify into");
        }

        try {
            String rawResponse = chatClient.prompt()
                    .system(PromptTemplates.classificationSystemPrompt())
                    .user(PromptTemplates.classificationUserPrompt(subject, description, availableCategoryNames))
                    .call()
                    .content();

            TicketClassificationResult result = classificationParser.parse(rawResponse);
            responseValidator.validateClassification(result, availableCategoryNames);

            return AiDecision.ok(result);

        } catch (InvalidAiResponseException e) {
            log.warn("AI returned an invalid classification response: {}", e.getMessage());
            return AiDecision.failure("AI response could not be validated: " + e.getMessage());

        } catch (Exception e) {
            log.error("AI classification call failed", e);
            throw new AiServiceException("Failed to reach AI classification service", e);
        }
    }

    @Override
    public AiDecision<ReplySuggestionResult> suggestReply(
            String ticketSubject, List<String> conversationHistory, String latestCustomerMessage) {

        try {
            String rawResponse = chatClient.prompt()
                    .system(PromptTemplates.replySuggestionSystemPrompt())
                    .user(PromptTemplates.replySuggestionUserPrompt(
                            ticketSubject, conversationHistory, latestCustomerMessage))
                    .call()
                    .content();

            ReplySuggestionResult result = ReplySuggestionResult.builder()
                    .suggestedText(rawResponse != null ? rawResponse.trim() : null)
                    .rawResponse(rawResponse)
                    .build();

            responseValidator.validateReplySuggestion(result);

            return AiDecision.ok(result);

        } catch (InvalidAiResponseException e) {
            log.warn("AI returned an invalid reply suggestion: {}", e.getMessage());
            return AiDecision.failure("AI response could not be validated: " + e.getMessage());

        } catch (Exception e) {
            log.error("AI reply suggestion call failed", e);
            throw new AiServiceException("Failed to reach AI reply suggestion service", e);
        }
    }
}
