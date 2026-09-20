package com.example.TriageIQ.Service.TicketClassificationService;

import com.example.TriageIQ.DTO.ResponseDTO.TicketClassificationResponse;

public interface TicketClassificationService {
    TicketClassificationResponse classifyTicket(Long ticketId);

    TicketClassificationResponse getClassification(Long ticketId);

    TicketClassificationResponse overrideClassification(
            Long ticketId, Long categoryId, String priority, Long reviewedByUserId);
}
