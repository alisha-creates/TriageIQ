package com.example.TriageIQ.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCategoryResponse {
    private Long id;

    private String name;

    private String description;

    private Boolean active;

    private Long departmentId;

    private String departmentName;
}
