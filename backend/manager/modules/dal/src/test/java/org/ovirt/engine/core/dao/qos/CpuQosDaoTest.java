package org.ovirt.engine.core.dao.qos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class CpuQosDaoTest extends BaseGenericDaoTestCase<Guid, CpuQos, CpuQosDao> {

    @Override
    protected CpuQos generateNewEntity() {
        CpuQos cpuQos = new CpuQos();
        cpuQos.setId(Guid.newGuid());
        cpuQos.setName("qos_d");
        cpuQos.setDescription("desc3");
        cpuQos.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        cpuQos.setCpuLimit(40);
        return cpuQos;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setName("newB");
        existingEntity.setDescription("desc2");
        existingEntity.setCpuLimit(30);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.QOS_ID_4;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 3;
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
        Map<Guid, CpuQos> cpuQosMap = dao.getCpuQosByVmIds(Collections.singleton(FixturesTool.VM_RHEL5_POOL_59));
        assertNotNull(cpuQosMap);
        assertNull(cpuQosMap.get(FixturesTool.VM_RHEL5_POOL_57));
    }
}
