package com.example.TriageIQ.Controller;

import com.example.TriageIQ.DTO.RequestDTO.CreateStaffRequest;
import com.example.TriageIQ.DTO.RequestDTO.DepartmentRequest;
import com.example.TriageIQ.DTO.RequestDTO.TicketCategoryRequest;
import com.example.TriageIQ.DTO.ResponseDTO.*;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Security.CurrentUserProvider;
import com.example.TriageIQ.Service.DepartmentService.DepartmentService;
import com.example.TriageIQ.Service.TicketCategoryService.TicketCategoryService;
import com.example.TriageIQ.Service.TicketClassificationService.TicketClassificationService;
import com.example.TriageIQ.Service.TicketService.TicketService;
import com.example.TriageIQ.Service.UserService.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final DepartmentService departmentService;
    private final TicketCategoryService categoryService;
    private final TicketService ticketService;
    private final TicketClassificationService classificationService;
    private final CurrentUserProvider currentUserProvider;

    // Password is optional here — leave it blank to have a secure temporary
    // password generated and emailed to the new staff member (mustChangePassword=true).
    @PostMapping("/users/agents")
    public ResponseEntity<Void> createAgent(@Valid @RequestBody CreateStaffRequest request) {
        userService.createStaff(request, Role.AGENT);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/users/admins")
    public ResponseEntity<Void> createAdmin(@Valid @RequestBody CreateStaffRequest request) {
        userService.createStaff(request, Role.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/users/{userId}/enabled")
    public ResponseEntity<Void> setUserEnabled(@PathVariable Long userId, @RequestParam boolean enabled) {
        userService.setEnabled(userId, enabled);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/department")
    public ResponseEntity<Void> assignDepartment(@PathVariable Long userId, @RequestParam Long departmentId) {
        userService.assignDepartment(userId, departmentId);
        return ResponseEntity.noContent().build();
    }

    // ===== Departments =====

    @PostMapping("/departments")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(request));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @GetMapping("/departments")
    public ResponseEntity<PageResponse<DepartmentResponse>> searchDepartments(
            @RequestParam(required = false) String keyword, Pageable pageable) {
        return ResponseEntity.ok(departmentService.searchDepartments(keyword, pageable));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Categories =====

    @PostMapping("/categories")
    public ResponseEntity<TicketCategoryResponse> createCategory(@Valid @RequestBody TicketCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<TicketCategoryResponse> updateCategory(
            @PathVariable Long id, @Valid @RequestBody TicketCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @GetMapping("/categories")
    public ResponseEntity<PageResponse<TicketCategoryResponse>> searchCategories(
            @RequestParam Long departmentId,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(categoryService.searchCategories(departmentId, keyword, pageable));
    }

    @PatchMapping("/categories/{id}/active")
    public ResponseEntity<Void> setCategoryActive(@PathVariable Long id, @RequestParam boolean active) {
        categoryService.setActive(id, active);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/tickets/needing-review")
    public ResponseEntity<List<TicketResponse>> getTicketsNeedingReview() {
        return ResponseEntity.ok(ticketService.getTicketsNeedingReview());
    }

    @PostMapping("/tickets/{ticketId}/classification/override")
    public ResponseEntity<TicketClassificationResponse> overrideClassification(
            @PathVariable Long ticketId,
            @RequestParam Long categoryId,
            @RequestParam String priority) {

        Long reviewerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(
                classificationService.overrideClassification(ticketId, categoryId, priority, reviewerId));
    }

    @PostMapping("/tickets/{ticketId}/assign-agent")
    public ResponseEntity<TicketResponse> assignAgent(@PathVariable Long ticketId, @RequestParam Long agentId) {
        Long changedByUserId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ticketService.assignAgent(ticketId, agentId, changedByUserId));
    }
}

