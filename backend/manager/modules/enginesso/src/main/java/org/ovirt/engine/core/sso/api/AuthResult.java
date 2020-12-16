package org.ovirt.engine.core.sso.api;

public class AuthResult {
    private int status;
    private String token;
    private Credentials credentials;

    public AuthResult() {
    }

    public AuthResult(int status) {
        setStatus(status);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
