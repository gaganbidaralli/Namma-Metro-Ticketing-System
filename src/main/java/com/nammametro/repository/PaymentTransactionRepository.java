package com.nammametro.repository;

import com.nammametro.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTransactionId(String transactionId);
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);
    List<PaymentTransaction> findByTicketId(Long ticketId);
    Optional<PaymentTransaction> findByTicketNumber(String ticketNumber);
}
