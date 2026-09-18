package com.tiemens.myblockchain.persistence;

import com.tiemens.myblockchain.core.Blockchain;
import com.tiemens.myblockchain.core.Transaction;
import com.tiemens.myblockchain.core.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockchainStorageTest {

    @Test
    void roundTripPreservesChainBalancesAndPendingPool(@TempDir Path tempDir) throws IOException {
        Blockchain blockchain = new Blockchain(2, 100);
        Wallet alice = new Wallet();
        Wallet bob = new Wallet();
        Wallet miner = new Wallet();

        blockchain.minePendingTransactions(alice.getAddress());
        Transaction aliceToBob = alice.createTransaction(bob.getAddress(), 25);
        blockchain.addTransaction(aliceToBob);
        blockchain.minePendingTransactions(miner.getAddress());

        // Queue a transaction that never gets mined, to confirm the pending pool round-trips too.
        Transaction bobToAlice = bob.createTransaction(alice.getAddress(), 5);
        blockchain.addTransaction(bobToAlice);

        Path file = tempDir.resolve("chain.json");
        BlockchainStorage.save(blockchain, file);

        Blockchain loaded = BlockchainStorage.load(file);

        assertTrue(loaded.isChainValid());
        assertEquals(blockchain.getDifficulty(), loaded.getDifficulty());
        assertEquals(blockchain.getMiningReward(), loaded.getMiningReward());
        assertEquals(blockchain.getChain().size(), loaded.getChain().size());
        assertEquals(blockchain.getPendingTransactions().size(), loaded.getPendingTransactions().size());

        assertEquals(alice.getBalance(blockchain), alice.getBalance(loaded));
        assertEquals(bob.getBalance(blockchain), bob.getBalance(loaded));
        assertEquals(miner.getBalance(blockchain), miner.getBalance(loaded));

        for (int i = 0; i < blockchain.getChain().size(); i++) {
            assertEquals(blockchain.getChain().get(i).getHash(), loaded.getChain().get(i).getHash());
            assertEquals(blockchain.getChain().get(i).getNonce(), loaded.getChain().get(i).getNonce());
        }
    }

    @Test
    void loadingMissingFileThrows(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.json");
        assertThrows(IOException.class, () -> BlockchainStorage.load(missing));
    }

    @Test
    void loadRejectsEmptyChain(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("empty.json");
        Files.writeString(file, "{\"difficulty\":2,\"miningReward\":100,\"chain\":[],\"pendingTransactions\":[]}");
        assertThrows(IOException.class, () -> BlockchainStorage.load(file));
    }
}
