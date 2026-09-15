package com.tiemens.myblockchain.core;

import com.tiemens.myblockchain.util.CryptoUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public Wallet() {
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    public String getAddress() {
        return CryptoUtil.encodePublicKey(publicKey);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public double getBalance(Blockchain blockchain) {
        return blockchain.getBalance(getAddress());
    }

    public Transaction createTransaction(String recipientAddress, double amount) {
        Transaction transaction = new Transaction(getAddress(), recipientAddress, amount);
        transaction.signTransaction(privateKey);
        return transaction;
    }
}
