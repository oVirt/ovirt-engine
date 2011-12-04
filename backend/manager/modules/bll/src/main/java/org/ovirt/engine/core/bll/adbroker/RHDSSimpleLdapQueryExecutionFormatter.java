package org.ovirt.engine.core.bll.adbroker;

public class RHDSSimpleLdapQueryExecutionFormatter extends SimpleLdapQueryExecutionFormatter {
    // In DS the guid is a readable String as well, but it is different then our guid, so we need to show the encoded
    // parameters
    @Override
    protected String getDisplayFilter(LdapQueryMetadata queryMetadata) {
        return String.format(queryMetadata.getFilter(),
                getEncodedParameters(queryMetadata.getQueryData().getFilterParameters(),
                        queryMetadata.getLdapGuidEncoder()));
    }

}
