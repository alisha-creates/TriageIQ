package com.example.TriageIQ.Service.TicketClassificationService;

import com.example.TriageIQ.DTO.ResponseDTO.TicketCategoryResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketClassificationResponse;
import com.example.TriageIQ.DTO.ResponseDTO.UserSummaryResponse;
import com.example.TriageIQ.Entity.Enum.Priority;
import com.example.TriageIQ.Entity.Enum.RoutingDecision;
import com.example.TriageIQ.Entity.Ticket;
import com.example.TriageIQ.Entity.TicketCategory;
import com.example.TriageIQ.Entity.TicketClassification;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Exception.ResourceNotFoundException;
import com.example.TriageIQ.Repository.TicketCategoryRepository;
import com.example.TriageIQ.Repository.TicketClassificationRepository;
import com.example.TriageIQ.Repository.TicketRepository;
import com.example.TriageIQ.Repository.UserRepository;
import com.example.TriageIQ.Service.ChatService.ChatService;
import com.example.TriageIQ.Service.ai.AiOrchestrationService.AiOrchestrationService;
import com.example.TriageIQ.Service.ai.model.AiDecision;
import com.example.TriageIQ.Service.ai.model.TicketClassificationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketClassificationServiceImpl implements TicketClassificationService{
    private static final double AUTO_ROUTE_CONFIDENCE_THRESHOLD = 0.7;

    private final AiOrchestrationService aiOrchestrationService;
    private final TicketRepository ticketRepository;
    private final TicketClassificationRepository classificationRepository;
    private final TicketCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    @Override
    @Transactional
    public TicketClassificationResponse classifyTicket(Long ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);

        if (classificationRepository.existsByTicketId(ticketId)) {
            throw new IllegalStateException(
                    "Ticket has already been classified. Use overrideClassification to change it.");
        }

        List<String> categoryNames = categoryRepository.findAll().stream()
                .filter(TicketCategory::getActive)
                .map(TicketCategory::getName)
                .toList();

        AiDecision<TicketClassificationResult> decision = aiOrchestrationService.classifyTicket(
                ticket.getSubject(), ticket.getDescription(), categoryNames);

        if (!decision.isSuccess()) {
            return persistClassification(ticket, null, null, null,
                    RoutingDecision.NEEDS_MANUAL_REVIEW, null, decision.getErrorMessage(), null);
        }

        TicketClassificationResult result = decision.getResult();
        Optional<TicketCategory> matchedCategory = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(result.getCategoryName()) && c.getActive())
                .findFirst();

        Priority priority = parsePriority(result.getSuggestedPriority());
        RoutingDecision routing = (matchedCategory.isPresent()
                && result.getConfidenceScore() != null
                && result.getConfidenceScore() >= AUTO_ROUTE_CONFIDENCE_THRESHOLD)
                ? RoutingDecision.AUTO_ROUTED
                : RoutingDecision.NEEDS_MANUAL_REVIEW;

        TicketClassificationResponse response = persistClassification(
                ticket, matchedCategory.orElse(null), priority, result.getConfidenceScore(),
                routing, result.getReasoning(), result.getRawResponse(), null);

        if (routing == RoutingDecision.AUTO_ROUTED) {
            ticket.setCategory(matchedCategory.get());
            ticket.setAssignedDepartment(matchedCategory.get().getDepartment());
            ticket.setPriority(priority);
            ticketRepository.save(ticket);

            chatService.postSystemMessage(ticket.getId(),
                    "Ticket auto-routed to " + matchedCategory.get().getDepartment().getName()
                            + " (" + matchedCategory.get().getName() + ")");
        } else {
            chatService.postSystemMessage(ticket.getId(), "This ticket needs manual review before routing.");
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TicketClassificationResponse getClassification(Long ticketId) {
        TicketClassification classification = classificationRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket has not been classified yet: " + ticketId));
        return toResponse(classification);
    }

    @Override
    @Transactional
    public TicketClassificationResponse overrideClassification(
            Long ticketId, Long categoryId, String priorityStr, Long reviewedByUserId) {

        Ticket ticket = getTicketOrThrow(ticketId);
        TicketClassification classification = classificationRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket has not been classified yet: " + ticketId));

        TicketCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        User reviewer = userRepository.findById(reviewedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + reviewedByUserId));
        Priority priority = parsePriority(priorityStr);

        classification.setCategory(category);
        classification.setSuggestedPriority(priority);
        classification.setRoutingDecision(RoutingDecision.AUTO_ROUTED);
        classification.setReviewedBy(reviewer);
        classificationRepository.save(classification);

        ticket.setCategory(category);
        ticket.setAssignedDepartment(category.getDepartment());
        ticket.setPriority(priority);
        ticketRepository.save(ticket);

        chatService.postSystemMessage(ticketId,
                "Routing manually corrected by " + reviewer.getName()
                        + " to " + category.getDepartment().getName() + " (" + category.getName() + ")");

        return toResponse(classification);
    }

    private TicketClassificationResponse persistClassification(
            Ticket ticket, TicketCategory category, Priority priority, Double confidence,
            RoutingDecision routing, String reasoning, String rawResponse, User reviewedBy) {

        TicketClassification classification = TicketClassification.builder()
                .ticket(ticket)
                .category(category)
                .suggestedPriority(priority)
                .routingDecision(routing)
                .confidenceScore(confidence)
                .reasoning(reasoning)
                .reviewedBy(reviewedBy)
                .build();

        return toResponse(classificationRepository.save(classification));
    }

    private Priority parsePriority(String priorityStr) {
        if (priorityStr == null) return Priority.MEDIUM;
        try {
            return Priority.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Priority.MEDIUM;
        }
    }

    private Ticket getTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
    }

    private TicketClassificationResponse toResponse(TicketClassification c) {
        return TicketClassificationResponse.builder()
                .id(c.getId())
                .category(c.getCategory() != null ? TicketCategoryResponse.builder()
                        .id(c.getCategory().getId())
                        .name(c.getCategory().getName())
                        .description(c.getCategory().getDescription())
                        .departmentId(c.getCategory().getDepartment().getId())
                        .departmentName(c.getCategory().getDepartment().getName())
                        .active(c.getCategory().getActive())
                        .build() : null)
                .suggestedPriority(c.getSuggestedPriority())
                .routingDecision(c.getRoutingDecision())
                .confidenceScore(c.getConfidenceScore())
                .reasoning(c.getReasoning())
                .reviewedBy(c.getReviewedBy() != null ? toUserSummary(c.getReviewedBy()) : null)
                .classifiedAt(c.getClassifiedAt())
                .build();
    }

    private UserSummaryResponse toUserSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId()).name(user.getName()).email(user.getEmail())
                .role(user.getRole()).avatarUrl(user.getAvatarUrl())
                .build();
    }
}
