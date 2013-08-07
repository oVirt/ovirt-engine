package org.ovirt.engine.core.bll.adbroker.serverordering;

import java.net.URI;
import java.util.List;

public class LdapServersNoOpOrderingAlgorithm extends LdapServersOrderingAlgorithm {

    /**
     * Override this since parent removes the server from the list and the
     * reorderImpl implementation of this class does not add the server
     * back to the list. So we end up with empty list of ldap servers.
     * @param server
     * @param servers
     */
    @Override
    public void reorder(URI server, List<URI> servers) {
    }

    @Override
    protected void reorderImpl(URI server, List<URI> restOfServers) {
    }

}
