package com.edu.servicesImpl;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.TypeReference;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;


import com.edu.entity.Document;
import com.edu.repositories.DocumentRepository;
import com.edu.services.BlockchainService;

@Service
public class BlockchainServiceImpl implements BlockchainService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private Credentials credentials;

    @Value("${blockchain.contract-address}")
    private String contractAddress;
    
    @Autowired
    DocumentRepository  documentRepository;

    @Override
    public void anchor(Document document) {

        try {
            byte[] hashBytes =
                Numeric.hexStringToByteArray(document.getSha256Hash());

            Function function = new Function(
                "register",
                List.of(new Bytes32(hashBytes)),
                List.of()
            );

            String encodedFunction =
                FunctionEncoder.encode(function);

            EthSendTransaction tx =
                web3j.ethSendTransaction(
                    Transaction.createFunctionCallTransaction(
                        credentials.getAddress(),
                        null,
                        DefaultGasProvider.GAS_PRICE,
                        DefaultGasProvider.GAS_LIMIT,
                        contractAddress,
                        encodedFunction
                    )
                ).send();

            if (tx.hasError()) {
                throw new RuntimeException(tx.getError().getMessage());
            }

            document.setBlockchainTx(tx.getTransactionHash());
            document.setStatus(Document.Status.BLOCKCHAIN_ANCHORED);
            documentRepository.save(document);
        } catch (Exception e) {
            throw new RuntimeException("Blockchain anchoring failed", e);
        }
    }

}
