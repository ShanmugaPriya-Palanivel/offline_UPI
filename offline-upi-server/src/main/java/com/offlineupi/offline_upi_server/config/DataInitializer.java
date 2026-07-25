package com.offlineupi.offline_upi_server.config;

import com.offlineupi.offline_upi_server.entity.Account;
import com.offlineupi.offline_upi_server.entity.User;
import com.offlineupi.offline_upi_server.repository.AccountRepository;
import com.offlineupi.offline_upi_server.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                         AccountRepository accountRepository,
                                         PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                // User 1: Alice
                User alice = new User();
                alice.setName("Alice Sharma");
                alice.setPhone("9876543210");
                alice.setEmail("alice@example.com");
                alice.setUpiId("alice@upi");
                alice.setPassword(passwordEncoder.encode("password123"));
                userRepository.save(alice);

                Account aliceAccount = new Account();
                aliceAccount.setAccountNumber("ACC-ALICE-101");
                aliceAccount.setBankName("State Bank of India");
                aliceAccount.setBalance(5000.0);
                aliceAccount.setUser(alice);
                accountRepository.save(aliceAccount);

                // User 2: Bob
                User bob = new User();
                bob.setName("Bob Kumar");
                bob.setPhone("9876543211");
                bob.setEmail("bob@example.com");
                bob.setUpiId("bob@upi");
                bob.setPassword(passwordEncoder.encode("password123"));
                userRepository.save(bob);

                Account bobAccount = new Account();
                bobAccount.setAccountNumber("ACC-BOB-102");
                bobAccount.setBankName("HDFC Bank");
                bobAccount.setBalance(1000.0);
                bobAccount.setUser(bob);
                accountRepository.save(bobAccount);

                // User 3: Merchant
                User merchant = new User();
                merchant.setName("SuperMart Store");
                merchant.setPhone("9876543212");
                merchant.setEmail("store@example.com");
                merchant.setUpiId("merchant@upi");
                merchant.setPassword(passwordEncoder.encode("password123"));
                userRepository.save(merchant);

                Account merchantAccount = new Account();
                merchantAccount.setAccountNumber("ACC-MERCHANT-103");
                merchantAccount.setBankName("ICICI Bank");
                merchantAccount.setBalance(500.0);
                merchantAccount.setUser(merchant);
                accountRepository.save(merchantAccount);

                System.out.println(">>> Sample Users & Accounts successfully seeded into Offline UPI Server!");
            }
        };
    }
}
