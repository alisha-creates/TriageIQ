package com.example.TriageIQ.Service.TicketService;

import com.example.TriageIQ.DTO.RequestDTO.CreateTicketRequest;
import com.example.TriageIQ.DTO.RequestDTO.EditTicketRequest;
import com.example.TriageIQ.DTO.RequestDTO.TicketSearchRequest;
import com.example.TriageIQ.DTO.RequestDTO.UpdateTicketStatusRequest;
import com.example.TriageIQ.DTO.ResponseDTO.PageResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketStatusHistoryResponse;
import com.example.TriageIQ.Entity.Enum.Role;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(CreateTicketRequest request, Long customerId);

    TicketResponse getTicket(Long ticketId, Long requestingUserId, Role requestingUserRole);

    PageResponse<TicketResponse> getMyTickets(Long customerId, Pageable pageable);

    PageResponse<TicketResponse> searchTickets(
            TicketSearchRequest request,
            Role requestingUserRole,
            Long requestingUserDepartmentId,
            Pageable pageable);

    List<TicketResponse> getUnassignedTickets(Long departmentId);

    TicketResponse claimTicket(Long ticketId, Long agentId);

    TicketResponse assignAgent(Long ticketId, Long agentId, Long changedByUserId);

    TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request, Long changedByUserId);

    List<TicketStatusHistoryResponse> getStatusHistory(Long ticketId, Long requestingUserId, Role requestingUserRole);

    List<TicketResponse> getTicketsNeedingReview();

    TicketResponse editTicket(Long ticketId, EditTicketRequest request, Long customerId);

    TicketResponse closeTicketByCustomer(Long ticketId, Long customerId, String reason);

    TicketResponse reopenTicketByCustomer(Long ticketId, Long customerId, String reason);
}
