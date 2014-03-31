package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.core.compat.Guid;

public class RHDSLdapIdEncoder implements LdapIdEncoder {
    private static final RHDSLdapIdEncoder instance = new RHDSLdapIdEncoder();

    public static RHDSLdapIdEncoder getInstance() {
        return instance;
    }

    private RHDSLdapIdEncoder() {
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
    public String encodedId(Guid id) {
        return getNsUniqueIdFromGuidString(id.toString());
    }

}

