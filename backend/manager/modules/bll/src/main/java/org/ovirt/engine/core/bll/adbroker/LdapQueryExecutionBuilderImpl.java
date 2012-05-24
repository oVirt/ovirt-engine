package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.ldap.LdapProviderType;


public class LdapQueryExecutionBuilderImpl implements LdapQueryExecutionBuilder {

    private static LdapQueryExecutionBuilder instance;

    public static LdapQueryExecutionBuilder getInstance() {
        return instance;
    }

    @Override
    public LdapQueryExecution build(LdapProviderType providerType, LdapQueryData queryData) {
        LdapQueryMetadata queryMetadata =
                LdapQueryMetadataFactoryImpl.getInstance().getLdapQueryMetadata(providerType,
                        queryData);
        return queryMetadata.getFormatter().format(queryMetadata);
    }

    static {
        instance = new LdapQueryExecutionBuilderImpl();
    }

    @Override
    public LdapQueryExecution build(LdapQueryMetadata metaData) {
        return metaData.getFormatter().format(metaData);
    }
}
