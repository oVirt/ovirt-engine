package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.serverordering;

import java.util.List;

public abstract class LdapServersOrderingAlgorithm {

    protected abstract void reorderImpl(String server, List<String> restOfServers);

    /**
     * It may reorder {@code server} in {@code servers} list. WARNING: this method should not be called from within
     * block that iterates through {@code servers} list!
     *
     * @param server
     *            server to be reordered
     * @param servers
     *            list of servers
     */
    public void reorder(String server, List<String> servers) {
        if (servers.remove(server)) {
            reorderImpl(server, servers);
        }
    }
}
