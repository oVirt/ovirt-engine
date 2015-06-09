package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.verifyModelSpecific;

public class BackendInstanceTypeResourceTest
    extends BackendTemplateBasedResourceTest<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType, BackendInstanceTypeResource> {

    public BackendInstanceTypeResourceTest() {
        super(new BackendInstanceTypeResource(GUIDS[0].toString()));
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetGraphicsExpectations(1);
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(
                VdcActionType.RemoveVmTemplate,
                VmTemplateParametersBase.class,
                new String[] { "VmTemplateId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        Response response = resource.remove();
        verifyRemove(response);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.InstanceType getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmTemplate.class), index);
    }

    @Override
    protected InstanceType getRestModel(int index) {
        return getModel(index);
    }

    @Override
    protected void verifyModel(InstanceType model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model);
    }
}
