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

    /**
     * Reconstructs a block exactly as it was persisted, preserving its original timestamp,
     * nonce and hash rather than re-mining it. Used when loading a chain from storage.
     */
    public static Block restore(List<Transaction> transactions, String previousHash,
                                 long timestamp, long nonce, String hash) {
        return new Block(transactions, previousHash, timestamp, nonce, hash);
    }

    private Block(List<Transaction> transactions, String previousHash,
                   long timestamp, long nonce, String hash) {
        this.timestamp = timestamp;
        this.transactions = new ArrayList<>(transactions);
        this.previousHash = previousHash;
        this.nonce = nonce;
        this.hash = hash;
    }

    public String calculateHash() {
        return CryptoUtil.sha256(previousHash + timestamp + getMerkleRoot() + nonce);
    }

    public String getMerkleRoot() {
        return MerkleTree.computeRoot(transactions);
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
