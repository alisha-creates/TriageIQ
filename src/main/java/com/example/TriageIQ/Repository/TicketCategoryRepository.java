package com.example.TriageIQ.Repository;

import com.example.TriageIQ.Entity.TicketCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
    Optional<TicketCategory> findByNameAndDepartmentId(String name, Long departmentId);

    List<TicketCategory> findByDepartmentId(Long departmentId);

    boolean existsByNameAndDepartmentId(String name, Long departmentId);

    List<TicketCategory> findByDepartmentIdAndActive(
            Long departmentId,
            Boolean active
    );

    Page<TicketCategory> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<TicketCategory> findByDepartmentIdAndActive(Long departmentId, Boolean active, Pageable pageable);

    Page<TicketCategory> findByDepartmentIdAndNameContainingIgnoreCase(
            Long departmentId, String keyword, Pageable pageable);
}
