package com.example.TriageIQ.Service.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDecision<T> {
    private boolean success;
    private T result;
    private String errorMessage;

    public static <T> AiDecision<T> ok(T result) {
        return AiDecision.<T>builder().success(true).result(result).build();
    }

    public static <T> AiDecision<T> failure(String errorMessage) {
        return AiDecision.<T>builder().success(false).errorMessage(errorMessage).build();
    }
}
