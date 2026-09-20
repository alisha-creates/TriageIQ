package com.example.TriageIQ.DTO.ResponseDTO;

import com.example.TriageIQ.Entity.Enum.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;

    private Long ticketId;

    private UserSummaryResponse sender;

    private SenderType senderType;

    private String content;

    private LocalDateTime sentAt;
}
