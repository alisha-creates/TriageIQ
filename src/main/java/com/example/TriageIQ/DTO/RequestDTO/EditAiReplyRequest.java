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
public class EditAiReplyRequest {
    @NotBlank(message = "Reply content is required")
    @Size(max = 10000,
            message = "Reply must not exceed 10000 characters")
    private String editedReply;
}
