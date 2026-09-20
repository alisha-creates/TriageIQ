package com.example.TriageIQ.Service.UserService;

import com.example.TriageIQ.DTO.RequestDTO.ChangePasswordRequest;
import com.example.TriageIQ.DTO.RequestDTO.CreateStaffRequest;
import com.example.TriageIQ.DTO.RequestDTO.LoginRequest;
import com.example.TriageIQ.DTO.RequestDTO.RegisterRequest;
import com.example.TriageIQ.DTO.ResponseDTO.AuthResponse;
import com.example.TriageIQ.DTO.ResponseDTO.RegistrationResponse;
import com.example.TriageIQ.DTO.ResponseDTO.UserSummaryResponse;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.User;

public interface UserService {
    RegistrationResponse register(RegisterRequest request);

    RegistrationResponse verifyEmail(String token);

    User createStaff(CreateStaffRequest request, Role role);

    RegistrationResponse bootstrapAdmin(RegisterRequest request, String providedSetupKey);

    AuthResponse login(LoginRequest request, Role expectedRole);

    void changePassword(Long userId, ChangePasswordRequest request);

    User createOrGetGoogleUser(String name, String email, String providerId);

    UserSummaryResponse getCurrentUserSummary(Long userId);

    void setEnabled(Long userId, boolean enabled);

    void assignDepartment(Long userId, Long departmentId);
}
