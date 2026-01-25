package com.example.auth.inscription.ports.out;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;

public interface SpringDataUsersRepository extends JpaRepository<UsersJpaEntity, Integer> 
{
    boolean existsByMail(String mail);
    java.util.Optional<UsersJpaEntity> findByMail(String mail);
}
