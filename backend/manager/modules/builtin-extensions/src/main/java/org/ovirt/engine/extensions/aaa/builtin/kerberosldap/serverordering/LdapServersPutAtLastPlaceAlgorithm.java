package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.serverordering;

import java.util.List;

public class LdapServersPutAtLastPlaceAlgorithm extends LdapServersOrderingAlgorithm {

    @Override
    protected void reorderImpl(String server, List<String> restOfServers) {
        restOfServers.add(server);
    }
}
