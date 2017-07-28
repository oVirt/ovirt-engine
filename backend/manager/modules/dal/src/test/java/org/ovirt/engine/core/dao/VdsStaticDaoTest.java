package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

public class VdsStaticDaoTest extends BaseDaoTestCase {
    private static final Guid VDS_JUST_STATIC_ID = new Guid("09617c59-cd31-4878-9c23-5ac17d8e1e3a");

    private VdsStaticDao dao;
    private VdsStatic existingVds;
    private VdsStatic newStaticVds;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getVdsStaticDao();
        existingVds = dao.get(VDS_JUST_STATIC_ID);
        newStaticVds = new VdsStatic();
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setSshPort(22);
        newStaticVds.setSshUsername("root");
        newStaticVds.setClusterId(existingVds.getClusterId());
        newStaticVds.setSshKeyFingerprint("b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31");
        newStaticVds.setCurrentKernelCmdline("a=b");
        newStaticVds.setLastStoredKernelCmdline("c=d");
        newStaticVds.setKernelCmdlineIommu(true);
    }

    /**
     * Ensures that an invalid id returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VdsStatic result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the right object is returned.
     */
    @Test
    public void testGet() {
        VdsStatic result = dao.get(existingVds.getId());

        assertNotNull(result);
        assertEquals(existingVds.getId(), result.getId());
    }

    /**
     * Ensures all the right VdsStatic instances are returned.
     */
    @Test
    public void testGetByHostName() {
        VdsStatic vds = dao.getByHostName(existingVds
                .getHostName());

        assertNotNull(vds);
        assertEquals(existingVds.getHostName(), vds.getHostName());
    }

    /**
     * Ensures all the right set of VdsStatic instances are returned.
     */
    @Test
    public void testGetAllForCluster() {
        List<VdsStatic> result = dao.getAllForCluster(existingVds
                .getClusterId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VdsStatic vds : result) {
            assertEquals(existingVds.getClusterId(), vds.getClusterId());
        }
    }

    /**
     * Ensures saving a VDS instance works.
     */
    @Test
    public void testSave() {
        dao.save(newStaticVds);

        VdsStatic staticResult = dao.get(newStaticVds.getId());

        assertNotNull(staticResult);
        assertEquals(newStaticVds, staticResult);
    }

    /**
     * Ensures removing a VDS instance works.
     */
    @Test
    public void testRemove() {
        dao.remove(existingVds.getId());

        VdsStatic resultStatic = dao.get(existingVds.getId());
        assertNull(resultStatic);
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
}
