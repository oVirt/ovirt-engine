package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.compat.Guid;

public class DefaultIdEncoder implements LdapIdEncoder {

    @Override
    public String encodedId(ExternalId id) {
        Guid guid = new Guid(id.getBytes(), true);
        return guid.toString();
    }

}
