package org.ovirt.engine.core.bll.adbroker.serverordering;

import java.net.URI;
import java.util.List;

public abstract class LdapServersOrderingAlgorithm {

    protected abstract void reorderImpl(URI server, List<URI> restOfServers);

    /**
     * It may reorder {@code server} in {@code servers} list. WARNING: this method should not be called from within
     * block that iterates through {@code servers} list!
     *
     * @param server
     *            server to be reordered
     * @param servers
     *            list of servers
     */
    public void reorder(URI server, List<URI> servers) {
        if (servers.remove(server)) {
            reorderImpl(server, servers);
        }
    }
}
