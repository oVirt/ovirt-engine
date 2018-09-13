package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class PolicyUnitMapperTest extends AbstractInvertibleMappingTest<SchedulingPolicyUnit, PolicyUnit, PolicyUnit> {

    public PolicyUnitMapperTest() {
        super(SchedulingPolicyUnit.class,
                PolicyUnit.class,
                PolicyUnit.class);
    }

    @Override
    protected void verify(SchedulingPolicyUnit model, SchedulingPolicyUnit transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getType(), transform.getType());
        assertEquals(model.isEnabled(), transform.isEnabled());
        assertNotNull(model.getProperties());
        assertEquals(CustomPropertiesParser.toMap(model.getProperties()),
                CustomPropertiesParser.toMap(transform.getProperties()));
    }

    @Override
    protected SchedulingPolicyUnit postPopulate(SchedulingPolicyUnit model) {
        model = super.postPopulate(model);
        model.setType(org.ovirt.engine.api.model.PolicyUnitType.FILTER);
        return model;
    }

}
