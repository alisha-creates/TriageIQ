package com.example.TriageIQ.Validation;

import com.example.TriageIQ.Entity.Enum.Priority;
import com.example.TriageIQ.Exception.InvalidAiResponseException;
import com.example.TriageIQ.Service.ai.model.ReplySuggestionResult;
import com.example.TriageIQ.Service.ai.model.TicketClassificationResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiResponseValidator {
    private static final int MAX_REPLY_LENGTH = 3000;
    private static final int MIN_REPLY_LENGTH = 5;

    public void validateClassification(TicketClassificationResult result, List<String> availableCategoryNames) {
        if (result.getCategoryName() == null || result.getCategoryName().isBlank()) {
            throw new InvalidAiResponseException("AI classification is missing a category");
        }

        boolean categoryIsValid = availableCategoryNames.stream()
                .anyMatch(c -> c.equalsIgnoreCase(result.getCategoryName()));
        if (!categoryIsValid) {
            throw new InvalidAiResponseException(
                    "AI returned a category not in the allowed list: " + result.getCategoryName());
        }

        if (result.getConfidenceScore() == null
                || result.getConfidenceScore() < 0.0
                || result.getConfidenceScore() > 1.0) {
            throw new InvalidAiResponseException(
                    "AI confidence score out of range: " + result.getConfidenceScore());
        }

        if (!isValidPriority(result.getSuggestedPriority())) {
            throw new InvalidAiResponseException(
                    "AI returned an invalid priority: " + result.getSuggestedPriority());
        }
    }

    public void validateReplySuggestion(ReplySuggestionResult result) {
        String text = result.getSuggestedText();

        if (text == null || text.isBlank()) {
            throw new InvalidAiResponseException("AI reply suggestion is empty");
        }

        if (text.length() < MIN_REPLY_LENGTH) {
            throw new InvalidAiResponseException("AI reply suggestion is too short to be useful");
        }
        if (text.length() > MAX_REPLY_LENGTH) {
            throw new InvalidAiResponseException(
                    "AI reply suggestion exceeds maximum length (" + MAX_REPLY_LENGTH + " chars)");
        }
    }

    private boolean isValidPriority(String value) {
        if (value == null) return false;
        try {
            Priority.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
