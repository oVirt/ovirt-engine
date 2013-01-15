package org.ovirt.engine.core.bll.adbroker.serverordering;

import java.net.URI;
import java.util.List;

public abstract class LdapServersOrderingAlgorithm {

    protected abstract void reorderImpl(URI server, List<URI> restOfServers);

    public void reorder(URI server, List<URI> servers) {
       if (servers.remove(server)) {
           reorderImpl(server, servers);
       }
    }
}
