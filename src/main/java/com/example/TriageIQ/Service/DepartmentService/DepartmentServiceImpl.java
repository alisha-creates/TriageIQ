package com.example.TriageIQ.Service.DepartmentService;

import com.example.TriageIQ.DTO.RequestDTO.DepartmentRequest;
import com.example.TriageIQ.DTO.ResponseDTO.DepartmentResponse;
import com.example.TriageIQ.DTO.ResponseDTO.PageResponse;
import com.example.TriageIQ.Entity.Department;
import com.example.TriageIQ.Exception.ResourceNotFoundException;
import com.example.TriageIQ.Repository.DepartmentRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService{
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new EntityExistsException("A department named '" + request.getName() + "' already exists");
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = getDepartmentOrThrow(id);

        if (!department.getName().equalsIgnoreCase(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new EntityExistsException("A department named '" + request.getName() + "' already exists");
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        return toResponse(departmentRepository.save(department));
    }

    @Override
    public DepartmentResponse getDepartment(Long id) {
        return toResponse(getDepartmentOrThrow(id));
    }

    @Override
    public PageResponse<DepartmentResponse> searchDepartments(String keyword, Pageable pageable) {
        Page<Department> page = (keyword == null || keyword.isBlank())
                ? departmentRepository.findAll(pageable)
                : departmentRepository.findByNameContainingIgnoreCase(keyword, pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = getDepartmentOrThrow(id);

        if (!department.getAgents().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete a department with agents still assigned to it. Reassign them first.");
        }
        if (!department.getTickets().isEmpty()) {
            throw new IllegalStateException("Cannot delete a department with tickets tied to it.");
        }

        departmentRepository.delete(department);
    }

    private Department getDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .build();
    }

}
