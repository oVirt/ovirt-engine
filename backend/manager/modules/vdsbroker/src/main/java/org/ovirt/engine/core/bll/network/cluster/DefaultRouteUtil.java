package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.compat.Guid;

public interface DefaultRouteUtil {
    boolean isDefaultRouteNetwork(Guid networkId, Guid clusterId);
}
