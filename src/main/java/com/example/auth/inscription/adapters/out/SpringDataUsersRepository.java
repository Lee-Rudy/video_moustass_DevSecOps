package com.example.auth.inscription.adapters.out;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUsersRepository extends JpaRepository<UsersJpaEntity, Integer> 
{
    boolean existsByMail(String mail);
}
