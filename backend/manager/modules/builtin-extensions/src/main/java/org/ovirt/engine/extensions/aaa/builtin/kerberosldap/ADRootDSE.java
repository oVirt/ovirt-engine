package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * Holds information queried from a root DSE
 */
public class ADRootDSE implements RootDSE {

    private String defaultNamingContext;

    public ADRootDSE(Attributes rootDseRecords) throws NamingException {
        String namingContext = rootDseRecords.get(ADRootDSEAttributes.defaultNamingContext.name()).get().toString();
        this.defaultNamingContext = namingContext;
    }

    @Override
    public void setDefaultNamingContext(String defaultNamingContext) {
        this.defaultNamingContext = defaultNamingContext;
    }

    @Override
    public String getDefaultNamingContext() {
        return defaultNamingContext;
    }
}
