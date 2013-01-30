package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class storage_pool_iso_map_id implements Serializable {
    private static final long serialVersionUID = -3579958698510291360L;

    Guid storageId;
    NGuid storagePoolId;
}
