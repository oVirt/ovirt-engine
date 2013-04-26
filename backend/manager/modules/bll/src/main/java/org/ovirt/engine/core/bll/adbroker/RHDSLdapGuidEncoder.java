package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;

public class RHDSLdapGuidEncoder implements LdapGuidEncoder {
    private static final RHDSLdapGuidEncoder instance = new RHDSLdapGuidEncoder();

    public static RHDSLdapGuidEncoder getInstance() {
        return instance;
    }

    private RHDSLdapGuidEncoder() {
        // Empty on purpose.
    }

    private String getNsUniqueIdFromGuidString(String guidString) {
        // 12345678-1234-5678-1234-567812345678 -->
        // 12345678-12345678-12345678-12345678
        StringBuilder sb = new StringBuilder();
        sb.append(guidString.substring(0, 13))
                .append(guidString.substring(14, 23))
                .append(guidString.substring(24, 28))
                .append("-")
                .append(guidString.substring(28, 36));
        return sb.toString();
    }

    @Override
    public String encodeGuid(Guid guid) {
        return getNsUniqueIdFromGuidString(guid.toString());
    }

}

