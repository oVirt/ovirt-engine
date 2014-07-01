package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;

public class VdsDynamicDAOTest extends BaseDAOTestCase {
    private VdsDynamicDAO dao;
    private VdsStaticDAO staticDao;
    private VdsStatisticsDAO statisticsDao;
    private VdsStatic existingVds;
    private VdsStatic newStaticVds;
    private VdsDynamic newDynamicVds;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVdsDynamicDao();
        staticDao =  dbFacade.getVdsStaticDao();
        statisticsDao =  dbFacade.getVdsStatisticsDao();
        existingVds = staticDao.get(FixturesTool.VDS_GLUSTER_SERVER2);

        newStaticVds = new VdsStatic();
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setVdsGroupId(existingVds.getVdsGroupId());
        newStaticVds.setProtocol(VdsProtocol.STOMP);
        newDynamicVds = new VdsDynamic();
    }

    /**
     * Ensures that an invalid id returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VdsDynamic result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the right object is returned.
     */
    @Test
    public void testGet() {
        VdsDynamic result = dao.get(existingVds.getId());

        assertNotNull(result);
        assertEquals(existingVds.getId(), result.getId());
    }

    /**
     * Ensures saving a VDS instance works.
     */
    @Test
    public void testSave() {
        staticDao.save(newStaticVds);
        newDynamicVds.setId(newStaticVds.getId());
        dao.save(newDynamicVds);

        VdsStatic staticResult = staticDao.get(newStaticVds.getId());
        VdsDynamic dynamicResult = dao.get(newDynamicVds.getId());

        assertNotNull(staticResult);
        assertEquals(newStaticVds, staticResult);
        assertNotNull(dynamicResult);
        assertEquals(newDynamicVds, dynamicResult);
    }

    /**
     * Ensures removing a VDS instance works.
     */
    @Test
    public void testRemove() {
        dao.remove(existingVds.getId());
        statisticsDao.remove(existingVds.getId());
        staticDao.remove(existingVds.getId());

        VdsStatic resultStatic = staticDao.get(existingVds.getId());
        assertNull(resultStatic);
        VdsDynamic resultDynamic = dao.get(existingVds.getId());
        assertNull(resultDynamic);
    }

    @Test
    public void testUpdateStatus() {
        VdsDynamic before = dao.get(existingVds.getId());
        before.setStatus(VDSStatus.Down);
        dao.updateStatus(before.getId(), before.getStatus());
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before, after);
    }

    @Test
    public void testUpdateNetConfigDirty() {
        VdsDynamic before = dao.get(existingVds.getId());
        Boolean netConfigDirty = before.getnet_config_dirty();
        netConfigDirty = Boolean.FALSE.equals(netConfigDirty);
        before.setnet_config_dirty(netConfigDirty);
        dao.updateNetConfigDirty(before.getId(), netConfigDirty);
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before, after);
    }

    @Test
    public void testGlusterVersion() {
        RpmVersion glusterVersion = new RpmVersion("glusterfs-3.4.0.34.1u2rhs-1.el6rhs");
        VdsDynamic before = dao.get(existingVds.getId());
        before.setGlusterVersion(glusterVersion);
        dao.update(before);
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(glusterVersion, after.getGlusterVersion());
    }

    @Test
    public void testSmartUpdatePartialVds() {
        int vmCount = 1;
        int pendingVcpusCount = 5;
        int pendingVmemSize = 25;
        int memCommited = 50;
        int vmsCoresCount = 15;
        VdsDynamic before = dao.get(existingVds.getId());
        before.setvm_count(before.getvm_count() + vmCount);
        before.setpending_vcpus_count(before.getpending_vcpus_count() + pendingVcpusCount);
        before.setpending_vmem_size(before.getpending_vmem_size() + pendingVmemSize);
        before.setmem_commited(before.getmem_commited() + memCommited + before.getguest_overhead());
        before.setvms_cores_count(before.getvms_cores_count() + vmsCoresCount);
        dao.updatePartialVdsDynamicCalc(before.getId(),
                vmCount,
                pendingVcpusCount,
                pendingVmemSize,
                memCommited,
                vmsCoresCount);
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before, after);

        vmCount = before.getvm_count() + 1;
        before.setvm_count(0);
        before.setmem_commited(before.getmem_commited() - memCommited - before.getguest_overhead());
        dao.updatePartialVdsDynamicCalc(before.getId(), -vmCount, 0, 0, -memCommited, 0);
        after = dao.get(existingVds.getId());
        assertEquals(before, after);
    }
}
