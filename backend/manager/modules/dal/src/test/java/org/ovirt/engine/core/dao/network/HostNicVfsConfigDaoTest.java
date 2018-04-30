package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class HostNicVfsConfigDaoTest extends BaseDaoTestCase<HostNicVfsConfigDao> {

    // Map the vfsConfig id to the number of networks and labels it has
    private static final Map<Guid, Pair<Integer, Integer>> EXPECTED_GUIDS;
    static {
        Map<Guid, Pair<Integer, Integer>> tmpMap = new HashMap<>();
        tmpMap.put(FixturesTool.HOST_NIC_VFS_CONFIG, new Pair<>(0, 0));
        tmpMap.put(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_1, new Pair<>(2, 2));
        tmpMap.put(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2, new Pair<>(1, 3));
        EXPECTED_GUIDS = Collections.unmodifiableMap(tmpMap);
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
        HostNicVfsConfig result = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG);

        assertNotNull(result);
        assertEquals(FixturesTool.HOST_NIC_VFS_CONFIG, result.getId());
        checkNetworksAndLabels(result, 0, 0);
    }

    /**
     * Ensures that retrieving a hostNicVfsConfig with {@code allNetworkAllowed=false} by id works as expected.
     */
    @Test
    public void testGetByIdNotAllNetworksAllowed() {
        HostNicVfsConfig result = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_1);

        assertNotNull(result);
        assertEquals(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_1, result.getId());
        checkNetworksAndLabels(result, 2, 2);
    }

    /**
     * Ensures that all hostNicVfsConfig are returned.
     */
    @Test
    public void testGetAll() {
        List<HostNicVfsConfig> result = dao.getAll();

        assertNotNull(result);
        assertEquals(EXPECTED_GUIDS.size(), result.size());

        for (HostNicVfsConfig vfsConfig : result) {
            assertTrue(EXPECTED_GUIDS.containsKey(vfsConfig.getId()));
            Pair<Integer, Integer> expectedConfig = EXPECTED_GUIDS.get(vfsConfig.getId());
            checkNetworksAndLabels(vfsConfig, expectedConfig.getFirst(), expectedConfig.getSecond());
        }
    }

    /**
     * Ensures that a null hostNicVfsConfig is returned.
     */
    @Test
    public void testGetByNicWithInvalidNicId() {
        HostNicVfsConfig result = dao.getByNicId(Guid.Empty);

        assertNull(result);
    }

    /**
     * Ensures that retrieving a hostNicVfsConfig by id works as expected.
     */
    @Test
    public void testGetByNicId() {
        HostNicVfsConfig result = dao.getByNicId(FixturesTool.VDS_NETWORK_INTERFACE);

        assertNotNull(result);
        assertEquals(FixturesTool.VDS_NETWORK_INTERFACE, result.getNicId());
    }

    /**
     * Ensures that saving a hostNicVfsConfig works as expected. No network and labels are added.
     */
    @Test
    public void testSave() {
        commonSave(true);
    }

    /**
     * Ensures that saving a hostNicVfsConfig works as expected. Network and labels are added.
     */
    @Test
    public void testSaveAddNetworksAndLabels() {
        commonSave(false);
    }

    private void commonSave(boolean allNetworksAllowed) {
        HostNicVfsConfig newConfig = new HostNicVfsConfig();

        newConfig.setId(Guid.newGuid());
        newConfig.setNicId(FixturesTool.VDS_NETWORK_INTERFACE_WITHOUT_QOS);

        newConfig.setAllNetworksAllowed(allNetworksAllowed);

        if (!allNetworksAllowed) {
            addNetworks(newConfig);
            addLabels(newConfig);
        }

        dao.save(newConfig);

        HostNicVfsConfig result = dao.get(newConfig.getId());

        assertNotNull(result);
        assertHostNicVfsConfigEquals(newConfig, result);
    }

    /**
     * Ensures that saving a hostNicVfsConfig works as expected. AllNetworkAllowed is true before and after the update.
     */
    @Test
    public void testUpdate() {
        commonUpdate(FixturesTool.HOST_NIC_VFS_CONFIG, true);
    }

    /**
     * Ensures that saving a hostNicVfsConfig works as expected. AllNetworkAllowed is true before the update and false
     * after the update.
     */
    @Test
    public void testUpdateAddNetworkAndLabel() {
        commonUpdate(FixturesTool.HOST_NIC_VFS_CONFIG, false);
    }

    /**
     * Ensures that saving a hostNicVfsConfig works as expected. AllNetworkAllowed is false before the update and true
     * after the update.
     */
    @Test
    public void testUpdateRemoveNetworksAndLabels() {
        commonUpdate(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2, true);
    }

    /**
     * Ensures that saving a hostNicVfsConfig works as expected. AllNetworkAllowed is false before the update and false
     * after the update. The networks and the labels are modified.
     */
    @Test
    public void testUpdateModifyNetworkAndLabels() {
        commonUpdate(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2, false);
    }

    private void commonUpdate(Guid vfsConfigId, boolean allNetworksAllowed) {
        HostNicVfsConfig before = dao.get(vfsConfigId);

        before.setNicId(FixturesTool.VDS_NETWORK_INTERFACE_WITHOUT_QOS);
        before.setMaxNumOfVfs(before.getMaxNumOfVfs() + 1);
        before.setNumOfVfs(before.getNumOfVfs() + 1);
        before.setAllNetworksAllowed(allNetworksAllowed);

        if (allNetworksAllowed) {
            before.setNetworks(new HashSet<>());
            before.setNetworkLabels(new HashSet<>());
        } else {
            addNetworks(before);
            addLabels(before);
        }

        dao.update(before);

        HostNicVfsConfig after = dao.get(vfsConfigId);

        assertNotNull(after);
        assertHostNicVfsConfigEquals(before, after);
    }

    private void addLabels(HostNicVfsConfig newConfig) {
        Set<String> labels = new HashSet<>();
        labels.add("lbl1");
        labels.add("lbl2");
        labels.add("lbl3");
        labels.add("lbl4");
        newConfig.setNetworkLabels(labels);
    }

    private void addNetworks(HostNicVfsConfig newConfig) {
        Set<Guid> networks = new HashSet<>();
        networks.add(FixturesTool.NETWORK_ENGINE);
        networks.add(FixturesTool.NETWORK_ENGINE_2);
        newConfig.setNetworks(networks);
    }

    /**
     * Ensures that removing a hostNicVfsConfig works as expected.
     */
    @Test
    public void testRemove() {
        commonRemove(FixturesTool.HOST_NIC_VFS_CONFIG);
    }

    /**
     * Ensures that removing a hostNicVfsConfig works as expected. The deleted vfsConfig has networks and labels, the
     * test makes sure they were deleted as well.
     */
    @Test
    public void testRemoveWithNetworkAndLabels() {
        commonRemove(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_1);

        Set<Guid> networks =
                ((HostNicVfsConfigDaoImpl) dao).getNetworksByVfsConfigId(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_1);
        Set<String> labels =
                ((HostNicVfsConfigDaoImpl) dao).getLabelsByVfsConfigId(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_1);

        checkNetworks(networks, 0);
        checkLabels(labels, 0);
    }

    private void commonRemove(Guid id) {
        HostNicVfsConfig result = dao.get(id);

        assertNotNull(result);

        dao.remove(result.getId());

        result = dao.get(id);

        assertNull(result);
    }

    private void checkNetworks(HostNicVfsConfig vfsConfig, int numOfNetworks) {
        checkNetworks(vfsConfig.getNetworks(), numOfNetworks);
    }

    private void checkNetworks(Set<Guid> networks, int numOfNetworks) {
        assertEquals(numOfNetworks, getListSize(networks), "num of networks");
    }

    private void checkLabels(HostNicVfsConfig vfsConfig, int numOfLabels) {
        checkLabels(vfsConfig.getNetworkLabels(), numOfLabels);
    }

    private void checkLabels(Set<String> labels, int numOfLabels) {
        assertEquals(numOfLabels, getListSize(labels), "num of labels");
    }

    private int getListSize(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    private void checkNetworksAndLabels(HostNicVfsConfig result, int numOfNetworks, int numOfLabels) {
        checkLabels(result, numOfLabels);
        checkNetworks(result, numOfNetworks);
    }

    private void assertHostNicVfsConfigEquals(HostNicVfsConfig config1, HostNicVfsConfig config2) {
        assertEquals(config1.getId(), config2.getId(), "id");
        assertEquals(config1.getNicId(), config2.getNicId(), "nic_id");
        assertEquals(config1.isAllNetworksAllowed(), config2.isAllNetworksAllowed(), "all_networks_allowed");
        assertEquals(config1.getNetworks(), config2.getNetworks(), "networks");
        assertEquals(config1.getNetworkLabels(), config2.getNetworkLabels(), "labels");
    }

    @Test
    public void testAddNetwork() {
        HostNicVfsConfig vfsConfig = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2);
        checkNetworks(vfsConfig, 1);

        dao.addNetwork(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2, FixturesTool.NETWORK_ENGINE_2);

        vfsConfig = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2);
        checkNetworks(vfsConfig, 2);
    }

    @Test
    public void testRemoveNetwork() {
        HostNicVfsConfig vfsConfig = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2);
        checkNetworks(vfsConfig, 1);

        dao.removeNetwork(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2, vfsConfig.getNetworks()
                .iterator()
                .next());

        vfsConfig = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2);
        checkNetworks(vfsConfig, 0);
    }

    @Test
    public void testAddLabel() {
        HostNicVfsConfig vfsConfig = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2);
        checkLabels(vfsConfig, 3);

        String label = "newLbl";
        dao.addLabel(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2, label);

        vfsConfig = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2);
        checkLabels(vfsConfig, 4);
    }

    @Test
    public void testRemoveLabel() {
        HostNicVfsConfig vfsConfig = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2);
        checkLabels(vfsConfig, 3);

        dao.removeLabel(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2, vfsConfig.getNetworkLabels()
                .iterator()
                .next());

        vfsConfig = dao.get(FixturesTool.HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2);
        checkLabels(vfsConfig, 2);
    }

    @Test
    public void testGetAllVfsConfigByHostId() {
        List<HostNicVfsConfig> vfsConfigs = dao.getAllVfsConfigByHostId(FixturesTool.HOST_ID);
        assertEquals(2, vfsConfigs.size());
    }

    @Test
    public void testGetAllVfsConfigByHostIdWithNoVfsConfigs() {
        List<HostNicVfsConfig> vfsConfigs = dao.getAllVfsConfigByHostId(FixturesTool.HOST_WITH_NO_VFS_CONFIGS_ID);
        assertTrue(vfsConfigs.isEmpty());
    }
}
