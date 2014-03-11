package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import javax.naming.directory.Attributes;

class GeneralRootDSE implements RootDSE {
    private final Attributes rootDseRecords;

    public GeneralRootDSE(Attributes rootDseRecords) {
        this.rootDseRecords = rootDseRecords;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setDefaultNamingContext(String defaultNamingContext) {
    }

    @Override
    public String getDefaultNamingContext() {
        return null;
    }

    public Attributes getRootDseRecords() {
        return rootDseRecords;
    }
}
