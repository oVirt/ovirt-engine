package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class RHDSRootDSE implements RootDSE {

    private String defaultNamingContext;

    public RHDSRootDSE(Attributes rootDseRecords) throws NamingException {
        Attribute namingContexts = rootDseRecords.get(RHDSRootDSEAttributes.namingContexts.name());
        if (namingContexts != null) {
            this.defaultNamingContext =
                    RHDSRootDSEContextMapper.getDefaultNamingContextFromNameingContexts(namingContexts);
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

