package com.example.TriageIQ.DTO.ResponseDTO;

import com.example.TriageIQ.Entity.Enum.Priority;
import com.example.TriageIQ.Entity.Enum.RoutingDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketClassificationResponse {
    private Long id;

    private TicketCategoryResponse category;

    private Priority suggestedPriority;

    private RoutingDecision routingDecision;

    private Double confidenceScore;

    private String reasoning;

    private UserSummaryResponse reviewedBy;

    private LocalDateTime classifiedAt;
}
