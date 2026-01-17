package com.example.auth.inscription.adapters.out;

import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.out.InscriptionRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaInscriptionRepositoryAdapter implements InscriptionRepository 
{

    private final SpringDataUsersRepository springRepo;

    public JpaInscriptionRepositoryAdapter(SpringDataUsersRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public Users save(Users user) {
        UsersJpaEntity entity = toJpa(user);
        UsersJpaEntity saved = springRepo.save(entity);
        return toDomain(saved);
    }

    private UsersJpaEntity toJpa(Users u) {
        UsersJpaEntity e = new UsersJpaEntity();
        // pas d'id ici, JPA le génère
        e.setName(u.getName());
        e.setMail(u.getMail());
        e.setPswHash(u.getPsw()); // on stockera déjà hashé dans le service
        e.setAdmin(u.getIsAdmin());
        e.setPublicKey(u.getPublicKey());
        e.setVaultKey(u.getVaultKey());
        return e;
    }

    private Users toDomain(UsersJpaEntity e) {
        return new Users(
                e.getId(),
                e.getName(),
                e.getMail(),
                e.getPswHash(), // hash en domaine pour ce flow (ok en test)
                e.isAdmin(),
                e.getPublicKey(),
                e.getVaultKey()
        );
    }
}
