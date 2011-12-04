package org.ovirt.engine.core.bll.adbroker;

public class RHDSMultipleLdapQueryExecutionFormatter extends MultipleLdapQueryExecutionFormatter {

    RHDSMultipleLdapQueryExecutionFormatter(String prefix, String suffix) {
        super(prefix, suffix);
    }

    // In DS the guid is a readable String as well, but it is different then our guid, so we need to show the encoded
    // parameters
    @Override
    protected String getDisplayFilter(LdapQueryMetadata queryMetadata) {
        Object[] encodedFilterParameters =
                getEncodedParameters(queryMetadata.getQueryData().getFilterParameters(),
                        queryMetadata.getLdapGuidEncoder());

        return getFilter(queryMetadata, encodedFilterParameters);
    }

}
