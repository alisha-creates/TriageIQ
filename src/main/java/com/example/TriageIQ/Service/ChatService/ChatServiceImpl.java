package com.example.TriageIQ.Service.ChatService;

import com.example.TriageIQ.DTO.RequestDTO.SendMessageRequest;
import com.example.TriageIQ.DTO.ResponseDTO.ChatMessageResponse;
import com.example.TriageIQ.DTO.ResponseDTO.UserSummaryResponse;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.Enum.SenderType;
import com.example.TriageIQ.Entity.Ticket;
import com.example.TriageIQ.Entity.TicketMessage;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Exception.ResourceNotFoundException;
import com.example.TriageIQ.Repository.TicketMessageRepository;
import com.example.TriageIQ.Repository.TicketRepository;
import com.example.TriageIQ.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService{
    private final TicketMessageRepository messageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC_PREFIX = "/topic/tickets/";

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(
            Long ticketId, SendMessageRequest request, Long senderUserId, Role senderRole) {

        Ticket ticket = getTicketOrThrow(ticketId);

        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + senderUserId));

        assertCanParticipate(ticket, senderUserId, senderRole);

        SenderType senderType = resolveSenderType(senderRole);

        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .sender(sender)
                .senderType(senderType)
                .content(request.getContent())
                .build();

        TicketMessage saved = messageRepository.save(message);

        ChatMessageResponse response = toResponse(saved);

        broadcast(ticketId, response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long ticketId, Long requestingUserId, Role requestingUserRole) {
        Ticket ticket = getTicketOrThrow(ticketId);

        assertCanParticipate(
                ticket,
                requestingUserId,
                requestingUserRole
        );

        return messageRepository
                .findByTicketIdOrderBySentAtAsc(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChatMessageResponse postSystemMessage(Long ticketId, String content) {
        Ticket ticket = getTicketOrThrow(ticketId);

        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .sender(null)
                .senderType(SenderType.SYSTEM)
                .content(content)
                .build();

        TicketMessage saved = messageRepository.save(message);

        ChatMessageResponse response = toResponse(saved);

        broadcast(ticketId, response);

        return response;
    }

    private SenderType resolveSenderType(Role role) {
        return switch (role) {
            case CUSTOMER -> SenderType.CUSTOMER;
            case AGENT, ADMIN -> SenderType.AGENT;
        };
    }

    private void assertCanParticipate(Ticket ticket, Long userId, Role role) {
        boolean isOwner = ticket.getCustomer().getId().equals(userId);
        boolean isAssignedAgent = ticket.getAssignedAgent() != null
                && ticket.getAssignedAgent().getId().equals(userId);

        if (role == Role.CUSTOMER && !isOwner) {
            throw new IllegalArgumentException("You do not have access to this ticket's chat");
        }
        if (role == Role.AGENT && !isOwner && !isAssignedAgent) {
            throw new IllegalArgumentException("You do not have access to this ticket's chat");
        }
    }

    private void broadcast(Long ticketId, ChatMessageResponse response) {
        messagingTemplate.convertAndSend(TOPIC_PREFIX + ticketId, response);
    }

    private Ticket getTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
    }

    private ChatMessageResponse toResponse(TicketMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .ticketId(message.getTicket().getId())
                .sender(message.getSender() != null ? toUserSummary(message.getSender()) : null)
                .senderType(message.getSenderType())
                .content(message.getContent())
                .sentAt(message.getSentAt())
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
