package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class HostNicVfsConfigDaoTest extends BaseDAOTestCase {
    private HostNicVfsConfigDao dao;

    private static final int NUM_OF_CONFIGS = 3;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getHostNicVfsConfigDao();
    }

    /**
     * Ensures that a null hostNicVfsConfig is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        HostNicVfsConfig result = dao.get(Guid.Empty);

        assertNull(result);
    }

    /**
     * Ensures that retrieving a hostNicVfsConfig by id works as expected.
     */
    @Test
    public void testGetById() {
        HostNicVfsConfig result = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_ALL_NETWORKS_ALLOWED);

        assertNotNull(result);
        assertEquals(FixturesTool.HOST_NIC_VFS_CONFIG_ALL_NETWORKS_ALLOWED, result.getId());
    }

    /**
     * Ensures that all hostNicVfsConfig are returned.
     */
    @Test
    public void testGetAll() {
        List<HostNicVfsConfig> result = dao.getAll();

        assertNotNull(result);
        assertEquals(NUM_OF_CONFIGS, result.size());
    }

    /**
     * Ensures that saving a hostNicVfsConfig works as expected.
     */
    @Test
    public void testSave() {
        HostNicVfsConfig newConfig = new HostNicVfsConfig();

        newConfig.setId(Guid.newGuid());
        newConfig.setNicId(FixturesTool.VDS_NETWORK_INTERFACE_WITHOUT_QOS);
        newConfig.setAllNetworksAllowed(true);

        dao.save(newConfig);

        HostNicVfsConfig result = dao.get(newConfig.getId());

        assertNotNull(result);
        assertHostNicVfsConfigEquals(newConfig, result);
    }

    /**
     * Ensures updating a hostNicVfsConfig works as expected.
     */
    @Test
    public void testUpdate() {
        HostNicVfsConfig before = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_ALL_NETWORKS_ALLOWED);

        before.setNicId(FixturesTool.VDS_NETWORK_INTERFACE_WITHOUT_QOS);
        before.setAllNetworksAllowed(!before.isAllNetworksAllowed());

        dao.update(before);

        HostNicVfsConfig after = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_ALL_NETWORKS_ALLOWED);

        assertNotNull(after);
        assertHostNicVfsConfigEquals(before, after);
    }

    /**
     * Ensures that removing a hostNicVfsConfig works as expected.
     */
    @Test
    public void testRemove() {
        HostNicVfsConfig result = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_ALL_NETWORKS_ALLOWED);

        assertNotNull(result);

        dao.remove(result.getId());

        result = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_ALL_NETWORKS_ALLOWED);

        assertNull(result);
    }

    private void assertHostNicVfsConfigEquals(HostNicVfsConfig config1, HostNicVfsConfig config2) {
        assertEquals(config1.getId(), config2.getId());
        assertEquals(config1.getNicId(), config2.getNicId());
        assertEquals(config1.isAllNetworksAllowed(), config2.isAllNetworksAllowed());
    }
}
