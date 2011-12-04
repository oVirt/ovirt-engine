package org.ovirt.engine.core.bll.adbroker;

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
                                queryMetadata.getLdapGuidEncoder()));

        String baseDN =
                String.format(queryMetadata.getBaseDN(),
                        getEncodedParameters(queryMetadata.getQueryData().getBaseDNParameters(),
                                queryMetadata.getLdapGuidEncoder()));

        return new LdapQueryExecution(filter,
                getDisplayFilter(queryMetadata),
                baseDN,
                queryMetadata.getContextMapper(),
                queryMetadata.getSearchScope(),
                queryMetadata.getReturningAttributes(),
                queryMetadata.getQueryData().getDomain());
    }
}
