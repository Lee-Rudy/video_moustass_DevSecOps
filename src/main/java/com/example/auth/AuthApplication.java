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

    /**
     * Bean de démonstration pour tester le login au démarrage.
     * REMARQUE: Les credentials sont chargés depuis les variables d'environnement ou application.properties
     * pour éviter les mots de passe hard-codés.
     * 
     * Pour désactiver ce bean en production, définir: demo.login.enabled=false
     */
    @Bean
    CommandLineRunner demoLogin(LoginService loginService, 
                                 @org.springframework.beans.factory.annotation.Value("${demo.login.enabled:false}") boolean enabled,
                                 @org.springframework.beans.factory.annotation.Value("${demo.login.mail:#{null}}") String mail,
                                 @org.springframework.beans.factory.annotation.Value("${demo.login.password:#{null}}") String password) {
        return args -> {
            // Bean désactivé par défaut en production pour des raisons de sécurité
            if (!enabled) {
                System.out.println("Demo login désactivé (demo.login.enabled=false)");
                return;
            }
            
            // Vérification que les credentials sont fournis via configuration
            if (mail == null || password == null) {
                System.out.println("AVERTISSEMENT: Credentials de démonstration non configurés dans application.properties");
                return;
            }

            // Test de login avec les credentials configurés
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
