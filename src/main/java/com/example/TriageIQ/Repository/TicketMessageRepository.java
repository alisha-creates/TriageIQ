package com.example.TriageIQ.Repository;

import com.example.TriageIQ.Entity.Enum.SenderType;
import com.example.TriageIQ.Entity.TicketMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    List<TicketMessage> findByTicketIdOrderBySentAtAsc(Long ticketId);

    List<TicketMessage> findByTicketIdOrderBySentAtDesc(Long ticketId);

    TicketMessage findFirstByTicketIdAndSenderTypeOrderBySentAtDesc(
            Long ticketId,
            SenderType senderType
    );

    Page<TicketMessage> findByTicketId(
            Long ticketId,
            Pageable pageable
    );

    Page<TicketMessage> findByTicketIdAndSenderType(
            Long ticketId,
            SenderType senderType,
            Pageable pageable
    );

    Page<TicketMessage> findByTicketIdAndContentContainingIgnoreCase(
            Long ticketId,
            String content,
            Pageable pageable
    );
}
