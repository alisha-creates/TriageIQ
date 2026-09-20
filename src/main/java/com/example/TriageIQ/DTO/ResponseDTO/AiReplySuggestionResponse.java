package com.example.TriageIQ.DTO.ResponseDTO;

import com.example.TriageIQ.Entity.Enum.SuggestionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReplySuggestionResponse {
    private Long id;

    private Long ticketId;

    private String suggestedReply;

    private String editedReply;

    private SuggestionStatus status;

    private UserSummaryResponse reviewedBy;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;
}
