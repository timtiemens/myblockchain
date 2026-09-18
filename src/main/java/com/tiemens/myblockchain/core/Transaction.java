package com.tiemens.myblockchain.core;

import com.tiemens.myblockchain.util.CryptoUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

public class Transaction {

    private final String sender;
    private final String recipient;
    private final double amount;
    private final long timestamp;
    private String signature;

    public Transaction(String sender, String recipient, double amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Reconstructs a transaction with a known timestamp and signature. Used when restoring a
     * transaction from persisted storage, where the original signature must be kept as-is
     * (the sender's private key needed to re-sign it isn't available at load time).
     */
    public Transaction(String sender, String recipient, double amount, long timestamp, String signature) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.timestamp = timestamp;
        this.signature = signature;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public String calculateHash() {
        return CryptoUtil.sha256(sender + recipient + amount + timestamp);
    }

    public void signTransaction(PrivateKey privateKey) {
        this.signature = CryptoUtil.sign(privateKey, calculateHash());
    }

    /** Coinbase/reward transactions have no sender and skip signature verification. */
    public boolean isValid() {
        if (sender == null) {
            return true;
        }
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        PublicKey senderKey = CryptoUtil.decodePublicKey(sender);
        return CryptoUtil.verify(senderKey, calculateHash(), signature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return amount == that.amount
                && timestamp == that.timestamp
                && Objects.equals(sender, that.sender)
                && Objects.equals(recipient, that.recipient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, recipient, amount, timestamp);
    }

    @Override
    public String toString() {
        String from = sender == null ? "COINBASE" : sender.substring(0, Math.min(16, sender.length()));
        return String.format("Transaction{%s -> %s, amount=%.4f}", from,
                recipient.substring(0, Math.min(16, recipient.length())), amount);
    }
}
