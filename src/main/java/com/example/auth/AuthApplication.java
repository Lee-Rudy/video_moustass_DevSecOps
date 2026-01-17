package com.example.auth;

import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.in.InscriptionUseCase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuthApplication 
{

    public static void main(String[] args) 
	{
        SpringApplication.run(AuthApplication.class, args);
    }

    // @Bean
    // CommandLineRunner demo(InscriptionUseCase useCase) 
	// {
    //     return args -> {
    //         Users admin = new Users(0, "Admin", "brunerleerudy@gmail.com", "Admin123456789", true, null, null);
    //         Users user  = new Users(1, "Alice", "alice@gmail.com", "Alice123456789", false, null, null);

    //         Users savedAdmin = useCase.saveUser(admin);
    //         Users savedUser  = useCase.saveUser(user);

    //         System.out.println("ADMIN saved => id=" + savedAdmin.getIdUsers()
    //                 + " mail=" + savedAdmin.getMail()
    //                 + " isAdmin=" + savedAdmin.getIsAdmin()
    //                 + " vaultKey=" + savedAdmin.getVaultKey());

    //         System.out.println("USER saved  => id=" + savedUser.getIdUsers()
    //                 + " mail=" + savedUser.getMail()
    //                 + " isAdmin=" + savedUser.getIsAdmin()
    //                 + " vaultKey=" + savedUser.getVaultKey());
    //     };
    // }
}
