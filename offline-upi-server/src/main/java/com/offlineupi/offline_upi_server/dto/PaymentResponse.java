package com.offlineupi.offline_upi_server.dto;

public class PaymentResponse {

    private boolean success;
    private String status; // SUCCESS, DUPLICATE_IGNORED, EXPIRED, INVALID_SIGNATURE, INSUFFICIENT_FUNDS, ACCOUNT_NOT_FOUND
    private String transactionId;
    private String message;
    private boolean deduplicated;
    private String relayNodeId;
    private Double amount;
    private String senderUpiId;
    private String receiverUpiId;
    private Double senderBalance;
    private Double receiverBalance;
    private Long timestamp;

    public PaymentResponse() {
    }

    public PaymentResponse(boolean success, String status, String transactionId, String message) {
        this.success = success;
        this.status = status;
        this.transactionId = transactionId;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDeduplicated() {
        return deduplicated;
    }

    public void setDeduplicated(boolean deduplicated) {
        this.deduplicated = deduplicated;
    }

    public String getRelayNodeId() {
        return relayNodeId;
    }

    public void setRelayNodeId(String relayNodeId) {
        this.relayNodeId = relayNodeId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSenderUpiId() {
        return senderUpiId;
    }

    public void setSenderUpiId(String senderUpiId) {
        this.senderUpiId = senderUpiId;
    }

    public String getReceiverUpiId() {
        return receiverUpiId;
    }

    public void setReceiverUpiId(String receiverUpiId) {
        this.receiverUpiId = receiverUpiId;
    }

    public Double getSenderBalance() {
        return senderBalance;
    }

    public void setSenderBalance(Double senderBalance) {
        this.senderBalance = senderBalance;
    }

    public Double getReceiverBalance() {
        return receiverBalance;
    }

    public void setReceiverBalance(Double receiverBalance) {
        this.receiverBalance = receiverBalance;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
