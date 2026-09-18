package com.tiemens.myblockchain.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tiemens.myblockchain.core.Block;
import com.tiemens.myblockchain.core.Blockchain;
import com.tiemens.myblockchain.core.Transaction;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves a {@link Blockchain} to a JSON file and loads it back.
 *
 * <p>Loaded blocks are reconstructed exactly as they were persisted: original timestamp,
 * nonce and hash are preserved rather than re-mined, and loaded transactions keep their
 * original signature rather than being re-signed (the sender's private key isn't available
 * at load time). Call {@code isChainValid()} on the result if you want to confirm the
 * persisted file wasn't corrupted or tampered with.</p>
 */
public final class BlockchainStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private BlockchainStorage() {
    }

    public static void save(Blockchain blockchain, Path path) throws IOException {
        BlockchainData data = toData(blockchain);
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(data, writer);
        }
    }

    public static Blockchain load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            BlockchainData data = GSON.fromJson(reader, BlockchainData.class);
            if (data == null || data.chain == null || data.chain.isEmpty()) {
                throw new IOException("No valid blockchain found in " + path);
            }
            return toBlockchain(data);
        }
    }

    private static BlockchainData toData(Blockchain blockchain) {
        List<BlockData> chainData = new ArrayList<>();
        for (Block block : blockchain.getChain()) {
            chainData.add(toData(block));
        }
        List<TransactionData> pendingData = new ArrayList<>();
        for (Transaction tx : blockchain.getPendingTransactions()) {
            pendingData.add(toData(tx));
        }
        return new BlockchainData(blockchain.getDifficulty(), blockchain.getMiningReward(), chainData, pendingData);
    }

    private static Blockchain toBlockchain(BlockchainData data) {
        List<Block> chain = new ArrayList<>();
        for (BlockData blockData : data.chain) {
            chain.add(toBlock(blockData));
        }
        List<Transaction> pending = new ArrayList<>();
        if (data.pendingTransactions != null) {
            for (TransactionData txData : data.pendingTransactions) {
                pending.add(toTransaction(txData));
            }
        }
        return Blockchain.restore(data.difficulty, data.miningReward, chain, pending);
    }

    private static BlockData toData(Block block) {
        List<TransactionData> transactions = new ArrayList<>();
        for (Transaction tx : block.getTransactions()) {
            transactions.add(toData(tx));
        }
        return new BlockData(block.getTimestamp(), transactions, block.getPreviousHash(), block.getHash(), block.getNonce());
    }

    private static Block toBlock(BlockData data) {
        List<Transaction> transactions = new ArrayList<>();
        if (data.transactions != null) {
            for (TransactionData txData : data.transactions) {
                transactions.add(toTransaction(txData));
            }
        }
        return Block.restore(transactions, data.previousHash, data.timestamp, data.nonce, data.hash);
    }

    private static TransactionData toData(Transaction tx) {
        return new TransactionData(tx.getSender(), tx.getRecipient(), tx.getAmount(), tx.getTimestamp(), tx.getSignature());
    }

    private static Transaction toTransaction(TransactionData data) {
        return new Transaction(data.sender, data.recipient, data.amount, data.timestamp, data.signature);
    }
}
