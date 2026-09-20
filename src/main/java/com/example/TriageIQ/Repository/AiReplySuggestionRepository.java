package com.example.TriageIQ.Repository;

import org.springframework.stereotype.Repository;
import com.example.TriageIQ.Entity.AiReplySuggestion;
import com.example.TriageIQ.Entity.Enum.SuggestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiReplySuggestionRepository extends JpaRepository<AiReplySuggestion, Long>{
    List<AiReplySuggestion> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    Optional<AiReplySuggestion> findFirstByTicketIdAndStatusOrderByCreatedAtDesc(
            Long ticketId, SuggestionStatus status);



    Page<AiReplySuggestion> findByStatus(SuggestionStatus status, Pageable pageable);

    Page<AiReplySuggestion> findByTicketId(
            Long ticketId,
            Pageable pageable
    );

    Page<AiReplySuggestion> findByTicketIdAndStatus(
            Long ticketId,
            SuggestionStatus status,
            Pageable pageable
    );
}


