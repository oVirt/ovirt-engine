package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

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
                        queryMetadata.getLdapIdEncoder());

        return getFilter(queryMetadata, encodedFilterParameters);
    }

}
