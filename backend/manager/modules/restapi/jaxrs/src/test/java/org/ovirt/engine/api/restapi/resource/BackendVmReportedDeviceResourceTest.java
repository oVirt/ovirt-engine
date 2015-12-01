package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmReportedDeviceResourceTest
        extends AbstractBackendSubResourceTest<ReportedDevice, VmGuestAgentInterface, BackendVmReportedDeviceResource> {

    private static final Guid VM_ID = GUIDS[1];
    protected static final Guid DEVICE_ID = new Guid("7365646e-6131-302e-3131-2e31322e3133");
    protected static final String ADDRESS = "10.11.12.13";

    public BackendVmReportedDeviceResourceTest() {
        super((BackendVmReportedDeviceResource) getCollection().getReportedDeviceResource(DEVICE_ID.toString()));
    }

    protected static BackendVmReportedDevicesResource getCollection() {
        return new BackendVmReportedDevicesResource(VM_ID);
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected VmGuestAgentInterface getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmGuestAgentInterface.class), index);
    }

    private VmGuestAgentInterface setUpEntityExpectations(VmGuestAgentInterface mock, int index) {
        expect(mock.getInterfaceName()).andReturn(NAMES[index]).anyTimes();
        expect(mock.getMacAddress()).andReturn(ADDRESS).anyTimes();
        return mock;
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVmGuestAgentInterfacesByVmId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { VM_ID },
                    getEntityList());
        }
    }

    protected List<VmGuestAgentInterface> getEntityList() {
        List<VmGuestAgentInterface> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Test
    public void testGet() throws Exception {
        resource.getParent().setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();

        ReportedDevice device = resource.get();
        assertEquals(DEVICE_ID.toString(), device.getId());
        verifyLinks(device);
    }
}
