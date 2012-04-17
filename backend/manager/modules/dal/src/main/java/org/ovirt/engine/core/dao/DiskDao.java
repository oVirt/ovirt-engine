package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.compat.Guid;

public interface DiskDao extends ReadDao<Disk, Guid> {
    // No special methods ATM.
}
