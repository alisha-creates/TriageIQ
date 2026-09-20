package com.example.TriageIQ.Controller;

import com.example.TriageIQ.DTO.RequestDTO.CreateTicketRequest;
import com.example.TriageIQ.DTO.RequestDTO.EditTicketRequest;
import com.example.TriageIQ.DTO.ResponseDTO.PageResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketResponse;
import com.example.TriageIQ.DTO.ResponseDTO.TicketStatusHistoryResponse;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Security.CurrentUserProvider;
import com.example.TriageIQ.Service.TicketService.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        User user = currentUserProvider.getCurrentUser();
        TicketResponse response = ticketService.createTicket(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<TicketResponse>> getMyTickets(Pageable pageable) {
        Long customerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ticketService.getMyTickets(customerId, pageable));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long ticketId) {
        User user = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(ticketService.getTicket(ticketId, user.getId(), user.getRole()));
    }

    @GetMapping("/{ticketId}/status-history")
    public ResponseEntity<List<TicketStatusHistoryResponse>> getStatusHistory(@PathVariable Long ticketId) {
        User user = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(ticketService.getStatusHistory(ticketId, user.getId(), user.getRole()));
    }

    /**
     * Customer-only. Only works while the ticket is still OPEN — once an agent
     * has started working on it, edits are locked (add a chat message instead).
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> editTicket(
            @PathVariable Long ticketId, @Valid @RequestBody EditTicketRequest request) {
        Long customerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ticketService.editTicket(ticketId, request, customerId));
    }

    /**
     * Customer-only. Closes the ticket instead of deleting it, so the
     * conversation/history is preserved. Works even if no agent has been
     * assigned yet — the customer decides they no longer need help.
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/{ticketId}/close")
    public ResponseEntity<TicketResponse> closeTicket(
            @PathVariable Long ticketId, @RequestParam(required = false) String reason) {
        Long customerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ticketService.closeTicketByCustomer(ticketId, customerId, reason));
    }

    /**
     * Customer-only. Reopens a RESOLVED or CLOSED ticket of their own.
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/{ticketId}/reopen")
    public ResponseEntity<TicketResponse> reopenTicket(
            @PathVariable Long ticketId, @RequestParam(required = false) String reason) {
        Long customerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ticketService.reopenTicketByCustomer(ticketId, customerId, reason));
    }
}