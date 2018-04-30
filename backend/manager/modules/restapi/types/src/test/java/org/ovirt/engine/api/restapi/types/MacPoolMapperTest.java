package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.MacPool;

public class MacPoolMapperTest extends AbstractInvertibleMappingTest<MacPool,
        org.ovirt.engine.core.common.businessentities.MacPool,
        org.ovirt.engine.core.common.businessentities.MacPool> {

    public MacPoolMapperTest() {
        super(MacPool.class,
                org.ovirt.engine.core.common.businessentities.MacPool.class,
                org.ovirt.engine.core.common.businessentities.MacPool.class);
    }

    @Override
    protected void verify(MacPool model, MacPool transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.isDefaultPool(), transform.isDefaultPool());
        assertEquals(model.isAllowDuplicates(), transform.isAllowDuplicates());
    }
}
