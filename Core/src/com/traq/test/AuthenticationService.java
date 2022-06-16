package com.traq.test;

/**
 * Created by Amit Kamboj on 2/8/16.
 */
import java.io.IOException;
import java.util.Base64;
import java.util.StringTokenizer;

public class AuthenticationService {
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean authenticate(String authCredentials) {

        if (null == authCredentials)
            return false;
        // header value format will be "Basic encodedstring" for Basic
        // authentication. Example "Basic YWRtaW46YWRtaW4="

        final String encodedUserPassword = authCredentials.replaceFirst("Basic"
                + " ", "");
        String usernameAndPassword = null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(
                    encodedUserPassword);
            usernameAndPassword = new String(decodedBytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final StringTokenizer tokenizer = new StringTokenizer(
                usernameAndPassword, "|");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();

        // we have fixed the userid and password as admin
        // call some UserService/LDAP here

        boolean authenticationStatus = getUserName().equals(username)
                && getPassword().equals(password);
        return authenticationStatus;
    }

    public static String encode(String credentials){
        String base64encodedString = null;
        try{
            credentials = credentials.replaceAll("authetication","");
            credentials = credentials.replaceAll("\"","");
            credentials = credentials.replaceAll(":","");
            credentials = credentials.replaceAll(",","");

            base64encodedString = Base64.getEncoder().encodeToString(credentials.getBytes("utf-8"));

        }catch(Exception ex){

        }
        return base64encodedString;
    }

    public static String decode(String authCredentials) {

        if (null == authCredentials)
            return null;
        // header value format will be "Basic encodedstring" for Basic
        // authentication. Example "Basic YWRtaW46YWRtaW4="

        final String encodedUserPassword = authCredentials.replaceFirst("Basic"
                + " ", "");
        String usernameAndPassword = null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(
                    encodedUserPassword);
            usernameAndPassword = new String(decodedBytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usernameAndPassword;
    }

    public static void main(String [] args){
        AuthenticationService as = new AuthenticationService();
        as.setUserName("amit");
        as.setPassword("amit123");
        boolean isAuthenticate = as.authenticate("YW1pdHxhbWl0MTIz");
        System.out.println("isAuthenticate "+isAuthenticate);
    }
}
