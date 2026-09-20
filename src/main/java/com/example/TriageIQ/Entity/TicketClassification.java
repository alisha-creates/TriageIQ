package com.example.TriageIQ.Entity;

import com.example.TriageIQ.Entity.Enum.Priority;
import com.example.TriageIQ.Entity.Enum.RoutingDecision;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_classifications", indexes = {
        @Index(name = "idx_classification_ticket", columnList = "ticket_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketClassification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_priority", length = 20)
    private Priority suggestedPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "routing_decision", length = 30)
    private RoutingDecision routingDecision;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reasoning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime classifiedAt;
}
