package org.ovirt.engine.core.dao;

import java.util.List;

import org.apache.commons.collections.KeyValue;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.compat.Guid;

/**
 * Dao for handling business entity snapshots
 */
public interface BusinessEntitySnapshotDao extends Dao {

    /**
     * Gets all snapshots kept for a command
     * @param commandID the command ID to look snapshots for
     * @return list containing snapshots for the command. In case no snapshot is found, return an empty list
     */
    public List<BusinessEntitySnapshot> getAllForCommandId(Guid commandID);

    /**
     * Removes all snapshots for a given command
     * @param commandID the command ID to remove  snapshots for
     */
    public void removeAllForCommandId(Guid   commandID);

    /**
     * Saves a new snapshot
     * @param entitySnapshot the snapshot to save
     */
    public void save(BusinessEntitySnapshot entitySnapshot);

    /**
     * Get all commands awaiting for compensation.
     */
    List<KeyValue> getAllCommands();
}

