package com.example.TriageIQ.Controller;

import com.example.TriageIQ.DTO.RequestDTO.EditAiReplyRequest;
import com.example.TriageIQ.DTO.RequestDTO.TicketSearchRequest;
import com.example.TriageIQ.DTO.RequestDTO.UpdateTicketStatusRequest;
import com.example.TriageIQ.DTO.ResponseDTO.AiReplySuggestionResponse;
import com.example.TriageIQ.DTO.ResponseDTO.PageResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketClassificationResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketResponse;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Security.CurrentUserProvider;
import com.example.TriageIQ.Service.ReplySuggestionService.ReplySuggestionService;
import com.example.TriageIQ.Service.TicketClassificationService.TicketClassificationService;
import com.example.TriageIQ.Service.TicketService.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
@PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
@RequiredArgsConstructor
public class AgentController {
    private final TicketService ticketService;
    private final ReplySuggestionService replySuggestionService;
    private final CurrentUserProvider currentUserProvider;
    private final TicketClassificationService classificationService;

    @GetMapping("/tickets/search")
    public ResponseEntity<PageResponse<TicketResponse>> searchTickets(
            TicketSearchRequest request, Pageable pageable) {

        User user = currentUserProvider.getCurrentUser();
        Long departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        return ResponseEntity.ok(
                ticketService.searchTickets(request, user.getRole(), departmentId, pageable));
    }

    @GetMapping("/tickets/unassigned")
    public ResponseEntity<List<TicketResponse>> getUnassignedTickets() {
        User user = currentUserProvider.getCurrentUser();
        Long departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        return ResponseEntity.ok(ticketService.getUnassignedTickets(departmentId));
    }

    @PostMapping("/tickets/{ticketId}/claim")
    public ResponseEntity<TicketResponse> claimTicket(@PathVariable Long ticketId) {
        Long agentId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ticketService.claimTicket(ticketId, agentId));
    }

    @PatchMapping("/tickets/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long ticketId, @Valid @RequestBody UpdateTicketStatusRequest request) {

        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ticketService.updateStatus(ticketId, request, userId));
    }

    @PostMapping("/tickets/{ticketId}/reply-suggestions")
    public ResponseEntity<AiReplySuggestionResponse> generateSuggestion(@PathVariable Long ticketId) {
        return ResponseEntity.ok(replySuggestionService.generateSuggestion(ticketId));
    }

    @GetMapping("/tickets/{ticketId}/reply-suggestions/pending")
    public ResponseEntity<AiReplySuggestionResponse> getPendingSuggestion(@PathVariable Long ticketId) {
        return ResponseEntity.ok(replySuggestionService.getLatestPending(ticketId));
    }

    @PutMapping("/reply-suggestions/{suggestionId}")
    public ResponseEntity<AiReplySuggestionResponse> editSuggestion(
            @PathVariable Long suggestionId, @Valid @RequestBody EditAiReplyRequest request) {

        Long reviewerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(replySuggestionService.editSuggestion(suggestionId, request, reviewerId));
    }

    @PostMapping("/reply-suggestions/{suggestionId}/accept")
    public ResponseEntity<Void> acceptSuggestion(@PathVariable Long suggestionId) {
        Long reviewerId = currentUserProvider.getCurrentUserId();
        replySuggestionService.acceptAndSend(suggestionId, reviewerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reply-suggestions/{suggestionId}/reject")
    public ResponseEntity<Void> rejectSuggestion(@PathVariable Long suggestionId) {
        Long reviewerId = currentUserProvider.getCurrentUserId();
        replySuggestionService.reject(suggestionId, reviewerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tickets/{ticketId}/classify")
    public ResponseEntity<TicketClassificationResponse> classifyTicket(@PathVariable Long ticketId) {
        // Mainly for re-triggering/testing — auto-classification already runs on creation
        return ResponseEntity.ok(classificationService.classifyTicket(ticketId));
    }

    @GetMapping("/tickets/{ticketId}/classification")
    public ResponseEntity<TicketClassificationResponse> getClassification(@PathVariable Long ticketId) {
        return ResponseEntity.ok(classificationService.getClassification(ticketId));
    }
}
