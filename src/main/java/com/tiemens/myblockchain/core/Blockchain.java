package com.tiemens.myblockchain.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Blockchain {

    private final List<Block> chain = new ArrayList<>();
    private final List<Transaction> pendingTransactions = new ArrayList<>();
    private final int difficulty;
    private final double miningReward;

    public Blockchain(int difficulty, double miningReward) {
        this.difficulty = difficulty;
        this.miningReward = miningReward;
        chain.add(createGenesisBlock());
    }

    /**
     * Reconstructs a blockchain from persisted state (e.g. loaded from disk) instead of
     * starting a fresh chain with a new genesis block.
     */
    public static Blockchain restore(int difficulty, double miningReward,
                                      List<Block> chain, List<Transaction> pendingTransactions) {
        return new Blockchain(difficulty, miningReward, chain, pendingTransactions);
    }

    private Blockchain(int difficulty, double miningReward,
                        List<Block> chain, List<Transaction> pendingTransactions) {
        if (chain == null || chain.isEmpty()) {
            throw new IllegalArgumentException("Restored chain must contain at least the genesis block");
        }
        this.difficulty = difficulty;
        this.miningReward = miningReward;
        this.chain.addAll(chain);
        this.pendingTransactions.addAll(pendingTransactions);
    }

    private Block createGenesisBlock() {
        return new Block(Collections.emptyList(), "0");
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addTransaction(Transaction transaction) {
        if (transaction.getSender() == null || transaction.getRecipient() == null) {
            throw new IllegalArgumentException("Transaction must include sender and recipient");
        }
        if (!transaction.isValid()) {
            throw new IllegalArgumentException("Cannot add invalid transaction");
        }
        pendingTransactions.add(transaction);
    }

    /** Mines all pending transactions into a new block and rewards the miner. */
    public Block minePendingTransactions(String minerAddress) {
        Transaction rewardTx = new Transaction(null, minerAddress, miningReward);
        List<Transaction> transactions = new ArrayList<>(pendingTransactions);
        transactions.add(rewardTx);

        Block block = new Block(transactions, getLatestBlock().getHash());
        block.mineBlock(difficulty);

        chain.add(block);
        pendingTransactions.clear();
        return block;
    }

    public double getBalance(String address) {
        double balance = 0;
        for (Block block : chain) {
            for (Transaction tx : block.getTransactions()) {
                if (address.equals(tx.getSender())) {
                    balance -= tx.getAmount();
                }
                if (address.equals(tx.getRecipient())) {
                    balance += tx.getAmount();
                }
            }
        }
        return balance;
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);

            if (!current.getHash().equals(current.calculateHash())) {
                return false;
            }
            if (!current.getPreviousHash().equals(previous.getHash())) {
                return false;
            }
            if (!current.hasValidTransactions()) {
                return false;
            }
            String target = "0".repeat(difficulty);
            if (!current.getHash().substring(0, difficulty).equals(target)) {
                return false;
            }
        }
        return true;
    }

    public List<Block> getChain() {
        return Collections.unmodifiableList(chain);
    }

    public List<Transaction> getPendingTransactions() {
        return Collections.unmodifiableList(pendingTransactions);
    }

    public int getDifficulty() {
        return difficulty;
    }

    public double getMiningReward() {
        return miningReward;
    }
}
