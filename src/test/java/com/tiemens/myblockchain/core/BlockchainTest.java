package com.tiemens.myblockchain.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockchainTest {

    private Blockchain blockchain;
    private Wallet alice;
    private Wallet bob;
    private Wallet miner;

    @BeforeEach
    void setUp() {
        blockchain = new Blockchain(2, 100);
        alice = new Wallet();
        bob = new Wallet();
        miner = new Wallet();
    }

    @Test
    void genesisChainIsValid() {
        assertTrue(blockchain.isChainValid());
        assertEquals(1, blockchain.getChain().size());
    }

    @Test
    void miningRewardsMinerAndIsValid() {
        blockchain.minePendingTransactions(miner.getAddress());
        assertEquals(100, miner.getBalance(blockchain));
        assertTrue(blockchain.isChainValid());
    }

    @Test
    void transactionMovesFundsBetweenWallets() {
        blockchain.minePendingTransactions(alice.getAddress());
        assertEquals(100, alice.getBalance(blockchain));

        Transaction tx = alice.createTransaction(bob.getAddress(), 40);
        blockchain.addTransaction(tx);
        blockchain.minePendingTransactions(miner.getAddress());

        assertEquals(60, alice.getBalance(blockchain));
        assertEquals(40, bob.getBalance(blockchain));
        assertEquals(100, miner.getBalance(blockchain));
        assertTrue(blockchain.isChainValid());
    }

    @Test
    void unsignedTransactionIsInvalid() {
        Transaction forged = new Transaction(alice.getAddress(), bob.getAddress(), 40);
        assertFalse(forged.isValid());
        assertThrows(IllegalArgumentException.class, () -> blockchain.addTransaction(forged));
    }

    @Test
    void transactionSignedByWrongKeyIsInvalid() {
        // signed with an unrelated key while claiming to be from alice
        Transaction forged = new Transaction(alice.getAddress(), bob.getAddress(), 40);
        forged.signTransaction(com.tiemens.myblockchain.util.CryptoUtil.generateKeyPair().getPrivate());
        assertFalse(forged.isValid());
    }

    @Test
    void proofOfWorkProducesHashWithLeadingZeros() {
        Block block = blockchain.minePendingTransactions(miner.getAddress());
        String target = "0".repeat(blockchain.getDifficulty());
        assertTrue(block.getHash().startsWith(target));
    }
}
