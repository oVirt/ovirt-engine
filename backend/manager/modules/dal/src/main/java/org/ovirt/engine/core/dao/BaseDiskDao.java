package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.compat.Guid;

public interface BaseDiskDao extends GenericDao<BaseDisk, Guid> {

    /**
     * Check if the {@link BaseDisk} with the given id exists or not.
     *
     * @param id
     *            The disk's id.
     * @return Does the disk exist or not.
     */
    boolean exists(Guid id);

    /**
     * Get disks by disk alias
     */
    List<BaseDisk> getDisksByAlias(String alias);

}
