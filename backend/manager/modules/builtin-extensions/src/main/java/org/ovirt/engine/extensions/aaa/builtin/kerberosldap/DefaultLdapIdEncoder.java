package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.core.compat.Guid;

public class DefaultLdapIdEncoder implements LdapIdEncoder {
    private static DefaultLdapIdEncoder instance = new DefaultLdapIdEncoder();

    public static DefaultLdapIdEncoder getInstance() {
        return instance;
    }

    private DefaultLdapIdEncoder() {
        // Empty on purpose.
    }

    @Override
    public String encodedId(Guid id) {
        return id.toString();
    }

}
