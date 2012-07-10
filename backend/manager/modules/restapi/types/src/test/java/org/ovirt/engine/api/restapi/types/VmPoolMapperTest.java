package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.core.common.businessentities.vm_pools;

public class VmPoolMapperTest extends AbstractInvertibleMappingTest<VmPool, vm_pools, vm_pools> {

    public VmPoolMapperTest() {
        super(VmPool.class, vm_pools.class, vm_pools.class);
    }

    @Override
    protected void verify(VmPool model, VmPool transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getSize(), transform.getSize());
        assertNotNull(transform.getCluster());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
    }
}
