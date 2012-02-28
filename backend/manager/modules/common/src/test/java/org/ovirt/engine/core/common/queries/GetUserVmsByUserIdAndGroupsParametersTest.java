package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GetUserVmsByUserIdAndGroupsParametersTest extends AbstractVdcUserQueryParametersBaseTestCase<GetUserVmsByUserIdAndGroupsParameters> {

    @SuppressWarnings("deprecation")
    @Override
    @Test
    public void testParameterizedConstructor() throws Exception {
        super.testParameterizedConstructor();
        assertEquals("Deprecated methods return wrong value", getParamObject().getUserId(), getParamObject().getId());
    }

}
