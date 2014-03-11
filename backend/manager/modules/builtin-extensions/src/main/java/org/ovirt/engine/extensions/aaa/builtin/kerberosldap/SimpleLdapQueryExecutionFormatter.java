package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class SimpleLdapQueryExecutionFormatter extends LdapQueryExecutionFormatterBase {

    @Override
    protected String getDisplayFilter(LdapQueryMetadata queryMetadata) {
        return String.format(queryMetadata.getFilter(), queryMetadata.getQueryData().getFilterParameters());
    }

    @Override
    public LdapQueryExecution format(LdapQueryMetadata queryMetadata) {

        String filter =
                String.format(queryMetadata.getFilter(),
                        getEncodedParameters(queryMetadata.getQueryData().getFilterParameters(),
                                queryMetadata.getLdapIdEncoder()));

        String baseDN =
                String.format(queryMetadata.getBaseDN(),
                        getEncodedParameters(queryMetadata.getQueryData().getBaseDNParameters(),
                                queryMetadata.getLdapIdEncoder()));

        return new LdapQueryExecution(filter,
                getDisplayFilter(queryMetadata),
                baseDN,
                queryMetadata.getContextMapper(),
                queryMetadata.getSearchScope(),
                queryMetadata.getReturningAttributes(),
                queryMetadata.getQueryData().getDomain());
    }
}
