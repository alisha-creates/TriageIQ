package com.example.TriageIQ.Repository;

import com.example.TriageIQ.Entity.Enum.AuthProvider;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    List<User> findByRole(Role role);

    List<User> findByDepartmentId(Long departmentId);

    Optional<User> findByActivationToken(String activationToken);
}
