package com.edu.servicesImpl;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import com.edu.entity.Document;
import com.edu.repositories.DocumentRepository;
import com.edu.services.BlockchainService;

@Service
public class BlockchainServiceImpl implements BlockchainService {

    private final Web3j web3j;
    private final Credentials credentials;
    
    @Autowired
    DocumentRepository documentRepository;

    @Value("${blockchain.contract-address}")
    private String contractAddress;

    // Sepolia chainId (MANDATORY)
    private static final long CHAIN_ID = 11155111L;

    // Safe defaults for Sepolia
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(300_000);
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L); // 20 gwei

    public BlockchainServiceImpl(
            Web3j web3j,
            Credentials credentials
    ) {
        this.web3j = web3j;
        this.credentials = credentials;
    }

    @Override
    public void anchor(Document document) {

        try {
            /* ================================
               1️⃣ Prepare document hash
               ================================ */
            byte[] hashBytes =
                Numeric.hexStringToByteArray(document.getSha256Hash());

            if (hashBytes.length != 32) {
                throw new IllegalArgumentException("SHA-256 hash must be 32 bytes");
            }

            /* ================================
               2️⃣ Build Solidity function call
               function register(bytes32)
               ================================ */
            Function function = new Function(
                "register",
                List.of(new Bytes32(hashBytes)),
                List.of()
            );

            String encodedFunction =
                FunctionEncoder.encode(function);

            /* ================================
               3️⃣ Get nonce
               ================================ */
            BigInteger nonce =
                web3j.ethGetTransactionCount(
                    credentials.getAddress(),
                    DefaultBlockParameterName.PENDING
                ).send().getTransactionCount();

            /* ================================
               4️⃣ Create RAW transaction
               ================================ */
            RawTransaction rawTransaction =
                RawTransaction.createTransaction(
                    nonce,
                    GAS_PRICE,
                    GAS_LIMIT,
                    contractAddress,
                    encodedFunction
                );

            /* ================================
               5️⃣ Sign transaction locally
               ================================ */
            byte[] signedMessage =
                TransactionEncoder.signMessage(
                    rawTransaction,
                    CHAIN_ID,
                    credentials
                );

            String hexValue =
                Numeric.toHexString(signedMessage);

            /* ================================
               6️⃣ Send RAW transaction
               (THIS FIXES YOUR ERROR)
               ================================ */
            EthSendTransaction response =
                web3j.ethSendRawTransaction(hexValue).send();

            if (response.hasError()) {
                throw new RuntimeException(
                    "Blockchain error: " + response.getError().getMessage()
                );
            }

            /* ================================
               7️⃣ Persist tx hash
               ================================ */
            document.setBlockchainTx(response.getTransactionHash());
            document.setStatus(Document.Status.BLOCKCHAIN_ANCHORED);
            documentRepository.save(document);
        } catch (Exception e) {
            throw new RuntimeException("Blockchain anchoring failed", e);
        }
    }
}


