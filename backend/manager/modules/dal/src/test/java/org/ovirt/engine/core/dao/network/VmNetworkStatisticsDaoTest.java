package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.FixturesTool;

public class VmNetworkStatisticsDaoTest extends NetworkStatisticsDaoTest<VmNetworkStatisticsDao, VmNetworkStatistics> {
    private static final Guid NEW_INTERFACE_ID = new Guid("14550e82-1e1f-47b5-ae41-b009348dabfa");
    private static final Guid VM_ID = FixturesTool.VM_RHEL5_POOL_57;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    private VmNetworkStatistics newVmStatistics;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        newVmStatistics = new VmNetworkStatistics();
        newVmStatistics.setId(NEW_INTERFACE_ID);
        newVmStatistics.setVmId(VM_ID);
        newVmStatistics.setStatus(InterfaceStatus.DOWN);
        newVmStatistics.setSampleTime(0.0);
        newVmStatistics.setReceiveDrops(BigInteger.ZERO);
        newVmStatistics.setReceiveRate(0.0);
        newVmStatistics.setReceivedBytes(BigInteger.ZERO);
        newVmStatistics.setReceivedBytesOffset(BigInteger.ZERO);
        newVmStatistics.setTransmitDrops(BigInteger.ZERO);
        newVmStatistics.setTransmitRate(0.0);
        newVmStatistics.setTransmittedBytes(BigInteger.ZERO);
        newVmStatistics.setTransmittedBytesOffset(BigInteger.ZERO);
    }

    /**
     * Ensures null is returned.
     */
    @Test
    public void testGetWithNonExistingId() {
        VmNetworkStatistics result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the network interface statistics entity is returned.
     */
    @Test
    public void testGet() {
        VmNetworkStatistics result = dao.get(FixturesTool.VM_NETWORK_INTERFACE);

        assertNotNull(result);
        assertEquals(FixturesTool.VM_NETWORK_INTERFACE, result.getId());
    }

    /**
     * Ensures that saving an interface for a VM works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newVmStatistics);

        VmNetworkStatistics savedStatistics = dao.get(NEW_INTERFACE_ID);

        assertNotNull(savedStatistics);
        assertEquals(newVmStatistics.getStatus(), savedStatistics.getStatus());
    }

    @Override
    protected List<VmNetworkInterface> getAllInterfaces() {
        return vmNetworkInterfaceDao.getAllForVm(VM_ID);
    }

    @Override
    protected void updateStatistics(VmNetworkStatistics stats) {
        dao.update(stats);
    }

    @Test
    public void testUpdateWithValues() {
        testUpdateStatistics(999.0, new BigInteger("999"));
    }

    @Test
    public void testUpdateNullValues() {
        testUpdateStatistics(null, null);
    }

    /**
     * Ensures that the specified VM's interfaces are deleted.
     */
    @Test
    public void testRemove() {
        assertNotNull(dao.get(FixturesTool.VM_NETWORK_INTERFACE));

        dao.remove(FixturesTool.VM_NETWORK_INTERFACE);

        assertNull(dao.get(FixturesTool.VM_NETWORK_INTERFACE));
    }

    @Test
    public void testGetAll() {
        assertThrows(UnsupportedOperationException.class, () -> dao.getAll());
    }

    @Test
    public void testUpdateAll() {
        VmNetworkStatistics existingStats = dao.get(FixturesTool.VM_NETWORK_INTERFACE);
        VmNetworkStatistics existingStats2 = dao.get(new Guid("e2817b12-f873-4046-b0da-0098293c0000"));
        existingStats.setReceiveDrops(BigInteger.TEN);
        existingStats2.setStatus(InterfaceStatus.DOWN);

        dao.updateAll(Arrays.asList(existingStats, existingStats2));

        assertEquals(existingStats.getReceiveDrops(), dao.get(existingStats.getId()).getReceiveDrops());
        assertEquals(existingStats2.getStatus(), dao.get(existingStats2.getId()).getStatus());
    }
}
