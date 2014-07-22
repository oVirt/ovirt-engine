package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public interface LdapQueryExecutionBuilder {

    public LdapQueryExecution build(LdapProviderType providerType, LdapQueryData queryData);
}
