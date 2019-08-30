package org.ovirt.engine.core.bll.host;

import org.ovirt.engine.core.common.businessentities.VDS;

public interface Updateable {

    /**
     * Performs the update
     * @param host
     *            the host to be updated
     * @param timeout
     *            maximum time to wait for the host to be updated
     */
    void update(final VDS host, int timeout);
}
