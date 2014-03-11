package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class MultipleLdapQueryExecutionFormatter extends LdapQueryExecutionFormatterBase {

    private String prefix;
    private String suffix;

    MultipleLdapQueryExecutionFormatter(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    protected String getDisplayFilter(LdapQueryMetadata queryMetadata) {
        // The display filter uses the regular parameters, because the encoded ones may not be readable
        return getFilter(queryMetadata, queryMetadata.getQueryData().getFilterParameters());
    }

    @Override
    public LdapQueryExecution format(LdapQueryMetadata queryMetadata) {

        String displayFilter = getDisplayFilter(queryMetadata);

        Object[] encodedFilterParameters =
                getEncodedParameters(queryMetadata.getQueryData().getFilterParameters(),
                        queryMetadata.getLdapIdEncoder());

        String filter = getFilter(queryMetadata, encodedFilterParameters);

        String baseDN =
                String.format(queryMetadata.getBaseDN(),
                        getEncodedParameters(queryMetadata.getQueryData().getBaseDNParameters(),
                                queryMetadata.getLdapIdEncoder()));

        return new LdapQueryExecution(filter,
                displayFilter,
                baseDN,
                queryMetadata.getContextMapper(),
                queryMetadata.getSearchScope(),
                queryMetadata.getReturningAttributes(),
                queryMetadata.getQueryData().getDomain());
    }

    protected String getFilter(LdapQueryMetadata queryMetadata, Object[] filterParameters) {
        StringBuilder filter = new StringBuilder(prefix);

        for (Object currObject : filterParameters) {
            filter.append(String.format(queryMetadata.getFilter(), currObject));
        }

        filter.append(suffix);

        return filter.toString();
    }
}
