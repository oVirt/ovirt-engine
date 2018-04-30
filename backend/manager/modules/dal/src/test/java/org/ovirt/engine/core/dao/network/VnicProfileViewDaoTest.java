package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class VnicProfileViewDaoTest extends BaseDaoTestCase<VnicProfileViewDao> {
    @Inject
    private NetworkDao networkDao;

    /**
     * Ensures the right set of vnic profiles is returned for the given data center.
     */
    @Test
    public void testGetAllForDataCenter() {
        List<VnicProfileView> result = dao.getAllForDataCenter(FixturesTool.DATA_CENTER);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (VnicProfileView profile : result) {
            assertEquals(FixturesTool.DATA_CENTER_NAME, profile.getDataCenterName());
        }
    }

    /**
     * Ensures the right set of vnic profiles is returned for the given cluster id.
     */
    @Test
    public void testGetAllForCluster() {
        List<VnicProfileView> result = dao.getAllForCluster(FixturesTool.CLUSTER);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        final Set<Guid> clusterNetworkIds = findClusterNetworkIds(FixturesTool.CLUSTER);
        for (VnicProfileView profile : result) {
            assertTrue(clusterNetworkIds.contains(profile.getNetworkId()));
        }
    }

    private Set<Guid> findClusterNetworkIds(Guid clusterId) {
        return networkDao.getAllForCluster(clusterId)
                .stream()
                .map(Network::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Ensures the right set of vnic profiles is returned for the given data center.
     */
    @Test
    public void testGetAllForDataCenterWithPermissions() {
        List<VnicProfileView> result = dao.getAllForDataCenter(FixturesTool.DATA_CENTER, PRIVILEGED_USER_ID, true);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (VnicProfileView profile : result) {
            assertEquals(FixturesTool.DATA_CENTER_NAME, profile.getDataCenterName());
        }
    }

    /**
     * Ensures the right set of vnic profiles is returned for the given data center.
     */
    @Test
    public void testGetAllForDataCenterWithNoPermissions() {
        List<VnicProfileView> result = dao.getAllForDataCenter(FixturesTool.DATA_CENTER, UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the network interface profile is returned.
     */
    @Test
    public void testGetForUserWithPermission() {
        // This user has permissions on the network, hence he has permissions on the associated profiles
        VnicProfileView result = dao.get(FixturesTool.VM_NETWORK_INTERFACE_PROFILE, PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertEquals(FixturesTool.VM_NETWORK_INTERFACE_PROFILE, result.getId());
    }

    /**
     * Ensures that no network interface profile is returned.
     */
    @Test
    public void testGetForUserWithoutPermission() {
        VnicProfileView result = dao.get(FixturesTool.VM_NETWORK_INTERFACE_PROFILE, UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    /**
     * Ensures that a single profile is returned.
     */
    @Test
    public void testGetAllForNetworkForUserWithPermission() {
        List<VnicProfileView> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE, PRIVILEGED_USER_ID, true);

        // this user has permissions on the network, hence he has permissions on the associated profiles
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForNetworkForUserWithoutPermission() {
        List<VnicProfileView> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE, UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a single profile is returned.
     */
    @Test
    public void testGetAllForUserWithPermission() {
        List<VnicProfileView> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForUserWithoutPermission() {
        List<VnicProfileView> result = dao.getAll(UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
