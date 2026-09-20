package com.example.TriageIQ.Repository;

import com.example.TriageIQ.Entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);

    boolean existsByName(String name);

    Page<Department> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}
