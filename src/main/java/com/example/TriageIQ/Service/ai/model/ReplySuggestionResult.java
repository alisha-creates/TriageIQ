package com.example.TriageIQ.Service.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplySuggestionResult {
    private String suggestedText;

    private String rawResponse;
}
