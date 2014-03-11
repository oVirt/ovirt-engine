package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;


public final class LdapFactory {

    private static LdapBroker ldapInstance;

    static {
        ldapInstance = new LdapBrokerImpl();
    }

    public static LdapBroker getInstance(String domain) {
        return ldapInstance;
    }
}
