package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.FencingPolicy;

public class FencingPolicyMapperTest extends AbstractInvertibleMappingTest<FencingPolicy, org.ovirt.engine.core.common.businessentities.FencingPolicy, org.ovirt.engine.core.common.businessentities.FencingPolicy>{

    public FencingPolicyMapperTest() {
        super(FencingPolicy.class, org.ovirt.engine.core.common.businessentities.FencingPolicy.class, org.ovirt.engine.core.common.businessentities.FencingPolicy.class);
    }

    @Override
    protected void verify(FencingPolicy model, FencingPolicy transform) {
        assertNotNull(transform);
        assertEquals(model.isEnabled(), transform.isEnabled());
        assertEquals(model.getSkipIfSdActive().isEnabled(), transform.getSkipIfSdActive().isEnabled());
        assertEquals(model.getSkipIfConnectivityBroken().isEnabled(), transform.getSkipIfConnectivityBroken().isEnabled());
        assertEquals(model.getSkipIfConnectivityBroken().getThreshold(), transform.getSkipIfConnectivityBroken().getThreshold());
    }
}
