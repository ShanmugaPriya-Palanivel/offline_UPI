package com.offlineupi.offline_upi_server.service;

import com.offlineupi.offline_upi_server.dto.PaymentRequest;
import com.offlineupi.offline_upi_server.dto.PaymentResponse;
import com.offlineupi.offline_upi_server.entity.Account;
import com.offlineupi.offline_upi_server.entity.Payment;
import com.offlineupi.offline_upi_server.entity.User;
import com.offlineupi.offline_upi_server.repository.AccountRepository;
import com.offlineupi.offline_upi_server.repository.PaymentRepository;
import com.offlineupi.offline_upi_server.repository.UserRepository;
import com.offlineupi.offline_upi_server.security.DigitalSignatureUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(AccountRepository accountRepository,
                          PaymentRepository paymentRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentResponse processOfflineRelayPayment(PaymentRequest request) {
        String nonce = request.getNonce();
        String relayNode = request.getRelayNodeId() != null ? request.getRelayNodeId() : "Peer-Unknown";

        // 1. DEDUPLICATION CHECK (Problem #2)
        if (nonce != null && !nonce.trim().isEmpty()) {
            Optional<Payment> existingPaymentOpt = paymentRepository.findByNonce(nonce);
            if (existingPaymentOpt.isPresent()) {
                Payment existingPayment = existingPaymentOpt.get();
                existingPayment.setDeduplicatedCount(existingPayment.getDeduplicatedCount() + 1);
                paymentRepository.save(existingPayment);

                PaymentResponse dupResponse = new PaymentResponse(
                        true,
                        "DUPLICATE_IGNORED",
                        nonce,
                        "Duplicate transaction request received from [" + relayNode + "]. Original transaction already processed via [" + existingPayment.getRelayNodeId() + "]."
                );
                dupResponse.setDeduplicated(true);
                dupResponse.setRelayNodeId(relayNode);
                dupResponse.setAmount(existingPayment.getAmount());
                dupResponse.setSenderUpiId(existingPayment.getSenderUpiId());
                dupResponse.setReceiverUpiId(existingPayment.getReceiverUpiId());
                return dupResponse;
            }
        }

        // 2. TTL EXPIRATION CHECK (Problem #3)
        long currentTime = System.currentTimeMillis();
        long timestamp = request.getTimestamp() != null ? request.getTimestamp() : currentTime;
        long ttlSeconds = request.getTtlSeconds() != null ? request.getTtlSeconds() : 300L;
        long expiryTime = timestamp + (ttlSeconds * 1000L);

        if (currentTime > expiryTime) {
            Payment failedPayment = new Payment();
            failedPayment.setNonce(nonce);
            failedPayment.setSenderUpiId(request.getSenderUpiId());
            failedPayment.setReceiverUpiId(request.getReceiverUpiId());
            failedPayment.setAmount(request.getAmount());
            failedPayment.setRelayNodeId(relayNode);
            failedPayment.setTimestamp(timestamp);
            failedPayment.setTtlSeconds(ttlSeconds);
            failedPayment.setStatus("EXPIRED");
            failedPayment.setFailureReason("TTL Exceeded (" + ttlSeconds + "s limit expired)");
            paymentRepository.save(failedPayment);

            PaymentResponse expiredResp = new PaymentResponse(
                    false,
                    "EXPIRED",
                    nonce,
                    "Transaction expired! Created at timestamp " + timestamp + " with TTL " + ttlSeconds + "s. Current time exceeds expiration."
            );
            expiredResp.setRelayNodeId(relayNode);
            return expiredResp;
        }

        // 3. ACCOUNT LOOKUP
        Account senderAccount = null;
        Account receiverAccount = null;

        if (request.getSenderUpiId() != null) {
            senderAccount = accountRepository.findByUserUpiId(request.getSenderUpiId()).orElse(null);
        }
        if (senderAccount == null && request.getSenderId() != null) {
            senderAccount = accountRepository.findById(request.getSenderId()).orElse(null);
        }

        if (request.getReceiverUpiId() != null) {
            receiverAccount = accountRepository.findByUserUpiId(request.getReceiverUpiId()).orElse(null);
        }
        if (receiverAccount == null && request.getReceiverId() != null) {
            receiverAccount = accountRepository.findById(request.getReceiverId()).orElse(null);
        }

        if (senderAccount == null || receiverAccount == null) {
            PaymentResponse notFoundResp = new PaymentResponse(
                    false,
                    "ACCOUNT_NOT_FOUND",
                    nonce,
                    "Sender or Receiver account not registered in bank server system."
            );
            notFoundResp.setRelayNodeId(relayNode);
            return notFoundResp;
        }

        // 4. DIGITAL SIGNATURE & SECURITY VERIFICATION (Problem #1)
        String publicKeyToUse = request.getPublicKey();
        if ((publicKeyToUse == null || publicKeyToUse.isEmpty()) && senderAccount.getUser() != null) {
            publicKeyToUse = senderAccount.getUser().getPublicKey();
        }

        if (request.getSignature() != null && !request.getSignature().isEmpty()) {
            String senderUpiStr = (request.getSenderUpiId() != null ? request.getSenderUpiId() : senderAccount.getId().toString());
            String receiverUpiStr = (request.getReceiverUpiId() != null ? request.getReceiverUpiId() : receiverAccount.getId().toString());
            String nonceStr = (nonce != null ? nonce : "");

            String amountFormatted2Dec = String.format(java.util.Locale.US, "%.2f", request.getAmount());
            String amountStandardDouble = String.valueOf(request.getAmount());
            String amountIntegerStr = (request.getAmount() % 1 == 0) ? String.format(java.util.Locale.US, "%.0f", request.getAmount()) : amountStandardDouble;

            String payloadData2Dec = senderUpiStr + "|" + receiverUpiStr + "|" + amountFormatted2Dec + "|" + nonceStr + "|" + timestamp;
            String payloadDataDouble = senderUpiStr + "|" + receiverUpiStr + "|" + amountStandardDouble + "|" + nonceStr + "|" + timestamp;
            String payloadDataInt = senderUpiStr + "|" + receiverUpiStr + "|" + amountIntegerStr + "|" + nonceStr + "|" + timestamp;

            boolean sigValid = false;
            try {
                if (publicKeyToUse != null && !publicKeyToUse.isEmpty()) {
                    sigValid = DigitalSignatureUtil.verifySignature(payloadData2Dec, request.getSignature(), publicKeyToUse)
                            || DigitalSignatureUtil.verifySignature(payloadDataInt, request.getSignature(), publicKeyToUse)
                            || DigitalSignatureUtil.verifySignature(payloadDataDouble, request.getSignature(), publicKeyToUse);
                }
            } catch (Exception e) {
                System.err.println(">>> Signature Verification Error: " + e.getMessage());
                e.printStackTrace();
                sigValid = false;
            }

            if (!sigValid) {
                Payment failedSigPayment = new Payment();
                failedSigPayment.setNonce(nonce);
                failedSigPayment.setSenderId(senderAccount.getId());
                failedSigPayment.setReceiverId(receiverAccount.getId());
                failedSigPayment.setSenderUpiId(senderAccount.getUser() != null ? senderAccount.getUser().getUpiId() : null);
                failedSigPayment.setReceiverUpiId(receiverAccount.getUser() != null ? receiverAccount.getUser().getUpiId() : null);
                failedSigPayment.setAmount(request.getAmount());
                failedSigPayment.setRelayNodeId(relayNode);
                failedSigPayment.setStatus("INVALID_SIGNATURE");
                failedSigPayment.setFailureReason("RSA Signature Verification Failed (Possible Tampering/Eavesdropping)");
                paymentRepository.save(failedSigPayment);

                PaymentResponse sigResp = new PaymentResponse(
                        false,
                        "INVALID_SIGNATURE",
                        nonce,
                        "Digital signature verification failed! Payload integrity or sender authenticity compromised."
                );
                sigResp.setRelayNodeId(relayNode);
                return sigResp;
            }
        }

        // 5. BALANCE CHECK
        if (senderAccount.getBalance() < request.getAmount()) {
            Payment failedBalPayment = new Payment();
            failedBalPayment.setNonce(nonce);
            failedBalPayment.setSenderId(senderAccount.getId());
            failedBalPayment.setReceiverId(receiverAccount.getId());
            failedBalPayment.setSenderUpiId(senderAccount.getUser() != null ? senderAccount.getUser().getUpiId() : null);
            failedBalPayment.setReceiverUpiId(receiverAccount.getUser() != null ? receiverAccount.getUser().getUpiId() : null);
            failedBalPayment.setAmount(request.getAmount());
            failedBalPayment.setRelayNodeId(relayNode);
            failedBalPayment.setStatus("INSUFFICIENT_FUNDS");
            failedBalPayment.setFailureReason("Sender balance (₹" + senderAccount.getBalance() + ") is lower than transfer amount (₹" + request.getAmount() + ")");
            paymentRepository.save(failedBalPayment);

            PaymentResponse balResp = new PaymentResponse(
                    false,
                    "INSUFFICIENT_FUNDS",
                    nonce,
                    "Insufficient account balance. Available: ₹" + senderAccount.getBalance() + ", Requested: ₹" + request.getAmount()
            );
            balResp.setRelayNodeId(relayNode);
            return balResp;
        }

        // 6. ATOMIC BALANCE SETTLEMENT
        senderAccount.setBalance(senderAccount.getBalance() - request.getAmount());
        receiverAccount.setBalance(receiverAccount.getBalance() + request.getAmount());

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        Payment payment = new Payment();
        payment.setSenderId(senderAccount.getId());
        payment.setReceiverId(receiverAccount.getId());
        payment.setSenderUpiId(senderAccount.getUser() != null ? senderAccount.getUser().getUpiId() : request.getSenderUpiId());
        payment.setReceiverUpiId(receiverAccount.getUser() != null ? receiverAccount.getUser().getUpiId() : request.getReceiverUpiId());
        payment.setAmount(request.getAmount());
        payment.setNonce(nonce);
        payment.setSignature(request.getSignature());
        payment.setTtlSeconds(ttlSeconds);
        payment.setTimestamp(timestamp);
        payment.setRelayNodeId(relayNode);
        payment.setStatus("SUCCESS");

        paymentRepository.save(payment);

        PaymentResponse response = new PaymentResponse(
                true,
                "SUCCESS",
                nonce,
                "Payment of ₹" + request.getAmount() + " successfully settled via Relay [" + relayNode + "]!"
        );
        response.setRelayNodeId(relayNode);
        response.setAmount(request.getAmount());
        response.setSenderUpiId(payment.getSenderUpiId());
        response.setReceiverUpiId(payment.getReceiverUpiId());
        response.setSenderBalance(senderAccount.getBalance());
        response.setReceiverBalance(receiverAccount.getBalance());
        response.setTimestamp(timestamp);

        return response;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public String transferMoney(Long senderId, Long receiverId, Double amount) {
        PaymentRequest request = new PaymentRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setAmount(amount);
        request.setRelayNodeId("Direct-Server");
        PaymentResponse response = processOfflineRelayPayment(request);
        return response.getMessage();
    }
}