package com.example.TriageIQ.Controller;

import com.example.TriageIQ.DTO.RequestDTO.ChangePasswordRequest;
import com.example.TriageIQ.DTO.RequestDTO.LoginRequest;
import com.example.TriageIQ.DTO.RequestDTO.RegisterRequest;
import com.example.TriageIQ.DTO.ResponseDTO.AuthResponse;
import com.example.TriageIQ.DTO.ResponseDTO.RegistrationResponse;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Security.CurrentUserProvider;
import com.example.TriageIQ.Service.UserService.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    // ===== Customer portal =====

    @PostMapping("/customer/register")
    public ResponseEntity<RegistrationResponse> registerCustomer(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/customer/login")
    public ResponseEntity<AuthResponse> loginCustomer(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request, Role.CUSTOMER));
    }

    // ===== Agent portal (no public sign-up — accounts are created by an Admin) =====

    @PostMapping("/agent/login")
    public ResponseEntity<AuthResponse> loginAgent(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request, Role.AGENT));
    }

    // ===== Admin portal (no public sign-up — see bootstrap below for the first admin) =====

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> loginAdmin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request, Role.ADMIN));
    }


    @PostMapping("/admin/bootstrap")
    public ResponseEntity<RegistrationResponse> bootstrapAdmin(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader("X-Setup-Key") String setupKey) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.bootstrapAdmin(request, setupKey));
    }


    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam String token, HttpServletResponse response) throws IOException {
        String targetUrl;
        try {
            userService.verifyEmail(token);
            targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/login")
                    .queryParam("verified", "true")
                    .encode()
                    .build().toUriString();
        } catch (RuntimeException ex) {
            targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/login")
                    .queryParam("verified", "false")
                    .queryParam("error", ex.getMessage())
                    .encode()
                    .build().toUriString();
        }
        response.sendRedirect(targetUrl);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}

