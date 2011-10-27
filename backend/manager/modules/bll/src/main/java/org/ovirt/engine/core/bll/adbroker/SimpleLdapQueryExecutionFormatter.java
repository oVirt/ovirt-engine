package org.ovirt.engine.core.bll.adbroker;

public class SimpleLdapQueryExecutionFormatter extends LdapQueryExecutionFormatterBase {

    @Override
    public LdapQueryExecution format(LdapQueryMetadata queryMetadata) {

        String filter =
                String.format(queryMetadata.getFilter(),
                        getEncodedParameters(queryMetadata.getQueryData().getFilterParameters(),
                                queryMetadata.getLdapGuidEncoder()));

        // The display filter uses the regular parameters, because the encoded ones may not be readable
        String displayFilter =
                String.format(queryMetadata.getFilter(), queryMetadata.getQueryData().getFilterParameters());

        String baseDN =
                String.format(queryMetadata.getBaseDN(),
                        getEncodedParameters(queryMetadata.getQueryData().getBaseDNParameters(),
                                queryMetadata.getLdapGuidEncoder()));
        return new LdapQueryExecution(filter,
                displayFilter,
                baseDN,
                queryMetadata.getContextMapper(),
                queryMetadata.getSearchScope(),
                queryMetadata.getReturningAttributes(),
                queryMetadata.getQueryData().getDomain());
    }
}
