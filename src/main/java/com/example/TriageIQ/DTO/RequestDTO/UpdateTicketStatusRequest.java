package com.example.TriageIQ.DTO.RequestDTO;

import com.example.TriageIQ.Entity.Enum.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketStatusRequest {
    @NotNull(message = "Status is required")
    private TicketStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
