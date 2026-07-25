package com.offlineupi.offline_upi_server.repository;

import com.offlineupi.offline_upi_server.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByNonce(String nonce);
}