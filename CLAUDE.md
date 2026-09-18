# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- Build + test: `./gradlew build`
- Run tests only: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.tiemens.myblockchain.core.BlockchainTest"`
- Run a single test method: `./gradlew test --tests "com.tiemens.myblockchain.core.BlockchainTest.transactionMovesFundsBetweenWallets"`
- Run the demo app: `./gradlew run`

Uses the Gradle wrapper (Gradle 9.7.1, Java 21 toolchain) — no local Gradle install needed. JUnit 5 via the platform launcher (`testRuntimeOnly`), required explicitly under Gradle 9.

## Architecture

Single-module educational blockchain: in-memory, single-process, no networking/persistence layer. All state lives in a `Blockchain` instance for the lifetime of the JVM process — there is no P2P, storage, or RPC layer to reason about.

- `com.tiemens.myblockchain.util.CryptoUtil` — SHA-256 hashing and ECDSA (`secp256r1`) keypair generation/sign/verify. All key material is exchanged as Base64-encoded X.509/PKCS8 strings elsewhere in the codebase.
- `com.tiemens.myblockchain.core.Wallet` — wraps a generated EC keypair. **The wallet address is the Base64-encoded public key itself**, not a hash or derived identifier.
- `com.tiemens.myblockchain.core.Transaction` — immutable sender/recipient/amount record. `isValid()` treats a `null` sender as a coinbase/reward transaction and skips signature verification; any other transaction must carry an ECDSA signature over `calculateHash()` verifiable against the sender address (which doubles as the public key).
- `com.tiemens.myblockchain.core.Block` — holds a list of transactions and `previousHash`; `mineBlock(difficulty)` is proof-of-work, incrementing `nonce` until `hash` has `difficulty` leading hex zero digits.
- `com.tiemens.myblockchain.core.Blockchain` — owns the chain (`List<Block>`) and a pending-transaction pool.
  - `addTransaction` validates the signature before queuing.
  - `minePendingTransactions(minerAddress)` appends a coinbase reward transaction (sender `null`) to the pending set, mines a new block on top of `getLatestBlock().getHash()`, and clears the pool.
  - `getBalance(address)` is **not** tracked incrementally — it recomputes by replaying every transaction in every block on each call.
  - `isChainValid()` re-derives each block's hash, checks the `previousHash` linkage, re-validates all transaction signatures, and re-checks the proof-of-work target.

`com.tiemens.myblockchain.persistence.BlockchainStorage` saves a `Blockchain` to a JSON file and loads it back, using the `gson` dependency declared in `build.gradle` (previously unused). Loaded blocks/transactions preserve their original timestamp, nonce, hash and signature rather than being re-mined or re-signed; call `isChainValid()` after loading to confirm the file wasn't corrupted. The `*Data` classes in that package are plain mutable DTOs used only for (de)serialization, kept separate from the immutable domain classes.
