package com.example.TriageIQ.Repository;

import com.example.TriageIQ.Entity.TicketStatusHistory;
import com.example.TriageIQ.Entity.Enum.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistory, Long>{
    List<TicketStatusHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);

    List<TicketStatusHistory> findByChangedByIdOrderByChangedAtDesc(Long userId);

    Page<TicketStatusHistory> findByTicketId(
            Long ticketId,
            Pageable pageable
    );

    Page<TicketStatusHistory> findByTicketIdAndNewStatus(
            Long ticketId,
            TicketStatus newStatus,
            Pageable pageable
    );

    Page<TicketStatusHistory> findByChangedById(
            Long userId,
            Pageable pageable
    );
}
