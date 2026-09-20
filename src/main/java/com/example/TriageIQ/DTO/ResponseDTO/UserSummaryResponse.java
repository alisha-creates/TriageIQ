package com.example.TriageIQ.DTO.ResponseDTO;

import com.example.TriageIQ.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private Long id;

    private String name;

    private String email;

    private Role role;

    private String avatarUrl;

    private Boolean mustChangePassword;
}
