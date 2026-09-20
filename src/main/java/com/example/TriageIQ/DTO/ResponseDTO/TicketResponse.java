package com.example.TriageIQ.DTO.ResponseDTO;

import com.example.TriageIQ.Entity.Enum.Priority;
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
public class TicketResponse {
    private Long id;

    private String subject;

    private String description;

    private TicketStatus status;

    private Priority priority;

    private UserSummaryResponse customer;

    private UserSummaryResponse assignedAgent;

    private DepartmentResponse assignedDepartment;

    private TicketCategoryResponse category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;
}
