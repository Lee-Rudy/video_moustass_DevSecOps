package com.example.auth.inscription.entity;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Users 
{
    private int idUsers;
    private String name;
    private String mail;
    private String psw;
    private boolean isAdmin;
    private String publicKey;
    private String vaultKey;

    //constructor vide
    public Users (){}

    // constructor
    public Users (int idUsers, String name, String mail, String psw, boolean isAdmin, String publicKey, String vaultKey)
    {
        setIdUsers(idUsers);
        setName(name);
        setMail(mail);
        setPsw(psw);
        setIsAdmin(isAdmin);
        setPublicKey(publicKey);
        setVaultKey(vaultKey);
    }

    // setters
    public void setIdUsers(int idUsers)
    {
        this.idUsers = idUsers;
    }

    public void setName(String name)
    {
        if (name == null || name.trim().isEmpty()) 
        {
            throw new IllegalArgumentException("Le champ nom ne doit pas être vide");
        }
        this.name = name.trim();
    }

    public void setMail(String mail)
    {
        if (mail == null || mail.trim().isEmpty())
        {
            throw new IllegalArgumentException("Le champ mail ne doit pas être vide");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pat = Pattern.compile(emailRegex);
        if (!pat.matcher(mail).matches())
        {
            throw new IllegalArgumentException("Le mail n'est pas valide");
        }
         this.mail = mail.toLowerCase().trim();
    }

    public void setPsw(String psw)
    {
        if (psw == null || psw.trim().isEmpty())
        {
            throw new IllegalArgumentException("Le champ mot de passe ne doit pas être vide");
        }
        this.psw = psw.trim();
        if (psw.length() < 8)
        {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }
        if (!psw.matches(".*[A-Z].*"))
        {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une lettre majuscule");
        }
    }

    public void setIsAdmin(boolean isAdmin)
    {
        this.isAdmin = isAdmin;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public void setVaultKey(String vaultKey)
    {
        this.vaultKey = vaultKey;
    }

    // getters
    public int getIdUsers(){return idUsers;}
    public String getName(){return name;}
    public String getMail(){return mail;}
    public String getPsw(){return psw;}
    public boolean getIsAdmin(){return isAdmin;}
    public String getPublicKey(){return publicKey;}
    public String getVaultKey(){return vaultKey;}

}
