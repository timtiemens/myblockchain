# myblockchain

A small, educational blockchain implemented in Java. It's a single-process,
in-memory simulation of the core mechanics behind blockchains and
cryptocurrencies — wallets, signed transactions, proof-of-work mining, and
chain validation — with no networking, persistence, or consensus protocol.
It's meant for learning and reference, not for running a real network.

## Features

- **Wallets** (`Wallet`) — each wallet generates its own ECDSA (`secp256r1`)
  keypair. A wallet's address *is* its Base64-encoded public key, so no
  separate address derivation step is needed.
- **Transactions** (`Transaction`) — immutable sender/recipient/amount
  records, signed with the sender's private key and verified against the
  sender address. A transaction with a `null` sender is treated as a
  coinbase (mining reward) transaction and skips signature verification.
- **Merkle trees** (`MerkleTree`) — each block's transactions are combined
  into a Merkle root (duplicating the last hash on odd levels) rather than
  hashed as a flat concatenation.
- **Blocks** (`Block`) — hold a list of transactions and the previous
  block's hash, and support proof-of-work mining (`mineBlock`), which
  increments a nonce until the block's hash has the required number of
  leading hex zeros.
- **Blockchain** (`Blockchain`) — owns the chain and a pool of pending
  transactions. `minePendingTransactions` mines those transactions into a
  new block along with a coinbase reward for the miner. `getBalance`
  recomputes a wallet's balance by replaying every transaction in every
  block (no incremental balance tracking). `isChainValid` re-derives every
  block's hash, checks hash linkage, re-verifies all transaction
  signatures, and re-checks proof-of-work.
- **Crypto utilities** (`CryptoUtil`) — SHA-256 hashing and ECDSA keypair
  generation/sign/verify, with keys exchanged as Base64-encoded
  X.509/PKCS8 strings.

## Requirements

- Java 21 (the build uses a Gradle toolchain, so a matching JDK is
  downloaded/selected automatically if one isn't already available)
- No local Gradle install needed — the project uses the Gradle wrapper
  (Gradle 9.7.1)

## Building and running

```bash
# Build and run all tests
./gradlew build

# Run tests only
./gradlew test

# Run a single test class
./gradlew test --tests "com.tiemens.myblockchain.core.BlockchainTest"

# Run the demo app
./gradlew run
```

On Windows, use `gradlew.bat` in place of `./gradlew`.

The demo app (`Main`) creates a blockchain with difficulty 4 and a mining
reward of 100, creates three wallets (Alice, Bob, and a miner), mines an
initial block to fund Alice, has Alice send 25 to Bob, mines that
transaction, and prints each wallet's balance along with whether the chain
is valid.

## Project layout

```
src/main/java/com/tiemens/myblockchain/
├── Main.java                  # demo entry point
├── core/
│   ├── Block.java              # transactions + previousHash, proof-of-work mining
│   ├── Blockchain.java         # chain + pending pool, mining, balances, validation
│   ├── MerkleTree.java         # Merkle root over a block's transaction hashes
│   ├── Transaction.java        # signed sender/recipient/amount record
│   └── Wallet.java              # ECDSA keypair + address + balance lookup
└── util/
    └── CryptoUtil.java         # SHA-256 hashing, ECDSA keygen/sign/verify

src/test/java/com/tiemens/myblockchain/core/
├── BlockchainTest.java
└── MerkleTreeTest.java
```

## Known limitations

This is a teaching/reference project, not production code:

- Everything lives in memory for the lifetime of the JVM process — there's
  no persistence, no P2P networking, and no RPC/API layer.
- `getBalance` is O(chain size) on every call rather than tracked
  incrementally.
- The `gson` dependency declared in `build.gradle` is currently unused —
  there is no serialization/persistence code yet.
- There's a single miner/validator process, so there's no real consensus
  mechanism (no competing nodes, no fork resolution).

## License

No license file is currently included in this repository.
