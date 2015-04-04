package org.ovirt.engine.core.dao;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.EngineBackupLog;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EngineBackupLogDaoTest extends BaseDAOTestCase {

    private EngineBackupLogDao dao;
    private EngineBackupLog existingEngineBackupLog;
    private final static String DB_NAME = "engine";
    private final static String NON_EXISTING_DB_NAME = "invalid";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getEngineBackupLogDao();
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
        existingEngineBackupLog = dao.getLastSuccessfulEngineBackup(DB_NAME);
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
        existingEngineBackupLog = dao.getLastSuccessfulEngineBackup(DB_NAME);
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

}
