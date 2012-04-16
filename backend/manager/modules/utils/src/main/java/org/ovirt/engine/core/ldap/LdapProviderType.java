package org.ovirt.engine.core.ldap;

public enum LdapProviderType {
    activeDirectory,
    ipa,
    rhds;

    public static LdapProviderType valueOfIgnoreCase(String name) {
        if (name == null) {
            throw new NullPointerException("Name is null");
        }
        for (LdapProviderType type : values()) {
            if (name.equalsIgnoreCase(type.name())) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum const for name " + name);
    }

}
