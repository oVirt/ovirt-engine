package org.ovirt.engine.core.dao;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;

import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.common.businessentities.EngineBackupLogId;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;

@Named
@Singleton
public class EngineBackupLogDaoImpl extends AbstractJpaDao<EngineBackupLog, EngineBackupLogId> implements EngineBackupLogDao {

    protected EngineBackupLogDaoImpl() {
        super(EngineBackupLog.class);
    }

    @Override
    public EngineBackupLog getLastSuccessfulEngineBackup(String scope) {
        TypedQuery<EngineBackupLog> query = entityManager.createNamedQuery("EngineBackupLog.getLatest",
                EngineBackupLog.class).setParameter("scope", scope).setMaxResults(1);
        return singleResult(query);
    }
}
