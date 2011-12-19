package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.compat.Guid;

public interface DiskDao extends GenericDao<Disk, Guid> {

    /**
     * Check if the {@link Disk} with the given id exists or not.
     *
     * @param id
     *            The disk's id.
     * @return Does the disk exist or not.
     */
    boolean exists(Guid id);
}
