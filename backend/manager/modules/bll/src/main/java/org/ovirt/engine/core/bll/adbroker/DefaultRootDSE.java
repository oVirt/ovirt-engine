package org.ovirt.engine.core.bll.adbroker;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;


public class DefaultRootDSE implements RootDSE {

    private String defaultNamingContext;

    public DefaultRootDSE() {
    }

    public DefaultRootDSE(String defaultNamingContext) {
        this.defaultNamingContext = defaultNamingContext;
    }

    public DefaultRootDSE(Attributes rootDseRecords) throws NamingException {
        Attribute namingContexts = rootDseRecords.get(DefaultRootDSEAttributes.namingContexts.name());
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
