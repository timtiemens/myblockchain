package com.tiemens.myblockchain.core;

import com.tiemens.myblockchain.util.CryptoUtil;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MerkleTreeTest {

    @Test
    void emptyTransactionListHashesEmptyString() {
        assertEquals(CryptoUtil.sha256(""), MerkleTree.computeRoot(Collections.emptyList()));
    }

    @Test
    void singleTransactionRootIsItsHash() {
        Transaction tx = new Transaction(null, "recipient", 10);
        assertEquals(tx.calculateHash(), MerkleTree.computeRoot(List.of(tx)));
    }

    @Test
    void oddNumberOfTransactionsDuplicatesLastHash() {
        Transaction a = new Transaction(null, "a", 1);
        Transaction b = new Transaction(null, "b", 2);
        Transaction c = new Transaction(null, "c", 3);

        String expectedLevel1Left = CryptoUtil.sha256(a.calculateHash() + b.calculateHash());
        String expectedLevel1Right = CryptoUtil.sha256(c.calculateHash() + c.calculateHash());
        String expectedRoot = CryptoUtil.sha256(expectedLevel1Left + expectedLevel1Right);

        assertEquals(expectedRoot, MerkleTree.computeRoot(List.of(a, b, c)));
    }

    @Test
    void differentTransactionOrderProducesDifferentRoot() {
        Transaction a = new Transaction(null, "a", 1);
        Transaction b = new Transaction(null, "b", 2);

        assertNotEquals(MerkleTree.computeRoot(List.of(a, b)), MerkleTree.computeRoot(List.of(b, a)));
    }
}
