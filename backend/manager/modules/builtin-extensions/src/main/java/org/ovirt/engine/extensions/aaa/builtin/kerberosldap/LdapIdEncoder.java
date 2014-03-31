package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.core.compat.Guid;

public interface LdapIdEncoder {
    public String encodedId(Guid id);
}
