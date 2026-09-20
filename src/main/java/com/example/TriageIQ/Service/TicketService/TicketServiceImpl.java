package com.example.TriageIQ.Service.TicketService;

import com.example.TriageIQ.DTO.RequestDTO.CreateTicketRequest;
import com.example.TriageIQ.DTO.RequestDTO.EditTicketRequest;
import com.example.TriageIQ.DTO.RequestDTO.TicketSearchRequest;
import com.example.TriageIQ.DTO.RequestDTO.UpdateTicketStatusRequest;
import com.example.TriageIQ.DTO.ResponseDTO.TicketResponse;
import com.example.TriageIQ.DTO.ResponseDTO.PageResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketCategoryResponse;
import com.example.TriageIQ.DTO.ResponseDTO.DepartmentResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketStatusHistoryResponse;
import com.example.TriageIQ.DTO.ResponseDTO.UserSummaryResponse;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.Enum.TicketStatus;
import com.example.TriageIQ.Entity.Ticket;
import com.example.TriageIQ.Entity.TicketStatusHistory;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Exception.ResourceNotFoundException;
import com.example.TriageIQ.Repository.TicketRepository;
import com.example.TriageIQ.Repository.TicketStatusHistoryRepository;
import com.example.TriageIQ.Repository.UserRepository;
import com.example.TriageIQ.Service.ChatService.ChatService;
import com.example.TriageIQ.Service.EmailService.EmailService;
import com.example.TriageIQ.Service.TicketClassificationService.TicketClassificationService;
import com.example.TriageIQ.Validation.TicketStateValidator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final EmailService emailService;
    private final TicketStateValidator stateValidator;
    private final TicketClassificationService classificationService;

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + customerId));

        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Only customers can create tickets");
        }

        Ticket ticket = Ticket.builder()
                .subject(request.getSubject())
                .description(request.getDescription())
                .customer(customer)
                .status(TicketStatus.OPEN)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        statusHistoryRepository.save(TicketStatusHistory.builder()
                .ticket(saved)
                .oldStatus(null)
                .newStatus(TicketStatus.OPEN)
                .changedBy(customer)
                .reason("Ticket created")
                .build());

        chatService.postSystemMessage(saved.getId(),
                "Ticket created. Our team will get back to you shortly.");

        classificationService.classifyTicket(saved.getId());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long ticketId, Long requestingUserId, Role requestingUserRole) {
        Ticket ticket = getTicketOrThrow(ticketId);
        assertCanView(ticket, requestingUserId, requestingUserRole);
        return toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> getMyTickets(Long customerId, Pageable pageable) {
        Page<Ticket> page = ticketRepository.findByCustomerId(customerId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> searchTickets(
            TicketSearchRequest request,
            Role requestingUserRole,
            Long requestingUserDepartmentId,
            Pageable pageable) {
        Long effectiveDepartmentId = request.getDepartmentId();
        if (requestingUserRole == Role.AGENT) {
            effectiveDepartmentId = requestingUserDepartmentId;
        }

        Specification<Ticket> spec = buildSearchSpecification(request, effectiveDepartmentId);
        Page<Ticket> page = ticketRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getUnassignedTickets(Long departmentId) {
        return ticketRepository.findByAssignedAgentIsNullAndAssignedDepartmentId(departmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse claimTicket(Long ticketId, Long agentId) {
        Ticket ticket = getTicketOrThrow(ticketId);

        if (ticket.getAssignedAgent() != null) {
            throw new IllegalStateException("This ticket has already been claimed");
        }

        User agent = getAgentOrThrow(agentId);
        return assignAndTransition(ticket, agent, agent, "Agent claimed ticket from queue");
    }

    @Override
    @Transactional
    public TicketResponse assignAgent(Long ticketId, Long agentId, Long changedByUserId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User agent = getAgentOrThrow(agentId);
        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + changedByUserId));

        return assignAndTransition(ticket, agent, changedBy, "Reassigned by " + changedBy.getRole());
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request, Long changedByUserId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + changedByUserId));

        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();

        stateValidator.validateTransition(oldStatus, newStatus);
        stateValidator.validateResolutionRequiresAssignment(ticket, newStatus);

        if (oldStatus == newStatus) {
            throw new IllegalArgumentException("Ticket is already " + newStatus);
        }

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            ticket.setResolvedAt(java.time.LocalDateTime.now());
        }
        Ticket saved = ticketRepository.save(ticket);

        statusHistoryRepository.save(TicketStatusHistory.builder()
                .ticket(saved)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .reason(request.getReason())
                .build());

        chatService.postSystemMessage(saved.getId(),
                "Status changed from " + oldStatus + " to " + newStatus
                        + (request.getReason() != null ? " — " + request.getReason() : ""));

        if (newStatus == TicketStatus.RESOLVED) {
            emailService.sendTicketResolvedEmail(saved.getCustomer(), saved);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketStatusHistoryResponse> getStatusHistory(Long ticketId, Long requestingUserId, Role requestingUserRole) {
        Ticket ticket = getTicketOrThrow(ticketId);
        assertCanView(ticket, requestingUserId, requestingUserRole);
        return statusHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsNeedingReview() {
        return ticketRepository.findByAssignedDepartmentIsNull().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse editTicket(Long ticketId, EditTicketRequest request, Long customerId) {
        Ticket ticket = getTicketOrThrow(ticketId);

        if (!ticket.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("You do not have access to this ticket");
        }
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new IllegalStateException(
                    "This ticket can only be edited while it is OPEN. Once an agent has started "
                            + "working on it, add a message to the conversation instead.");
        }

        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        Ticket saved = ticketRepository.save(ticket);

        chatService.postSystemMessage(saved.getId(), "Customer updated the ticket details.");

        // Content changed, so refresh the AI classification/priority against the new text.
        classificationService.classifyTicket(saved.getId());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse closeTicketByCustomer(Long ticketId, Long customerId, String reason) {
        Ticket ticket = getTicketOrThrow(ticketId);

        if (!ticket.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("You do not have access to this ticket");
        }

        TicketStatus oldStatus = ticket.getStatus();
        stateValidator.validateTransition(oldStatus, TicketStatus.CLOSED);

        String effectiveReason = (reason != null && !reason.isBlank()) ? reason : "Closed by customer";

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setResolvedAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(ticket);

        statusHistoryRepository.save(TicketStatusHistory.builder()
                .ticket(saved)
                .oldStatus(oldStatus)
                .newStatus(TicketStatus.CLOSED)
                .changedBy(ticket.getCustomer())
                .reason(effectiveReason)
                .build());

        chatService.postSystemMessage(saved.getId(), "Ticket closed by customer — " + effectiveReason);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse reopenTicketByCustomer(Long ticketId, Long customerId, String reason) {
        Ticket ticket = getTicketOrThrow(ticketId);

        if (!ticket.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("You do not have access to this ticket");
        }

        TicketStatus oldStatus = ticket.getStatus();
        stateValidator.validateTransition(oldStatus, TicketStatus.REOPENED);

        String effectiveReason = (reason != null && !reason.isBlank()) ? reason : "Reopened by customer";

        ticket.setStatus(TicketStatus.REOPENED);
        ticket.setResolvedAt(null);
        Ticket saved = ticketRepository.save(ticket);

        statusHistoryRepository.save(TicketStatusHistory.builder()
                .ticket(saved)
                .oldStatus(oldStatus)
                .newStatus(TicketStatus.REOPENED)
                .changedBy(ticket.getCustomer())
                .reason(effectiveReason)
                .build());

        chatService.postSystemMessage(saved.getId(), "Ticket reopened by customer — " + effectiveReason);

        return toResponse(saved);
    }

    private TicketResponse assignAndTransition(Ticket ticket, User agent, User changedBy, String reason) {
        TicketStatus oldStatus = ticket.getStatus();

        ticket.setAssignedAgent(agent);
        ticket.setAssignedDepartment(agent.getDepartment());
        if (oldStatus == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        Ticket saved = ticketRepository.save(ticket);

        statusHistoryRepository.save(TicketStatusHistory.builder()
                .ticket(saved)
                .oldStatus(oldStatus)
                .newStatus(saved.getStatus())
                .changedBy(changedBy)
                .reason(reason)
                .build());

        chatService.postSystemMessage(saved.getId(),
                agent.getName() + " has joined the conversation.");

        return toResponse(saved);
    }

    private Specification<Ticket> buildSearchSpecification(TicketSearchRequest request, Long departmentId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), request.getPriority()));
            }
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("assignedDepartment").get("id"), departmentId));
            }
            if (request.getAssignedAgentId() != null) {
                predicates.add(cb.equal(root.get("assignedAgent").get("id"), request.getAssignedAgentId()));
            }
            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String pattern = "%" + request.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("subject")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void assertCanView(Ticket ticket, Long requestingUserId, Role requestingUserRole) {
        boolean isOwner = ticket.getCustomer().getId().equals(requestingUserId);
        boolean isAssignedAgent = ticket.getAssignedAgent() != null
                && ticket.getAssignedAgent().getId().equals(requestingUserId);

        if (requestingUserRole == Role.CUSTOMER && !isOwner) {
            throw new IllegalArgumentException("You do not have access to this ticket");
        }
        if (requestingUserRole == Role.AGENT && !isOwner && !isAssignedAgent) {
            throw new IllegalArgumentException("You do not have access to this ticket");
        }
    }

    private Ticket getTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
    }

    private User getAgentOrThrow(Long agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + agentId));
        if (agent.getRole() != Role.AGENT) {
            throw new IllegalArgumentException("Only agents can be assigned tickets");
        }
        return agent;
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .customer(toUserSummary(ticket.getCustomer()))
                .assignedAgent(ticket.getAssignedAgent() != null ? toUserSummary(ticket.getAssignedAgent()) : null)
                .assignedDepartment(ticket.getAssignedDepartment() != null
                        ? DepartmentResponse.builder()
                        .id(ticket.getAssignedDepartment().getId())
                        .name(ticket.getAssignedDepartment().getName())
                        .description(ticket.getAssignedDepartment().getDescription())
                        .build()
                        : null)
                .category(ticket.getCategory() != null
                        ? TicketCategoryResponse.builder()
                        .id(ticket.getCategory().getId())
                        .name(ticket.getCategory().getName())
                        .description(ticket.getCategory().getDescription())
                        .departmentId(ticket.getCategory().getDepartment().getId())
                        .departmentName(ticket.getCategory().getDepartment().getName())
                        .active(ticket.getCategory().getActive())
                        .build()
                        : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }

    private TicketStatusHistoryResponse toHistoryResponse(TicketStatusHistory history) {
        return TicketStatusHistoryResponse.builder()
                .id(history.getId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedBy(history.getChangedBy() != null ? toUserSummary(history.getChangedBy()) : null)
                .reason(history.getReason())
                .changedAt(history.getChangedAt())
                .build();
    }

    private UserSummaryResponse toUserSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
