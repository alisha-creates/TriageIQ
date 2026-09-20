package com.example.TriageIQ.Service.TicketCategoryService;

import com.example.TriageIQ.DTO.RequestDTO.TicketCategoryRequest;
import com.example.TriageIQ.DTO.ResponseDTO.PageResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketCategoryResponse;
import com.example.TriageIQ.Entity.Department;
import com.example.TriageIQ.Entity.TicketCategory;
import com.example.TriageIQ.Exception.ResourceNotFoundException;
import com.example.TriageIQ.Repository.DepartmentRepository;
import com.example.TriageIQ.Repository.TicketCategoryRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketCategoryServiceImpl implements TicketCategoryService{
    private final TicketCategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public TicketCategoryResponse createCategory(TicketCategoryRequest request) {
        Department department = getDepartmentOrThrow(request.getDepartmentId());

        if (categoryRepository.existsByNameAndDepartmentId(request.getName(), department.getId())) {
            throw new EntityExistsException(
                    "A category named '" + request.getName() + "' already exists in " + department.getName());
        }

        TicketCategory category = TicketCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .department(department)
                .active(true)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public TicketCategoryResponse updateCategory(Long id, TicketCategoryRequest request) {
        TicketCategory category = getCategoryOrThrow(id);
        Department department = getDepartmentOrThrow(request.getDepartmentId());

        boolean nameOrDepartmentChanged =
                !category.getName().equalsIgnoreCase(request.getName())
                        || !category.getDepartment().getId().equals(department.getId());

        if (nameOrDepartmentChanged
                && categoryRepository.existsByNameAndDepartmentId(request.getName(), department.getId())) {
            throw new EntityExistsException(
                    "A category named '" + request.getName() + "' already exists in " + department.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDepartment(department);

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketCategoryResponse getCategory(Long id) {
        return toResponse(getCategoryOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCategoryResponse> getCategoriesByDepartment(Long departmentId) {
        return categoryRepository.findByDepartmentId(departmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketCategoryResponse> searchCategories(
            Long departmentId, String keyword, Pageable pageable) {
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        Page<TicketCategory> page = categoryRepository
                .findByDepartmentIdAndNameContainingIgnoreCase(departmentId, safeKeyword, pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    @Transactional
    public void setActive(Long id, boolean active) {
        TicketCategory category = getCategoryOrThrow(id);
        category.setActive(active);
        categoryRepository.save(category);
    }

    private TicketCategory getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket category not found: " + id));
    }

    private Department getDepartmentOrThrow(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
    }

    private TicketCategoryResponse toResponse(TicketCategory category) {
        return TicketCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .departmentId(category.getDepartment().getId())
                .departmentName(category.getDepartment().getName())
                .active(category.getActive())
                .build();
    }
}
