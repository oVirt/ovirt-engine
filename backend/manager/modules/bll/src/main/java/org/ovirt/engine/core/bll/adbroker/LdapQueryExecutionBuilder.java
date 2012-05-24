package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.ldap.LdapProviderType;

public interface LdapQueryExecutionBuilder {

    public LdapQueryExecution build(LdapProviderType providerType, LdapQueryData queryData);

    public LdapQueryExecution build(LdapQueryMetadata metaData);
}
