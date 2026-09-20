package com.example.TriageIQ.Repository;

import com.example.TriageIQ.Entity.Enum.Priority;
import com.example.TriageIQ.Entity.Enum.TicketStatus;
import com.example.TriageIQ.Entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Page<Ticket> findByCustomerId(Long customerId, Pageable pageable);

    List<Ticket> findByAssignedAgentIsNullAndAssignedDepartmentId(Long departmentId);

    List<Ticket> findByAssignedDepartmentIsNull();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedAgent.id = :agentId AND t.status IN :openStatuses")
    long countOpenTicketsForAgent(
            @Param("agentId") Long agentId,
            @Param("openStatuses") List<TicketStatus> openStatuses
    );
}

//    Page<Ticket> findByAssignedAgentId(Long agentId, Pageable pageable);
//
//    Page<Ticket> findByAssignedDepartmentId(Long departmentId, Pageable pageable);
//
//    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
//
//    Page<Ticket> findByPriority(Priority priority, Pageable pageable);
//
//    Page<Ticket> findByAssignedAgentIdAndStatus(Long agentId, TicketStatus status, Pageable pageable);
//
//    Page<Ticket> findByAssignedDepartmentIdAndStatus(Long departmentId, TicketStatus status, Pageable pageable);
//
//    Page<Ticket> findByAssignedAgentIdAndPriority(Long agentId, Priority priority, Pageable pageable);
//
//    Page<Ticket> findByAssignedDepartmentIdAndPriority(Long departmentId, Priority priority, Pageable pageable);