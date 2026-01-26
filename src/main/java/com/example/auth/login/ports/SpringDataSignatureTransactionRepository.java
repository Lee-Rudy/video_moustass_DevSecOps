package com.example.auth.login.ports;

import com.example.auth.login.entity.SignatureTransactionJpaEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSignatureTransactionRepository extends JpaRepository<SignatureTransactionJpaEntity, Integer> {

    /** Ordres reçus par l'utilisateur (transaction_send_to = nom du destinataire). */
    List<SignatureTransactionJpaEntity> findByTransactionSendToOrderByCreatedAtDesc(String transactionSendTo);
}
