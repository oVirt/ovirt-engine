package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;

public class SchedulingPolicyMapperTest extends AbstractInvertibleMappingTest<SchedulingPolicy, ClusterPolicy, ClusterPolicy> {

    public SchedulingPolicyMapperTest() {
        super(SchedulingPolicy.class,
                ClusterPolicy.class,
                ClusterPolicy.class);
    }

    @Override
    protected void verify(SchedulingPolicy model, SchedulingPolicy transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());

        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());

        assertEquals(model.isLocked(), transform.isLocked());
        assertEquals(model.isDefaultPolicy(), transform.isDefaultPolicy());
        assertNotNull(model.getProperties());
        assertEquals(CustomPropertiesParser.toMap(model.getProperties()),
                CustomPropertiesParser.toMap(transform.getProperties()));
    }

    @Test
    public void shouldPreferNameBeforePolicy(){
        final Mapper<SchedulingPolicy, ClusterPolicy>
                mapper = getMappingLocator().getMapper(SchedulingPolicy.class, ClusterPolicy.class);
        SchedulingPolicy from = new SchedulingPolicy();
        ClusterPolicy to;
        from.setName("name");
        from.setPolicy("policy");
        to = mapper.map(from, null);
        assertEquals(to.getName(), from.getName());
        from.setName(null);
        to = mapper.map(from, null);
        assertEquals(to.getName(), from.getPolicy());
    }


}
