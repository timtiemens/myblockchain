package com.tiemens.myblockchain.core;

import com.tiemens.myblockchain.util.CryptoUtil;

import java.util.ArrayList;
import java.util.List;

/** Computes a Merkle root over transaction hashes; odd levels duplicate the last hash. */
public final class MerkleTree {

    private MerkleTree() {
    }

    public static String computeRoot(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return CryptoUtil.sha256("");
        }

        List<String> level = new ArrayList<>(transactions.size());
        for (Transaction tx : transactions) {
            level.add(tx.calculateHash());
        }

        while (level.size() > 1) {
            List<String> nextLevel = new ArrayList<>((level.size() + 1) / 2);
            for (int i = 0; i < level.size(); i += 2) {
                String left = level.get(i);
                String right = (i + 1 < level.size()) ? level.get(i + 1) : left;
                nextLevel.add(CryptoUtil.sha256(left + right));
            }
            level = nextLevel;
        }

        return level.get(0);
    }
}
