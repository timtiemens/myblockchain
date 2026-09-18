package com.tiemens.myblockchain.persistence;

import java.util.List;

/**
 * Plain, mutable mirror of {@link com.tiemens.myblockchain.core.Blockchain} used only for
 * JSON (de)serialization.
 */
class BlockchainData {

    int difficulty;
    double miningReward;
    List<BlockData> chain;
    List<TransactionData> pendingTransactions;

    BlockchainData() {
    }

    BlockchainData(int difficulty, double miningReward, List<BlockData> chain, List<TransactionData> pendingTransactions) {
        this.difficulty = difficulty;
        this.miningReward = miningReward;
        this.chain = chain;
        this.pendingTransactions = pendingTransactions;
    }
}
