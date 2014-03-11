package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public interface RootDSE {
    public void setDefaultNamingContext(String defaultNamingContext);

    public String getDefaultNamingContext();
}
