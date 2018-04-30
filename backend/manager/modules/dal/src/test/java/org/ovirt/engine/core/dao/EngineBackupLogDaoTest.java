package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.utils.RandomUtils;

public class EngineBackupLogDaoTest extends BaseDaoTestCase<EngineBackupLogDao> {
    private EngineBackupLog existingEngineBackupLog;
    private EngineBackupLog newEntity;
    private static final String SCOPE = "db";
    private static final String NON_EXISTING_SCOPE = "invalid";

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        existingEngineBackupLog = dao.getLastSuccessfulEngineBackup(SCOPE);
        newEntity = new EngineBackupLog();
        newEntity.setScope(RandomUtils.instance().nextString(20));
        newEntity.setDoneAt(new Date());
        newEntity.setPassed(true);
        newEntity.setOutputMessage(RandomUtils.instance().nextString(20));
        newEntity.setFqdn(RandomUtils.instance().nextString(20));
        newEntity.setLogPath(RandomUtils.instance().nextString(20));
    }

    @Test
    public void testGetLastSuccessfulEngineBackup() {
        existingEngineBackupLog = dao.getLastSuccessfulEngineBackup(SCOPE);
        assertNotNull(existingEngineBackupLog);
    }

    @Test
    public void testGetLastSuccessfulEngineBackupWithWrongDbName() {
        existingEngineBackupLog = dao.getLastSuccessfulEngineBackup(NON_EXISTING_SCOPE);
        assertNull(existingEngineBackupLog);
    }

    @Test
    public void testAddingNewUnsuccessfulBackupEvent() {
        EngineBackupLog engineBackupLog = new EngineBackupLog();
        engineBackupLog.setScope(SCOPE);
        engineBackupLog.setDoneAt(Calendar.getInstance().getTime());
        engineBackupLog.setPassed(false);
        engineBackupLog.setOutputMessage("backup failed");
        engineBackupLog.setFqdn(RandomUtils.instance().nextString(20));
        engineBackupLog.setLogPath(RandomUtils.instance().nextString(20));
        dao.save(engineBackupLog);
        EngineBackupLog entry = dao.getLastSuccessfulEngineBackup(SCOPE);
        assertNotNull(entry);
        assertEquals(entry, existingEngineBackupLog);
    }

    @Test
    public void testAddingNewSuccessfulBackupEvent() {
        EngineBackupLog engineBackupLog = new EngineBackupLog();
        engineBackupLog.setScope(SCOPE);
        engineBackupLog.setDoneAt(Calendar.getInstance().getTime());
        engineBackupLog.setPassed(true);
        engineBackupLog.setOutputMessage("backup completed successfully");
        engineBackupLog.setFqdn(RandomUtils.instance().nextString(20));
        engineBackupLog.setLogPath(RandomUtils.instance().nextString(20));
        dao.save(engineBackupLog);
        EngineBackupLog entry = dao.getLastSuccessfulEngineBackup(SCOPE);
        assertNotNull(entry);
        assertNotEquals(entry.getDoneAt(), existingEngineBackupLog.getDoneAt());
        assertTrue(entry.isPassed());
    }
}
