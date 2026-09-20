package com.example.TriageIQ.Security;

import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Exception.ResourceNotFoundException;
import com.example.TriageIQ.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }
}
