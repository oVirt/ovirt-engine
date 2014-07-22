package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public class RootDSEFactory {

    public static RootDSE get(LdapProviderType ldapProviderType, Attributes rootDseRecords) throws NumberFormatException, NamingException {
        switch (ldapProviderType) {

        case activeDirectory:
            return  new ADRootDSE(rootDseRecords);
        case ipa:
            return new IPARootDSE(rootDseRecords);
        case rhds:
            return new RHDSRootDSE(rootDseRecords);
        case itds:
            return new ITDSRootDSE(rootDseRecords);
        case openLdap:
            return new DefaultRootDSE(rootDseRecords);
        default:
            throw new IllegalArgumentException("Invalid LDAP provider type.");
        }

    }
}

