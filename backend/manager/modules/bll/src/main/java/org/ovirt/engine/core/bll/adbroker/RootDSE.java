package org.ovirt.engine.core.bll.adbroker;

public interface RootDSE {
    public void setDefaultNamingContext(String defaultNamingContext);

    public String getDefaultNamingContext();
}
