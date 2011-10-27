package org.ovirt.engine.core.bll.adbroker;

public interface LdapQueryExecutionBuilder {

    public LdapQueryExecution build(LdapProviderType providerType, LdapQueryData queryData);

    public LdapQueryExecution build(LdapQueryMetadata metaData);
}
