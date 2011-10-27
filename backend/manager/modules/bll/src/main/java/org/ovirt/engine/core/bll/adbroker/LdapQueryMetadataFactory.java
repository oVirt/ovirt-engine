package org.ovirt.engine.core.bll.adbroker;

public interface LdapQueryMetadataFactory {

    public LdapQueryMetadata getLdapQueryMetadata(LdapProviderType providerType, LdapQueryData queryData);

}
