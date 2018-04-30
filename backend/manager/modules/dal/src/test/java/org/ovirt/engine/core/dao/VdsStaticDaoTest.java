package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

public class VdsStaticDaoTest extends BaseGenericDaoTestCase<Guid, VdsStatic, VdsStaticDao> {
    @Override
    protected VdsStatic generateNewEntity() {
        VdsStatic newStaticVds = new VdsStatic();
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setSshPort(22);
        newStaticVds.setSshUsername("root");
        newStaticVds.setClusterId(existingEntity.getClusterId());
        newStaticVds.setSshKeyFingerprint("b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31");
        newStaticVds.setCurrentKernelCmdline("a=b");
        newStaticVds.setLastStoredKernelCmdline("c=d");
        newStaticVds.setKernelCmdlineIommu(true);
        return newStaticVds;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setComment("new comment");
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VDS_JUST_STATIC_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 6;
    }

    @Disabled
    @Override
    public void testGetAll() {
        // Not supported
    }

    /**
     * Ensures all the right VdsStatic instances are returned.
     */
    @Test
    public void testGetByHostName() {
        VdsStatic vds = dao.getByHostName(existingEntity.getHostName());

        assertNotNull(vds);
        assertEquals(existingEntity.getHostName(), vds.getHostName());
    }

    /**
     * Ensures all the right set of VdsStatic instances are returned.
     */
    @Test
    public void testGetAllForCluster() {
        List<VdsStatic> result = dao.getAllForCluster(existingEntity.getClusterId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VdsStatic vds : result) {
            assertEquals(existingEntity.getClusterId(), vds.getClusterId());
        }
    }

    @Test
    public void testIfExistsHostThatMissesNetworkInClusterRightNetwork() {
        assertFalse(dao.checkIfExistsHostThatMissesNetworkInCluster(
                FixturesTool.CLUSTER,
                "engine",
                VDSStatus.Up));
    }

    @Test
    public void testIfExistsHostThatMissesNetworkInClusterWrongNetwork() {
        final boolean resultBeforeStatusUpdate = dao.checkIfExistsHostThatMissesNetworkInCluster(
                FixturesTool.CLUSTER,
                "no such network",
                VDSStatus.Up);
        assertTrue(resultBeforeStatusUpdate);
    }

    @Test
    public void testUpdateVdsReinstallRequired() {
        dao.updateReinstallRequired(getExistingEntityId(), true);
        assertTrue(dao.get(getExistingEntityId()).isReinstallRequired());
    }
}
