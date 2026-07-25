package com.offlineupi.offline_upi_server.controller;

import com.offlineupi.offline_upi_server.dto.PaymentRequest;
import com.offlineupi.offline_upi_server.dto.PaymentResponse;
import com.offlineupi.offline_upi_server.entity.Account;
import com.offlineupi.offline_upi_server.entity.Payment;
import com.offlineupi.offline_upi_server.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/send")
    public PaymentResponse sendPayment(@RequestBody PaymentRequest request) {
        return paymentService.processOfflineRelayPayment(request);
    }

    @PostMapping("/relay-send")
    public PaymentResponse relaySendPayment(@RequestBody PaymentRequest request) {
        return paymentService.processOfflineRelayPayment(request);
    }

    @GetMapping("/history")
    public List<Payment> getHistory() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/accounts")
    public List<Account> getAccounts() {
        return paymentService.getAllAccounts();
    }
}