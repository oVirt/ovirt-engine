package org.ovirt.engine.core.bll.adbroker;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

public class RootDSEFactory {

    public static RootDSE get(LdapProviderType ldapProviderType, Attributes rootDseRecords) throws NumberFormatException, NamingException {
        switch (ldapProviderType) {

        case activeDirectory:
            return  new ADRootDSE(rootDseRecords);
        case ipa:
            return new IPARootDSE(rootDseRecords);
        case general:
        default:
            return new GeneralRootDSE(rootDseRecords);
        }

    }
}

