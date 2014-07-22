package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public interface LdapQueryMetadataFactory {

    public LdapQueryMetadata getLdapQueryMetadata(LdapProviderType providerType, LdapQueryData queryData);

}
