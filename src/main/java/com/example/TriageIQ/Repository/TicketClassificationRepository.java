package com.example.TriageIQ.Repository;

import com.example.TriageIQ.Entity.Enum.Priority;
import com.example.TriageIQ.Entity.Enum.RoutingDecision;
import com.example.TriageIQ.Entity.TicketClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketClassificationRepository extends JpaRepository<TicketClassification, Long> {
    Optional<TicketClassification> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);

    Page<TicketClassification> findBySuggestedPriority(Priority suggestedPriority, Pageable pageable);

    Page<TicketClassification> findByRoutingDecision(
            RoutingDecision routingDecision,
            Pageable pageable
    );
}
