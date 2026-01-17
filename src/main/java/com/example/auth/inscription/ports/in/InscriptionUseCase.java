package com.example.auth.inscription.ports.in;

import com.example.auth.inscription.entity.Users;
import java.util.List;

public interface InscriptionUseCase 
{
    Users saveUser(Users user);
}
