package org.ovirt.engine.core.bll.adbroker.serverordering;

import java.net.URI;
import java.util.List;

public class LdapServersPutAtLastPlaceAlgorithm extends LdapServersOrderingAlgorithm {

    @Override
    protected void reorderImpl(URI server, List<URI> restOfServers) {
        restOfServers.add(server);
    }
}
