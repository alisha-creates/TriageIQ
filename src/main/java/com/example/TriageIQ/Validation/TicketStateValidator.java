package com.example.TriageIQ.Validation;

import com.example.TriageIQ.Entity.Enum.TicketStatus;
import com.example.TriageIQ.Entity.Ticket;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TicketStateValidator {
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TicketStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TicketStatus.OPEN,
                EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CLOSED));

        ALLOWED_TRANSITIONS.put(TicketStatus.IN_PROGRESS,
                EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));

        ALLOWED_TRANSITIONS.put(TicketStatus.RESOLVED,
                EnumSet.of(TicketStatus.CLOSED, TicketStatus.REOPENED));

        ALLOWED_TRANSITIONS.put(TicketStatus.REOPENED,
                EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CLOSED));

        ALLOWED_TRANSITIONS.put(TicketStatus.CLOSED,
                EnumSet.of(TicketStatus.REOPENED));
    }

    public void validateTransition(TicketStatus from, TicketStatus to) {
        if (from == to) {
            throw new IllegalArgumentException("Ticket is already " + to);
        }

        Set<TicketStatus> allowedNext = ALLOWED_TRANSITIONS.get(from);
        if (allowedNext == null || !allowedNext.contains(to)) {
            throw new IllegalStateException(
                    "Cannot transition ticket from " + from + " to " + to
                            + ". Allowed next states: " + (allowedNext == null ? "none" : allowedNext));
        }
    }

    public void validateResolutionRequiresAssignment(Ticket ticket, TicketStatus newStatus) {
        boolean isClosingTransition = newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED;

        if (isClosingTransition && ticket.getAssignedAgent() == null) {
            throw new IllegalStateException(
                    "Cannot mark an unassigned ticket as " + newStatus + ". Assign an agent first.");
        }
    }
}
