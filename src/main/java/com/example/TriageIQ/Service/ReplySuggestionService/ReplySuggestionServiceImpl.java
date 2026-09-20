package com.example.TriageIQ.Service.ReplySuggestionService;

import com.example.TriageIQ.DTO.RequestDTO.EditAiReplyRequest;
import com.example.TriageIQ.DTO.RequestDTO.SendMessageRequest;
import com.example.TriageIQ.DTO.ResponseDTO.AiReplySuggestionResponse;
import com.example.TriageIQ.DTO.ResponseDTO.UserSummaryResponse;
import com.example.TriageIQ.Entity.*;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.Enum.SenderType;
import com.example.TriageIQ.Entity.Enum.SuggestionStatus;
import com.example.TriageIQ.Exception.ResourceNotFoundException;
import com.example.TriageIQ.Repository.AiReplySuggestionRepository;
import com.example.TriageIQ.Repository.TicketMessageRepository;
import com.example.TriageIQ.Repository.TicketRepository;
import com.example.TriageIQ.Repository.UserRepository;
import com.example.TriageIQ.Service.ChatService.ChatService;
import com.example.TriageIQ.Service.ai.AiOrchestrationService.AiOrchestrationService;
import com.example.TriageIQ.Service.ai.model.AiDecision;
import com.example.TriageIQ.Service.ai.model.ReplySuggestionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplySuggestionServiceImpl implements ReplySuggestionService{
    private final AiOrchestrationService aiOrchestrationService;
    private final AiReplySuggestionRepository suggestionRepository;
    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    @Override
    @Transactional
    public AiReplySuggestionResponse generateSuggestion(Long ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);

        TicketMessage latestCustomerMessage = messageRepository
                .findFirstByTicketIdAndSenderTypeOrderBySentAtDesc(ticketId, SenderType.CUSTOMER);

        if (latestCustomerMessage == null) {
            throw new IllegalStateException("No customer message to respond to yet");
        }

        List<String> history = messageRepository.findByTicketIdOrderBySentAtAsc(ticketId).stream()
                .map(m -> m.getSenderType() + ": " + m.getContent())
                .toList();

        AiDecision<ReplySuggestionResult> decision = aiOrchestrationService.suggestReply(
                ticket.getSubject(), history, latestCustomerMessage.getContent());

        if (!decision.isSuccess()) {
            throw new IllegalStateException("AI reply generation failed: " + decision.getErrorMessage());
        }

        AiReplySuggestion suggestion = AiReplySuggestion.builder()
                .ticket(ticket)
                .suggestedReply(decision.getResult().getSuggestedText())
                .status(SuggestionStatus.GENERATED)
                .build();

        AiReplySuggestion saved = suggestionRepository.save(suggestion);

        saved.setStatus(SuggestionStatus.PENDING);

        return toResponse(suggestionRepository.save(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public AiReplySuggestionResponse getLatestPending(Long ticketId) {
        return suggestionRepository
                .findFirstByTicketIdAndStatusOrderByCreatedAtDesc(
                        ticketId,
                        SuggestionStatus.PENDING)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No pending suggestion for this ticket"));
    }

    @Override
    @Transactional
    public AiReplySuggestionResponse editSuggestion(
            Long suggestionId, EditAiReplyRequest request, Long reviewedByUserId) {

        AiReplySuggestion suggestion = getSuggestionOrThrow(suggestionId);
        User reviewer = getAgentOrAdminOrThrow(reviewedByUserId);

        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending suggestions can be edited");
        }

        suggestion.setEditedReply(request.getEditedReply());
        suggestion.setStatus(SuggestionStatus.EDITED);
        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewedAt(LocalDateTime.now());

        return toResponse(suggestionRepository.save(suggestion));
    }

    @Override
    @Transactional
    public void acceptAndSend(Long suggestionId, Long reviewedByUserId) {
        AiReplySuggestion suggestion = getSuggestionOrThrow(suggestionId);
        User reviewer = getAgentOrAdminOrThrow(reviewedByUserId);

        if (suggestion.getStatus() != SuggestionStatus.PENDING
                && suggestion.getStatus() != SuggestionStatus.EDITED) {
            throw new IllegalStateException(
                    "Only pending or edited suggestions can be accepted");
        }

        String finalText = suggestion.getEditedReply() != null
                ? suggestion.getEditedReply()
                : suggestion.getSuggestedReply();

        suggestion.setStatus(SuggestionStatus.ACCEPTED);
        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewedAt(LocalDateTime.now());
        suggestionRepository.save(suggestion);

        chatService.sendMessage(
                suggestion.getTicket().getId(),
                SendMessageRequest.builder().content(finalText).build(),
                reviewer.getId(),
                reviewer.getRole());
    }

    @Override
    @Transactional
    public void reject(Long suggestionId, Long reviewedByUserId) {
        AiReplySuggestion suggestion = getSuggestionOrThrow(suggestionId);
        User reviewer = getAgentOrAdminOrThrow(reviewedByUserId);

        if (suggestion.getStatus() != SuggestionStatus.PENDING
                && suggestion.getStatus() != SuggestionStatus.EDITED) {
            throw new IllegalStateException(
                    "Only pending or edited suggestions can be rejected");
        }

        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewedAt(LocalDateTime.now());
        suggestionRepository.save(suggestion);
    }

    private AiReplySuggestion getSuggestionOrThrow(Long id) {
        return suggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Suggestion not found: " + id));
    }

    private User getAgentOrAdminOrThrow(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userId));

        if (user.getRole() == Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Only agents or admins can review AI suggestions");
        }

        return user;
    }

    private Ticket getTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found: " + ticketId));
    }

    private AiReplySuggestionResponse toResponse(AiReplySuggestion s) {
        return AiReplySuggestionResponse.builder()
                .id(s.getId())
                .ticketId(s.getTicket().getId())
                .suggestedReply(s.getSuggestedReply())
                .editedReply(s.getEditedReply())
                .status(s.getStatus())
                .reviewedBy(s.getReviewedBy() != null
                        ? toUserSummary(s.getReviewedBy())
                        : null)
                .reviewedAt(s.getReviewedAt())
                .createdAt(s.getCreatedAt())
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
