package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class LdapCredentials {
    private String userName;
    private String password;

    public LdapCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
