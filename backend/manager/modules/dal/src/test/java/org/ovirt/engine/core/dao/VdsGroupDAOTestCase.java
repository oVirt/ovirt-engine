package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;

public class VdsGroupDAOTestCase extends BaseDAOTestCase {
    private static final int NUMBER_OF_GROUPS = 6;

    private VdsGroupDAO dao;
    private VDS existingVds;
    private VDSGroup existingVdsGroup;
    private VDSGroup newGroup;
    private VDSGroup groupWithNoRunningVms;
    private storage_pool storagePool;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        VdsDAO vdsDAO = prepareDAO(dbFacade.getVdsDAO());

        existingVds = vdsDAO
                .get(new NGuid("afce7a39-8e8c-4819-ba9c-796d316592e7"));

        StoragePoolDAO storagePoolDAO = prepareDAO(dbFacade.getStoragePoolDAO());

        storagePool = storagePoolDAO.get(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));

        dao = prepareDAO(dbFacade.getVdsGroupDAO());

        existingVdsGroup = dao.get(existingVds.getvds_group_id());
        groupWithNoRunningVms = dbFacade.getVdsGroupDAO().get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d3"));

        newGroup = new VDSGroup();
        newGroup.setname("New VDS Group");
        newGroup.setcompatibility_version(new Version("3.0"));
    }

    /**
     * Ensures that the id must be valid.
     */
    @Test
    public void testGetWithInvalidId() {
        VDSGroup result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that null is returned.
     */
    @Test
    public void testGetWithRunningVmsWhereThereAreNone() {
        VDSGroup result = dao.getWithRunningVms(groupWithNoRunningVms.getID());

        assertNull(result);
    }

    /**
     * Ensures that the VDS group is returned.
     */
    @Test
    public void testGetWithRunningVms() {
        VDSGroup result = dao.getWithRunningVms(existingVdsGroup.getID());

        assertNotNull(result);
    }

    /**
     * Ensures that retrieving a group works as expected.
     */
    @Test
    public void testGet() {
        VDSGroup result = dao.get(existingVdsGroup.getID());

        assertNotNull(result);
        assertEquals(existingVdsGroup, result);
    }

    /**
     * Ensures that a bad name result in a null group.
     */
    @Test
    public void testGetByNameWithBadName() {
        VDSGroup result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures that the right group is returned.
     */
    @Test
    public void testGetByName() {
        VDSGroup result = dao.getByName(existingVdsGroup.getname());

        assertNotNull(result);
        assertEquals(existingVdsGroup, result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<VDSGroup> result = dao.getAllForStoragePool(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right group is returned.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<VDSGroup> result = dao.getAllForStoragePool(storagePool.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDSGroup group : result) {
            assertEquals(storagePool.getId(), group.getstorage_pool_id());
        }
    }

    /**
     * Ensures that the right number of groups are returned.
     */
    @Test
    public void testGetAll() {
        List<VDSGroup> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NUMBER_OF_GROUPS, result.size());
    }

    /**
     * Ensures saving a group works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newGroup);

        VDSGroup result = dao.getByName(newGroup.getname());

        assertNotNull(result);
        assertEquals(newGroup, result);
    }

    /**
     * Ensures that updating a group works as expected.
     */
    @Test
    public void testUpdate() {
        String oldName = existingVdsGroup.getname();

        existingVdsGroup.setname("This is the new name");
        dao.update(existingVdsGroup);

        VDSGroup result = dao.get(existingVdsGroup.getID());

        assertNotNull(result);
        assertEquals(existingVdsGroup, result);

        result = dao.getByName(oldName);

        assertNull(result);
    }

    /**
     * Ensures that removing a group works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(groupWithNoRunningVms.getID());

        VDSGroup result = dao.get(groupWithNoRunningVms.getID());

        assertNull(result);
    }
}
