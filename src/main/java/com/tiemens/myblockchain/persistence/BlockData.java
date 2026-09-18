package com.tiemens.myblockchain.persistence;

import java.util.List;

/**
 * Plain, mutable mirror of {@link com.tiemens.myblockchain.core.Block} used only for JSON
 * (de)serialization.
 */
class BlockData {

    long timestamp;
    List<TransactionData> transactions;
    String previousHash;
    String hash;
    long nonce;

    BlockData() {
    }

    BlockData(long timestamp, List<TransactionData> transactions, String previousHash, String hash, long nonce) {
        this.timestamp = timestamp;
        this.transactions = transactions;
        this.previousHash = previousHash;
        this.hash = hash;
        this.nonce = nonce;
    }
}
