package com.example.TriageIQ.DTO.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Name must be under 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;
}
