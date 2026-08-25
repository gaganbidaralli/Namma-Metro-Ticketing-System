package com.nammametro.repository;

import com.nammametro.model.Ticket;
import com.nammametro.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    List<Ticket> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Ticket> findByStatusAndValidUntilBefore(TicketStatus status, LocalDateTime now);
    long countByStatus(TicketStatus status);
}
