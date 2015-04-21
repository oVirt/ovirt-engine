package org.ovirt.engine.core.dao;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.common.businessentities.EngineBackupLogId;
import org.ovirt.engine.core.utils.RandomUtils;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EngineBackupLogDaoTest extends BaseHibernateDaoTestCase<EngineBackupLogDao, EngineBackupLog, EngineBackupLogId> {

    private EngineBackupLogDao dao;
    private EngineBackupLog existingEngineBackupLog;
    private EngineBackupLog newEntity;
    private final static String DB_NAME = "engine";
    private final static String NON_EXISTING_DB_NAME = "invalid";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getEngineBackupLogDao();
        existingEngineBackupLog = dao.getLastSuccessfulEngineBackup(DB_NAME);
        newEntity = new EngineBackupLog();
        newEntity.setDbName(RandomUtils.instance().nextString(20));
        newEntity.setOutputMessage(RandomUtils.instance().nextString(20));
        newEntity.setDoneAt(new Date());
        newEntity.setPassed(true);
    }

    @Test
    public void testGetLastSuccessfulEngineBackup() {
        existingEngineBackupLog = dao.getLastSuccessfulEngineBackup(DB_NAME);
        assertNotNull(existingEngineBackupLog);
    }

    @Test
    public void testGetLastSuccessfulEngineBackupWithWrongDbName() {
        existingEngineBackupLog = dao.getLastSuccessfulEngineBackup(NON_EXISTING_DB_NAME);
        assertNull(existingEngineBackupLog);
    }

    @Test
    public void testAddingNewUnsuccessfulBackupEvent() {
        EngineBackupLog engineBackupLog = new EngineBackupLog();
        engineBackupLog.setDbName(DB_NAME);
        engineBackupLog.setDoneAt(Calendar.getInstance().getTime());
        engineBackupLog.setPassed(false);
        engineBackupLog.setOutputMessage("backup failed");
        dao.save(engineBackupLog);
        EngineBackupLog entry = dao.getLastSuccessfulEngineBackup(DB_NAME);
        assertNotNull(entry);
        assertEquals(entry, existingEngineBackupLog);
    }

    @Test
    public void testAddingNewSuccessfulBackupEvent() {
        EngineBackupLog engineBackupLog = new EngineBackupLog();
        engineBackupLog.setDbName(DB_NAME);
        engineBackupLog.setDoneAt(Calendar.getInstance().getTime());
        engineBackupLog.setPassed(true);
        engineBackupLog.setOutputMessage("backup completed successfully");
        dao.save(engineBackupLog);
        EngineBackupLog entry = dao.getLastSuccessfulEngineBackup(DB_NAME);
        assertNotNull(entry);
        assertNotEquals(entry.getDoneAt(), existingEngineBackupLog.getDoneAt());
        assertTrue(entry.isPassed());
    }

    @Override protected EngineBackupLogDao getDao() {
        return dao;
    }

    @Override protected EngineBackupLog getExistingEntity() {
        return existingEngineBackupLog;
    }

    @Override protected EngineBackupLog getNonExistentEntity() {
        return newEntity;
    }

    @Override protected int getAllEntitiesCount() {
        return 1;
    }

    @Override protected EngineBackupLog modifyEntity(EngineBackupLog entity) {
        entity.setOutputMessage("test");
        return entity;
    }

    @Override protected void verifyEntityModification(EngineBackupLog result) {
        assertEquals("test", result.getOutputMessage());
    }
}
