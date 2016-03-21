package org.ovirt.engine.core.dao.qos;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class CpuQosDaoTest extends BaseDaoTestCase {

    private CpuQosDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = getDbFacade().getCpuQosDao();
    }

    /**
     * Ensures that retrieving with an invalid ID returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        assertNull(dao.get(Guid.newGuid()));
    }

    @Test
    public void getCpuQos() {
        CpuQos cpuQos = new CpuQos();
        cpuQos.setId(FixturesTool.QOS_ID_4);
        cpuQos.setName("qos_d");
        cpuQos.setDescription("desc1");
        cpuQos.setStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        cpuQos.setCpuLimit(50);

        CpuQos fetched = dao.get(FixturesTool.QOS_ID_4);
        assertNotNull(fetched);
        assertEquals(cpuQos, fetched);
    }

    @Test
    public void updateCpuQos() {
        CpuQos cpuQos = dao.get(FixturesTool.QOS_ID_5);
        assertNotNull(cpuQos);
        cpuQos.setName("newB");
        cpuQos.setDescription("desc2");
        cpuQos.setCpuLimit(30);
        assertFalse(cpuQos.equals(dao.get(FixturesTool.QOS_ID_5)));
        dao.update(cpuQos);
        CpuQos fetched = dao.get(FixturesTool.QOS_ID_5);
        assertNotNull(fetched);
        assertEquals(cpuQos, fetched);
    }

    @Test
    public void removeCpuQos() {
        assertNotNull(dao.get(FixturesTool.QOS_ID_6));
        dao.remove(FixturesTool.QOS_ID_6);
        assertNull(dao.get(FixturesTool.QOS_ID_6));
    }

    @Test
    public void saveCpuQos() {
        CpuQos cpuQos = new CpuQos();
        cpuQos.setId(Guid.newGuid());
        assertNull(dao.get(cpuQos.getId()));
        cpuQos.setName("qos_d");
        cpuQos.setDescription("desc3");
        cpuQos.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        cpuQos.setCpuLimit(40);
        dao.save(cpuQos);
        CpuQos fetched = dao.get(cpuQos.getId());
        assertNotNull(fetched);
        assertEquals(cpuQos, fetched);
    }

    @Test
    public void getAllCpuQosForCpuPool() {
        List<CpuQos> allForCpuPoolId = dao.getAllForStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        assertNotNull(allForCpuPoolId);
        assertEquals(3, allForCpuPoolId.size());
    }

    @Test
    public void getQosByVmId() {
        Map<Guid, CpuQos> cpuQosMap = dao.getCpuQosByVmIds(Collections.singleton(FixturesTool.VM_RHEL5_POOL_50));
        assertNotNull(cpuQosMap);
        assertEquals(FixturesTool.QOS_ID_4, cpuQosMap.get(FixturesTool.VM_RHEL5_POOL_50).getId());
    }

    @Test
    public void getNoQosByVmId() {
        Map<Guid, CpuQos> cpuQosMap = dao.getCpuQosByVmIds(Collections.singleton(FixturesTool.VM_RHEL5_POOL_57));
        assertNotNull(cpuQosMap);
        assertNull(cpuQosMap.get(FixturesTool.VM_RHEL5_POOL_57));
    }
}
