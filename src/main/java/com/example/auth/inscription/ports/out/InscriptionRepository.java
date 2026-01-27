package com.example.auth.inscription.ports.out;
import java.util.List;
import java.util.Optional;

import com.example.auth.inscription.entity.Users;

public interface InscriptionRepository 
{
    Users save(Users user);
    
    List<Users> findAll();
    
    void deleteById(Integer userId);
}
