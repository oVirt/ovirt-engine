package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;

public class SchedulingFilterMapperTest extends AbstractInvertibleMappingTest<Filter, ClusterPolicy, ClusterPolicy> {

    public SchedulingFilterMapperTest() {
        super(Filter.class,
                ClusterPolicy.class,
                ClusterPolicy.class);
    }

    private static final String ID = Guid.newGuid().toString();

    @Override
    protected Filter postPopulate(Filter model) {
        model.setId(ID);
        SchedulingPolicyUnit schedulingPolicyUnit = new SchedulingPolicyUnit();
        schedulingPolicyUnit.setId(ID);
        model.setSchedulingPolicyUnit(schedulingPolicyUnit);
        model.setPosition(0);
        return model;
    }

    @Override
    protected Filter getModel(Filter filter) {
        filter = new Filter();
        filter.setId(ID);
        return filter;
    }

    @Override
    protected void verify(Filter model, Filter transform) {
        assertNotNull(transform);

        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getSchedulingPolicyUnit().getId(), transform.getSchedulingPolicyUnit().getId());
        assertEquals(model.getPosition(), transform.getPosition());
    }

}
