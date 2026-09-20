package com.example.TriageIQ.Service.UserService;

import com.example.TriageIQ.DTO.RequestDTO.ChangePasswordRequest;
import com.example.TriageIQ.DTO.RequestDTO.CreateStaffRequest;
import com.example.TriageIQ.DTO.RequestDTO.LoginRequest;
import com.example.TriageIQ.DTO.RequestDTO.RegisterRequest;
import com.example.TriageIQ.DTO.ResponseDTO.RegistrationResponse;
import com.example.TriageIQ.DTO.ResponseDTO.AuthResponse;
import com.example.TriageIQ.DTO.ResponseDTO.UserSummaryResponse;
import com.example.TriageIQ.Entity.Department;
import com.example.TriageIQ.Entity.Enum.AuthProvider;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Exception.OAuth2AuthenticationProcessingException;
import com.example.TriageIQ.Exception.ResourceNotFoundException;
import com.example.TriageIQ.Repository.DepartmentRepository;
import com.example.TriageIQ.Repository.UserRepository;
import com.example.TriageIQ.Security.JwtService;
import com.example.TriageIQ.Security.LoginAttemptService;
import com.example.TriageIQ.Service.EmailService.EmailService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;

    @Value("${app.admin.bootstrap-key}")
    private String adminBootstrapKey;

    private static final java.util.Set<String> DISALLOWED_BOOTSTRAP_KEYS = java.util.Set.of(
            "CHANGE_ME_TO_A_LONG_RANDOM_SECRET", "secret", "changeme", "admin", "password", "12345"
    );

    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityExistsException(
                    "An account with this email already exists"
            );
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(request.getPassword())
                )
                .role(Role.CUSTOMER)
                .provider(AuthProvider.LOCAL)
                .providerId(null)
                .enabled(true)
                .emailVerified(false)
                .mustChangePassword(false)
                .build();

        User savedUser = userRepository.save(user);

        String verificationToken =
                jwtService.generateEmailVerificationToken(savedUser);

        emailService.sendVerificationEmail(
                savedUser,
                verificationToken
        );

        return RegistrationResponse.builder()
                .message(
                        "Account created successfully. " +
                                "Check your email to verify your account."
                )
                .email(savedUser.getEmail())
                .build();
    }

    @Override
    @Transactional
    public RegistrationResponse verifyEmail(String token) {
        String email = jwtService.extractEmailFromVerificationToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("This account is already verified");
        }

        user.setEmailVerified(true);

        userRepository.save(user);

        return RegistrationResponse.builder()
                .message("Email verified successfully. You can now log in.")
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public User createStaff(CreateStaffRequest request, Role role) {
        if (role == Role.CUSTOMER) {
            throw new IllegalArgumentException("Use register() for customer sign-up");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityExistsException("An account with this email already exists");
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + request.getDepartmentId()));
        }

        boolean passwordProvided = request.getPassword() != null && !request.getPassword().isBlank();
        String rawPassword = passwordProvided ? request.getPassword() : generateTemporaryPassword();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .department(department)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .emailVerified(true)
                // Staff accounts always have to prove they've seen the password
                // and set their own before they can use the system.
                .mustChangePassword(true)
                .build();

        User saved = userRepository.save(user);

        emailService.sendStaffInviteEmail(saved, rawPassword, role);

        return saved;
    }

    @Override
    @Transactional
    public RegistrationResponse bootstrapAdmin(RegisterRequest request, String providedSetupKey) {
        if (adminBootstrapKey == null
                || adminBootstrapKey.isBlank()
                || adminBootstrapKey.length() < 16
                || DISALLOWED_BOOTSTRAP_KEYS.contains(adminBootstrapKey.toLowerCase())) {
            throw new IllegalStateException(
                    "Admin bootstrap is not configured. Set app.admin.bootstrap-key to a real, random secret (16+ characters) first.");
        }
        if (providedSetupKey == null || !providedSetupKey.equals(adminBootstrapKey)) {
            throw new BadCredentialsException("Invalid setup key");
        }
        if (!userRepository.findByRole(Role.ADMIN).isEmpty()) {
            throw new IllegalStateException(
                    "An admin account already exists. Ask an existing admin to create additional admins.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityExistsException("An account with this email already exists");
        }

        User admin = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .emailVerified(true)
                .mustChangePassword(false)
                .build();

        userRepository.save(admin);

        return RegistrationResponse.builder()
                .message("First admin account created. You can now log in from the admin portal.")
                .email(admin.getEmail())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request, Role expectedRole) {
        if (loginAttemptService.isBlocked(request.getEmail())) {
            throw new BadCredentialsException(
                    "Too many failed login attempts. Try again in 15 minutes.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getRole() != expectedRole) {
            throw new BadCredentialsException(
                    "This isn't a " + expectedRole.name().toLowerCase()
                            + " account. Please use the correct login page for your role.");
        }

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BadCredentialsException(
                    "This account signs in with " + user.getProvider() + ". Use that method instead.");
        }
        if (!user.getEnabled()) {
            throw new BadCredentialsException("This account has been disabled");
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException(
                    "Please verify your email before logging in"
            );
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailure(request.getEmail());
            throw e;
        }

        loginAttemptService.recordSuccess(request.getEmail());

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .user(toSummary(user))
                .build();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUserOrThrow(userId);

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalArgumentException(
                    "This account signs in with " + user.getProvider() + " and has no password to change.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User createOrGetGoogleUser(String name, String email, String providerId) {
        return userRepository
                .findByProviderAndProviderId(
                        AuthProvider.GOOGLE,
                        providerId
                )
                .orElseGet(() -> {
                            User existingUser =
                                    userRepository.findByEmail(email)
                                            .orElse(null);

                            if (existingUser != null) {
                                // Staff accounts must always sign in with a password — never let a
                                // Google account silently take over an Agent/Admin's identity.
                                if (existingUser.getRole() != Role.CUSTOMER) {
                                    throw new OAuth2AuthenticationProcessingException(
                                            "Staff accounts can't sign in with Google. Please use your email and password."
                                    );
                                }

                                if (existingUser.getProvider() == AuthProvider.LOCAL) {
                                    existingUser.setProvider(AuthProvider.GOOGLE);
                                    existingUser.setProviderId(providerId);
                                    existingUser.setEmailVerified(true);
                                }
                                existingUser.setName(name);
                                return userRepository.save(existingUser);
                            }

                            User googleUser = User.builder()
                                    .name(name)
                                    .email(email)
                                    .password(null)
                                    .role(Role.CUSTOMER)
                                    .provider(AuthProvider.GOOGLE)
                                    .providerId(providerId)
                                    .enabled(true)
                                    .emailVerified(true)
                                    .mustChangePassword(false)
                                    .activationToken(null)
                                    .activationTokenExpiry(null)
                                    .build();

                            return userRepository.save(googleUser);
                        }
                );
    }

    @Override
    public UserSummaryResponse getCurrentUserSummary(Long userId) {
        return toSummary(getUserOrThrow(userId));
    }

    @Override
    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        User user = getUserOrThrow(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignDepartment(Long userId, Long departmentId) {
        User user = getUserOrThrow(userId);
        if (user.getRole() == Role.CUSTOMER) {
            throw new IllegalArgumentException("Customers cannot be assigned to a department");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
        user.setDepartment(department);
        userRepository.save(user);
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(14);
        for (int i = 0; i < 14; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UserSummaryResponse toSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .mustChangePassword(user.getMustChangePassword())
                .build();
    }
}
