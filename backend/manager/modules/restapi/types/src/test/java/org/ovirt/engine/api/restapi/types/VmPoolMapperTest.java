package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.VmPool;

public class VmPoolMapperTest extends AbstractInvertibleMappingTest<VmPool, org.ovirt.engine.core.common.businessentities.VmPool, org.ovirt.engine.core.common.businessentities.VmPool> {

    public VmPoolMapperTest() {
        super(VmPool.class,
                org.ovirt.engine.core.common.businessentities.VmPool.class,
                org.ovirt.engine.core.common.businessentities.VmPool.class);
    }

    @Override
    protected void verify(VmPool model, VmPool transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getComment(), transform.getComment());
        assertEquals(model.getSize(), transform.getSize());
        assertNotNull(transform.getCluster());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
        assertEquals(model.getMaxUserVms(), transform.getMaxUserVms());
        assertEquals(model.getDisplay().getProxy(), transform.getDisplay().getProxy());
        assertEquals(model.isStateful(), transform.isStateful());
    }
}
