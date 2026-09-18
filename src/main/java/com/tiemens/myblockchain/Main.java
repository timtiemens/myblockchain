package com.tiemens.myblockchain;

import com.tiemens.myblockchain.core.Blockchain;
import com.tiemens.myblockchain.core.Transaction;
import com.tiemens.myblockchain.core.Wallet;
import com.tiemens.myblockchain.persistence.BlockchainStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        Blockchain blockchain = new Blockchain(4, 100);

        Wallet alice = new Wallet();
        Wallet bob = new Wallet();
        Wallet miner = new Wallet();

        System.out.println("Mining initial block to fund Alice...");
        blockchain.minePendingTransactions(alice.getAddress());

        System.out.println("Alice balance: " + alice.getBalance(blockchain));

        Transaction tx = alice.createTransaction(bob.getAddress(), 25);
        blockchain.addTransaction(tx);

        System.out.println("Mining block with Alice -> Bob transaction...");
        blockchain.minePendingTransactions(miner.getAddress());

        System.out.println("Alice balance: " + alice.getBalance(blockchain));
        System.out.println("Bob balance: " + bob.getBalance(blockchain));
        System.out.println("Miner balance: " + miner.getBalance(blockchain));

        System.out.println("Chain valid? " + blockchain.isChainValid());

        Path savedChain = Files.createTempFile("myblockchain", ".json");
        try {
            BlockchainStorage.save(blockchain, savedChain);
            System.out.println("Saved chain to " + savedChain);

            Blockchain reloaded = BlockchainStorage.load(savedChain);
            System.out.println("Reloaded chain valid? " + reloaded.isChainValid());
            System.out.println("Reloaded Alice balance: " + alice.getBalance(reloaded));
        } finally {
            Files.deleteIfExists(savedChain);
        }
    }
}
