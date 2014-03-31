package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.core.compat.Guid;

public class DefaultIdEncoder implements LdapIdEncoder {

    @Override
    public String encodedId(Guid id) {
        return id.toString();
    }

}
