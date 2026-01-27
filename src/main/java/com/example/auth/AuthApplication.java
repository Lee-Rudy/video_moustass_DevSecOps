package com.example.auth;

import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.in.InscriptionUseCase;
import com.example.auth.login.models.LoginService;
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

    @Bean
    CommandLineRunner demoLogin(LoginService loginService) {
        return args -> {
            //login user
            String mail = "alice@gmail.com";
            String password = "Alice123456789";

            // token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcklkIjoxfQ.hGlTvJpW7Y-GHTvunyfmcXIrvQOkhfgzDeus4D1PiAU
            // token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcklkIjoyfQ.fY1ZyJct0UTas5BTBg3SYzeZttGek8_VsSoaOkoYpjU

            //login admin
            // String mail = "brunerleerudy@gmail.com";
            // String password = "Admin123456789";
            var opt = loginService.authenticate(mail, password);
            if (opt.isPresent()) {
                var res = opt.get();
                System.out.println("LOGIN ok => mail=" + mail + " userId=" + res.userId() + " token=" + res.token());
            } else {
                System.out.println("LOGIN échec => mail=" + mail + " (utilisateur inexistant ou mot de passe incorrect)");
            }
        };
    }

    // @Bean
    // CommandLineRunner demo(InscriptionUseCase useCase) 
	// {
    //     return args -> {
    //         // Users admin = new Users(0, "Admin", "brunerleerudy@gmail.com", "Admin123456789", true, null, null);
            //  Users user  = new Users(1, "Alice", "alice@gmail.com", "Alice123456789", false, null, null);
    //         Users user  = new Users(3, "Rudy", "brunerlee@gmail.com", "Rudy123456789", false, null, null);
    //         //Users user  = new Users(5, "Lee", "brunerleerudy@gmail.com", "Lee123456789", false, null, null);


    //         // Users savedAdmin = useCase.saveUser(admin);
    //         Users savedUser  = useCase.saveUser(user);

    //         // System.out.println("ADMIN saved => id=" + savedAdmin.getIdUsers()
    //         //         + " mail=" + savedAdmin.getMail()
    //         //         + " isAdmin=" + savedAdmin.getIsAdmin()
    //         //         + " vaultKey=" + savedAdmin.getVaultKey());

    //         System.out.println("USER saved  => id=" + savedUser.getIdUsers()
    //                 + " mail=" + savedUser.getMail()
    //                 + " isAdmin=" + savedUser.getIsAdmin()
    //                 + " vaultKey=" + savedUser.getVaultKey());
    //     };
    // }
}
