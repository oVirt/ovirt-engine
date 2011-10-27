package org.ovirt.engine.core.bll.adbroker;

import sun.security.krb5.KrbException;

public interface KerberosManagerSericeManagmentMBean {

    @SuppressWarnings("restriction")
    public void refresh() throws KrbException;

}
