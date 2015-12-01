package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.KeyValue;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.compat.Guid;

public class BusinessEntitySnapshotDaoTest extends BaseDaoTestCase {
    private BusinessEntitySnapshotDao dao;
    private Guid commandWithTwoSnapshotsId = new Guid("48e85ac4-17cc-40d1-8b4e-37edcea8d78a");
    private Guid commandWithOneSnapshotId = new Guid("0dcaa6a5-16fe-4270-8614-5dd249a057e0");

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getBusinessEntitySnapshotDao();
    }


    @Test
    public void testGetByCommandId() {
        List<BusinessEntitySnapshot> snapshots = dao.getAllForCommandId(commandWithTwoSnapshotsId);
        assertTrue(snapshots != null);
        assertEquals(2, snapshots.size());
        snapshots = dao.getAllForCommandId(commandWithOneSnapshotId);
        assertTrue(snapshots != null);
        assertEquals(1, snapshots.size());
    }

    @Test
    public void testInsertSnapshot() {
        BusinessEntitySnapshot snapshot = new BusinessEntitySnapshot();
        Guid commandId = Guid.newGuid();
        snapshot.setCommandId(commandId);
        snapshot.setCommandType("org.ovirt.engine.core.bll.UpdateVdsCommand");
        snapshot.setEntityId(Guid.newGuid().toString());
        snapshot.setEntityType("org.ovirt.engine.core.common.businessentities.VdsStatic");
        snapshot.setEntitySnapshot("something");
        snapshot.setSnapshotClass("someClass");
        dao.save(snapshot);
        List<BusinessEntitySnapshot> snapshotsFromDb = dao.getAllForCommandId(commandId);
        assertNotNull(snapshotsFromDb);
        assertEquals(1, snapshotsFromDb.size());
        assertEquals(snapshot, snapshotsFromDb.get(0));
    }

    @Test
    public void testDeleteByCommandId() {
        int numberOfResultsBeforeDeletion = 2;
        Guid commandId = commandWithTwoSnapshotsId;
            testDeleteByCommandId(numberOfResultsBeforeDeletion, commandId);
        numberOfResultsBeforeDeletion = 1;
        commandId = commandWithOneSnapshotId;
        testDeleteByCommandId(numberOfResultsBeforeDeletion, commandId);
    }

    @Test
    public void testGetAllCommands() {
        List<KeyValue> snapshots = dao.getAllCommands();
        assertNotNull(snapshots);
        assertEquals(2, snapshots.size());
        Set<Guid> expectedSet = new HashSet<>();
        expectedSet.add(commandWithOneSnapshotId);
        expectedSet.add(commandWithTwoSnapshotsId);
        Set<Guid> resultSet = new HashSet<>();
        for (KeyValue snapshot : snapshots) {
            resultSet.add((Guid)snapshot.getKey());
        }
        assertEquals(expectedSet, resultSet);
    }

    private void testDeleteByCommandId(int numberOfResultsBeforeDeletion, Guid commandId) {
        List<BusinessEntitySnapshot> snapshots = dao.getAllForCommandId(commandId);
        assertTrue(snapshots != null);
        assertEquals(numberOfResultsBeforeDeletion, snapshots.size());
        dao.removeAllForCommandId(commandId);
        snapshots = dao.getAllForCommandId(commandId);
        assertTrue(snapshots != null);
        assertEquals(0, snapshots.size());
    }
}
