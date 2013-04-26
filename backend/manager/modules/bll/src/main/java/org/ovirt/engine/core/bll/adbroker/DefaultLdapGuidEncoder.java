package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;

public class DefaultLdapGuidEncoder implements LdapGuidEncoder {
    private static DefaultLdapGuidEncoder instance = new DefaultLdapGuidEncoder();

    public static DefaultLdapGuidEncoder getInstance() {
        return instance;
    }

    private DefaultLdapGuidEncoder() {
        // Empty on purpose.
    }

    @Override
    public String encodeGuid(Guid guid) {
        return guid.toString();
    }

}
