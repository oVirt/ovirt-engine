package org.ovirt.engine.ui.webadmin.auth;

/**
 * Holds data relevant for silent login functionality (development environment only).
 */
public class SilentLoginData {

    private final String userName;
    private String password;
    private final String domain;

    public SilentLoginData(String userName, String password, String domain) {
        this.userName = userName;
        this.password = password;
        this.domain = domain;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }
}
