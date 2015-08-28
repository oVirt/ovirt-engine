package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.EngineBackupLog;

public interface EngineBackupLogDao extends Dao {

    /**
     * Gets the last successful engine backup record
     */
    EngineBackupLog getLastSuccessfulEngineBackup(String scope);

    /**
     * Save an engine backup log entity to the db
     *
     * @param engineBackupLog
     *            the entity to save
     */
    void save(EngineBackupLog engineBackupLog);
}
