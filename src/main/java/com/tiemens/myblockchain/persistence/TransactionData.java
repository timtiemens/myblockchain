package com.tiemens.myblockchain.persistence;

/**
 * Plain, mutable mirror of {@link com.tiemens.myblockchain.core.Transaction} used only for
 * JSON (de)serialization. Kept separate from the domain class so the domain model stays
 * immutable and Gson never has to bypass a constructor via reflection to build one.
 */
class TransactionData {

    String sender;
    String recipient;
    double amount;
    long timestamp;
    String signature;

    TransactionData() {
    }

    TransactionData(String sender, String recipient, double amount, long timestamp, String signature) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.timestamp = timestamp;
        this.signature = signature;
    }
}
