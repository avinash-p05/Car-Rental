package org.example.services;

public class AuthServices {

    private static AuthServices instance;

    public static AuthServices getInstance(){
        if(instance==null){
            instance = new AuthServices();
        }
        return instance;
    }

    public String generateHash(String data){
        return Integer.toString(data.hashCode());
    }
    public boolean verifyHash(String data,int originalPasswordHash){
        return data.hashCode()==originalPasswordHash;
    }
}
