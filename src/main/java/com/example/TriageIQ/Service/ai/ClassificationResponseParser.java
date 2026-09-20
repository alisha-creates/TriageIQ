package com.example.TriageIQ.Service.ai;

import com.example.TriageIQ.Exception.InvalidAiResponseException;
import com.example.TriageIQ.Service.ai.model.TicketClassificationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ClassificationResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicketClassificationResult parse(String rawResponse) {
        String cleaned = stripCodeFences(rawResponse);

        JsonNode node;
        try {
            node = objectMapper.readTree(cleaned);
        } catch (Exception e) {
            throw new InvalidAiResponseException("AI response was not valid JSON: " + rawResponse, e);
        }

        if (!node.has("category") || !node.has("priority")) {
            throw new InvalidAiResponseException("AI response missing required fields: " + rawResponse);
        }

        return TicketClassificationResult.builder()
                .categoryName(node.get("category").asText())
                .suggestedPriority(node.get("priority").asText())
                .confidenceScore(node.has("confidence") ? node.get("confidence").asDouble() : 0.5)
                .reasoning(node.has("reasoning") ? node.get("reasoning").asText() : null)
                .rawResponse(rawResponse)
                .build();
    }

    private String stripCodeFences(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }
}
