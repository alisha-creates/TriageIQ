package com.example.TriageIQ.Service.ai;

import java.util.List;

public class PromptTemplates {
    public static String classificationSystemPrompt() {
        return """
                You are a support ticket triage assistant. Classify the ticket into
                exactly one of the provided categories, and suggest a priority.

                Respond with ONLY valid JSON, no markdown, no code fences, no extra text:
                {
                  "category": "<one of the provided category names, exactly as given>",
                  "priority": "<LOW|MEDIUM|HIGH|URGENT>",
                  "confidence": <number between 0.0 and 1.0>,
                  "reasoning": "<one sentence explaining the decision>"
                }

                If none of the categories fit well, still pick the closest one and
                lower the confidence score accordingly.
                """;
    }

    public static String classificationUserPrompt(
            String subject, String description, List<String> availableCategories) {
        return """
                Subject: %s
                Description: %s

                Available categories: %s
                """.formatted(subject, description, String.join(", ", availableCategories));
    }

    public static String replySuggestionSystemPrompt() {
        return """
                You are a helpful, professional customer support agent. Draft a reply
                to the customer's latest message, using the conversation history for
                context. Be concise, empathetic, and specific to their issue. Do not
                make promises about timelines or outcomes you cannot verify. Respond
                with plain text only — no JSON, no markdown formatting.
                """;
    }

    public static String replySuggestionUserPrompt(
            String ticketSubject, List<String> conversationHistory, String latestCustomerMessage) {
        return """
                Ticket subject: %s

                Conversation so far:
                %s

                Latest customer message to respond to:
                %s
                """.formatted(ticketSubject, String.join("\n", conversationHistory), latestCustomerMessage);
    }
}
