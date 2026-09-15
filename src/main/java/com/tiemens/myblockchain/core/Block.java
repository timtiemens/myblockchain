package com.tiemens.myblockchain.core;

import com.tiemens.myblockchain.util.CryptoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Block {

    private final long timestamp;
    private final List<Transaction> transactions;
    private final String previousHash;
    private String hash;
    private long nonce;

    public Block(List<Transaction> transactions, String previousHash) {
        this.timestamp = System.currentTimeMillis();
        this.transactions = new ArrayList<>(transactions);
        this.previousHash = previousHash;
        this.nonce = 0;
        this.hash = calculateHash();
    }

    public String calculateHash() {
        StringBuilder txData = new StringBuilder();
        for (Transaction tx : transactions) {
            txData.append(tx.calculateHash());
        }
        return CryptoUtil.sha256(previousHash + timestamp + txData + nonce);
    }

    /** Proof-of-work: increments nonce until the hash has `difficulty` leading zero hex digits. */
    public void mineBlock(int difficulty) {
        String target = "0".repeat(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    public boolean hasValidTransactions() {
        return transactions.stream().allMatch(Transaction::isValid);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public long getNonce() {
        return nonce;
    }
}
