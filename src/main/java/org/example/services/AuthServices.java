package org.example.services;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class AuthServices {

    private static AuthServices instance;

    public static AuthServices getInstance(){
        if(instance==null){
            instance = new AuthServices();
        }
        return instance;
    }

    public String generateHash(String data){
        String hashedPassword = BCrypt.hashpw(data, BCrypt.gensalt());
        return hashedPassword;
    }

    public boolean verifyHash(String data,String originalPasswordHash){
        return BCrypt.checkpw(data, originalPasswordHash);
    }
}
