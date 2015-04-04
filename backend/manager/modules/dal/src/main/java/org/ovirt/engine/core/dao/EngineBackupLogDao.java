package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.EngineBackupLog;

public interface EngineBackupLogDao extends DAO {

    /**
     * Gets the last successful engine backup record
     */
    EngineBackupLog getLastSuccessfulEngineBackup(String dbName);

    void save(EngineBackupLog engineBackupLog);
}
