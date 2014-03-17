package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.serverordering;

import java.net.URI;
import java.util.List;

public class LdapServersPutAtLastPlaceAlgorithm extends LdapServersOrderingAlgorithm {

    @Override
    protected void reorderImpl(URI server, List<URI> restOfServers) {
        restOfServers.add(server);
    }
}
