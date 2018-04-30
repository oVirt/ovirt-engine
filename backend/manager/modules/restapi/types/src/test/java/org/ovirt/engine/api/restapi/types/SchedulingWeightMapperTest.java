package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;

public class SchedulingWeightMapperTest extends AbstractInvertibleMappingTest<Weight, ClusterPolicy, ClusterPolicy> {

    public SchedulingWeightMapperTest() {
        super(Weight.class,
                ClusterPolicy.class,
                ClusterPolicy.class);
    }

    private static final String ID = Guid.newGuid().toString();
    private static final int FACTOR = 1;

    @Override
    protected Weight postPopulate(Weight model) {
        model.setId(ID);
        SchedulingPolicyUnit schedulingPolicyUnit = new SchedulingPolicyUnit();
        schedulingPolicyUnit.setId(ID);
        model.setSchedulingPolicyUnit(schedulingPolicyUnit);
        model.setFactor(FACTOR);
        return model;
    }

    @Override
    protected Weight getModel(Weight Weight) {
        Weight = new Weight();
        Weight.setId(ID);
        Weight.setFactor(FACTOR);
        return Weight;
    }

    @Override
    protected void verify(Weight model, Weight transform) {
        assertNotNull(transform);

        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getSchedulingPolicyUnit().getId(), transform.getSchedulingPolicyUnit().getId());
        assertEquals(model.getFactor(), transform.getFactor());
    }

}
