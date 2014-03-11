package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.core.common.utils.ExternalId;

public interface LdapIdEncoder {
    public String encodedId(ExternalId id);
}
