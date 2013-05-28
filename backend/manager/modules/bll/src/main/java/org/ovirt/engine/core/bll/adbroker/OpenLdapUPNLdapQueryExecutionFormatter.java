package org.ovirt.engine.core.bll.adbroker;

/**
 * OpenLdap doesn't have a UPN, so this formatter practically adjusts the query to search by user name instead of UPN
 */
public class OpenLdapUPNLdapQueryExecutionFormatter extends SimpleLdapQueryExecutionFormatter {

    /**
     * Put the user name instead of the UPN in the filter
     */
    protected String getFilter(LdapQueryMetadata queryMetadata) {
        String userPrincipalName = (String)queryMetadata.getQueryData().getFilterParameters()[0];
        String userName = userPrincipalName.split("@")[0];
        return String.format(queryMetadata.getFilter(), userName);
    }

    @Override
    protected String getDisplayFilter(LdapQueryMetadata queryMetadata) {
        return getFilter(queryMetadata);
    }

    @Override
    public LdapQueryExecution format(LdapQueryMetadata queryMetadata) {

        String filter = getFilter(queryMetadata);

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
