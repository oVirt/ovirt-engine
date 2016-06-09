package org.ovirt.engine.core.vdsbroker.vdsbroker.kubevirt;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

/**
 * For now this class is only responsible for providing a HTTP URL to a service. In the future this would return a
 * prepared HTTP request which already has the right credentials set and is ready to use.
 */
public interface ServiceDiscovery {

    /**
     * Look up a system wide service
     *
     * @param serviceName System wide name of the service
     * @return Service HTTP connection URL
     */
    public String discover(@NotNull String serviceName);

    /**
     * Look up a service running on a specific host/node. Most likely you want a service out of a daemon set.
     *
     * @param serviceName Service name
     * @param vdsId       Id of the host/node where we need the service from
     * @return
     */
    public String discover(@NotNull String serviceName, Guid vdsId);
}
