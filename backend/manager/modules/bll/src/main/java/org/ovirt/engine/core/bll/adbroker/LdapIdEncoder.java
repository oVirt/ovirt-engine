package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.utils.ExternalId;

public interface LdapIdEncoder {
    public String encodedId(ExternalId id);
}
