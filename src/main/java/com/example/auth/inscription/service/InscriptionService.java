package com.example.auth.inscription.service;

import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.in.InscriptionUseCase;
import com.example.auth.inscription.ports.out.InscriptionRepository;
import com.example.auth.inscription.ports.out.UserKeyVaultPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InscriptionService implements InscriptionUseCase 
{

    private final InscriptionRepository inscriptionRepository;
    private final UserKeyVaultPort userKeyVaultPort;
    private final BCryptPasswordEncoder encoder;

    public InscriptionService(
            InscriptionRepository inscriptionRepository,
            UserKeyVaultPort userKeyVaultPort,
            BCryptPasswordEncoder encoder
    ) {
        this.inscriptionRepository = inscriptionRepository;
        this.userKeyVaultPort = userKeyVaultPort;
        this.encoder = encoder;
    }

    @Override
    public Users saveUser(Users user) {
        // 1) hash password
        String hash = encoder.encode(user.getPsw());
        user.setPsw(hash);

        // 2) générer un nom de clé Vault (à stocker dans vault_key en DB)
        String vaultKeyName = "user-signing-" + UUID.randomUUID();
        user.setVaultKey(vaultKeyName);

        // 3) créer la clé dans Vault (privée reste dans Vault)
        userKeyVaultPort.createSigningKey(vaultKeyName);

        // 4) exporter la clé publique -> stocker en DB
        String publicKey = userKeyVaultPort.exportPublicKey(vaultKeyName);
        user.setPublicKey(publicKey);

        // 5) save DB
        return inscriptionRepository.save(user);
    }

    @Override
    public List<Users> getAllUsers() {
        return inscriptionRepository.findAll();
    }

    @Override
    public void deleteUser(Integer userId) {
        inscriptionRepository.deleteById(userId);
    }
}
