package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.AffinityGroup;

public class AffinityGroupMapperTest extends AbstractInvertibleMappingTest<AffinityGroup, org.ovirt.engine.core.common.scheduling.AffinityGroup, org.ovirt.engine.core.common.scheduling.AffinityGroup> {

    public AffinityGroupMapperTest() {
        super(AffinityGroup.class,
                org.ovirt.engine.core.common.scheduling.AffinityGroup.class,
                org.ovirt.engine.core.common.scheduling.AffinityGroup.class);
    }

    @Override
    protected void verify(AffinityGroup model, AffinityGroup transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
        assertEquals(model.isPositive(), transform.isPositive());
        assertEquals(model.isEnforcing(), transform.isEnforcing());
    }

}
