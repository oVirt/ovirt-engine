package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.utils.ExternalId;
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
    public String encodedId(ExternalId id) {
        Guid guid = new Guid(id.getBytes(), true);
        return guid.toString();
    }

}
