package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public class LdapQueryExecutionBuilderImpl implements LdapQueryExecutionBuilder {

    private static volatile LdapQueryExecutionBuilder instance;

    public static LdapQueryExecutionBuilder getInstance() {
        if (instance == null) {
            synchronized (LdapQueryExecutionBuilderImpl.class) {
                if (instance == null) {
                    instance = new LdapQueryExecutionBuilderImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public LdapQueryExecution build(LdapProviderType providerType, LdapQueryData queryData) {
        LdapQueryMetadata queryMetadata =
                LdapQueryMetadataFactoryImpl.getInstance().getLdapQueryMetadata(providerType,
                        queryData);
        return queryMetadata.getFormatter().format(queryMetadata);
    }
}
