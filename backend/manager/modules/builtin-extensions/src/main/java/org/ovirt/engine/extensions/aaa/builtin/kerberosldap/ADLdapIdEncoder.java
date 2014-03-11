package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.compat.Guid;

public class ADLdapIdEncoder implements LdapIdEncoder {
    private static final ADLdapIdEncoder instance = new ADLdapIdEncoder();

    public static ADLdapIdEncoder getInstance() {
        return instance;
    }

    private ADLdapIdEncoder() {
        // Empty on purpose.
    }

    @Override
    public String encodedId(ExternalId id) {
        // AD guid is stored in reversed order than MS-SQL guid -
        // Since it is important for us to work with GUIDs which are MS-SQL
        // aligned,
        // for each GUID -before using with AD we will change its byte order to
        // support AD
        Guid adGuid = new Guid(id.getBytes(), false);
        byte[] bytes = adGuid.toByteArray();
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < bytes.length; idx++) {
            sb.append("\\" + String.format("%02X", bytes[idx]));
        }

        return sb.toString();
    }

}
