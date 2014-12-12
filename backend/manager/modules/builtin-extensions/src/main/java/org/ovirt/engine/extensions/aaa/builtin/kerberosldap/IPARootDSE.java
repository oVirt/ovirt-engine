package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;


public class IPARootDSE implements RootDSE {

    private String defaultNamingContext;

    public IPARootDSE(Attributes rootDseRecords) throws NamingException {
        Attribute namingContexts = rootDseRecords.get(IPARootDSEAttributes.defaultnamingcontext.name());
        if ( namingContexts != null ) {
            this.defaultNamingContext = namingContexts.get(0).toString();
        }
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

