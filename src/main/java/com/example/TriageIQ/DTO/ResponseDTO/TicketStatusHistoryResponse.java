package com.example.TriageIQ.DTO.ResponseDTO;

import com.example.TriageIQ.Entity.Enum.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusHistoryResponse {
    private Long id;

    private TicketStatus oldStatus;

    private TicketStatus newStatus;

    private UserSummaryResponse changedBy;

    private String reason;

    private LocalDateTime changedAt;
}
