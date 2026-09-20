package com.example.TriageIQ.Service.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketClassificationResult {
    private String categoryName;

    private String suggestedPriority;

    private Double confidenceScore;

    private String reasoning;

    private String rawResponse;
}
