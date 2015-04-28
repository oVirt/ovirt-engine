package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.common.businessentities.EngineBackupLogId;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;

@Named
@Singleton
public class EngineBackupLogDaoDbFacadeImpl extends AbstractJpaDao<EngineBackupLog, EngineBackupLogId> implements EngineBackupLogDao {

    protected EngineBackupLogDaoDbFacadeImpl() {
        super(EngineBackupLog.class);
    }

    @Override
    public EngineBackupLog getLastSuccessfulEngineBackup(String scope) {
        TypedQuery<EngineBackupLog> query = entityManager.createNamedQuery("EngineBackupLog.getLatest",
                EngineBackupLog.class).setParameter("scope", scope).setMaxResults(1);
        return singleResult(query);
    }
}
