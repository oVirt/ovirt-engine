package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.ldap.LdapProviderType;

public interface LdapQueryMetadataFactory {

    public LdapQueryMetadata getLdapQueryMetadata(LdapProviderType providerType, LdapQueryData queryData);

}
