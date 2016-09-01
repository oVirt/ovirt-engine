package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code ActionGroupDao} defines a type for performing CRUD operations on instances of {@link ActionGroup}.
 */
public interface ActionGroupDao extends Dao {
    /**
     * Retrieves all action groups for the specified role.
     *
     * @param id
     *            the role id
     * @return the list of action groups
     */
    List<ActionGroup> getAllForRole(Guid id);
}
