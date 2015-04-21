package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.common.businessentities.EngineBackupLogId;

public interface EngineBackupLogDao extends GenericDao<EngineBackupLog, EngineBackupLogId> {

    /**
     * Gets the last successful engine backup record
     */
    EngineBackupLog getLastSuccessfulEngineBackup(String dbName);
}
