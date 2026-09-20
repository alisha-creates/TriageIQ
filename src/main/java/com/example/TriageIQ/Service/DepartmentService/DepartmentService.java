package com.example.TriageIQ.Service.DepartmentService;

import com.example.TriageIQ.DTO.RequestDTO.DepartmentRequest;
import com.example.TriageIQ.DTO.ResponseDTO.DepartmentResponse;
import com.example.TriageIQ.DTO.ResponseDTO.PageResponse;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);

    DepartmentResponse getDepartment(Long id);

    PageResponse<DepartmentResponse> searchDepartments(String keyword, Pageable pageable);

    void deleteDepartment(Long id);
}
