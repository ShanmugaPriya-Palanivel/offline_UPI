package com.offlineupi.offline_upi_server.repository;

import com.offlineupi.offline_upi_server.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import com.offlineupi.offline_upi_server.entity.User;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUser(User user);
    Optional<Account> findByUserUpiId(String upiId);
}