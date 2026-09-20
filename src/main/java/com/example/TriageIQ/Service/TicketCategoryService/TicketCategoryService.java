package com.example.TriageIQ.Service.TicketCategoryService;

import com.example.TriageIQ.DTO.RequestDTO.TicketCategoryRequest;
import com.example.TriageIQ.DTO.ResponseDTO.PageResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketCategoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketCategoryService {
    TicketCategoryResponse createCategory(TicketCategoryRequest request);

    TicketCategoryResponse updateCategory(Long id, TicketCategoryRequest request);

    TicketCategoryResponse getCategory(Long id);

    List<TicketCategoryResponse> getCategoriesByDepartment(Long departmentId);

    PageResponse<TicketCategoryResponse> searchCategories(Long departmentId, String keyword, Pageable pageable);

    void setActive(Long id, boolean active);
}
