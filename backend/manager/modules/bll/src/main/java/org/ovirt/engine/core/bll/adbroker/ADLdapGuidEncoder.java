package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;

public class ADLdapGuidEncoder implements LdapGuidEncoder {
    private static final ADLdapGuidEncoder instance = new ADLdapGuidEncoder();

    public static ADLdapGuidEncoder getInstance() {
        return instance;
    }

    private ADLdapGuidEncoder() {
        // Empty on purpose.
    }

    @Override
    public String encodeGuid(Guid guid) {
        byte[] ba = guid.toByteArray();

        // AD guid is stored in reversed order than MS-SQL guid -
        // Since it is important for us to work with GUIDs which are MS-SQL
        // aligned,
        // for each GUID -before using with AD we will change its byte order to
        // support AD
        Guid adGuid = new Guid(ba, false);
        ba = adGuid.toByteArray();
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < ba.length; idx++) {
            sb.append("\\" + String.format("%02X", ba[idx]));
        }

        return sb.toString();
    }

}
