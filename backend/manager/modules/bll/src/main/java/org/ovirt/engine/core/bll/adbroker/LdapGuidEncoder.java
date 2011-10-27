package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;

public interface LdapGuidEncoder {
    public String encodeGuid( Guid guid );
}
