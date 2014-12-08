package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;

public abstract class AbstractBackendHostDevicesResourceTest<C extends AbstractBackendResource<HostDevice, org.ovirt.engine.core.common.businessentities.HostDevice>>
        extends AbstractBackendResourceTest<HostDevice, org.ovirt.engine.core.common.businessentities.HostDevice> {

    protected static final Guid VM_ID = GUIDS[0];
    protected static final Guid HOST_ID = GUIDS[1];
    protected static final String DEVICE_NAME = "pci_0000_00_09_0";
    protected static final String PARENT_NAME = "computer";

    protected final C resource;

    protected AbstractBackendHostDevicesResourceTest(C resource) {
        this.resource = resource;
    }

    @Override
    protected void init() {
        initResource(resource);
    }

    protected void setUriInfo(UriInfo uriInfo) {
        resource.setUriInfo(uriInfo);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.HostDevice getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.HostDevice hostDevice = new org.ovirt.engine.core.common.businessentities.HostDevice();
        hostDevice.setHostId(HOST_ID);
        hostDevice.setDeviceName(DEVICE_NAME);
        hostDevice.setParentDeviceName(PARENT_NAME);
        hostDevice.setProductName(NAMES[index]);
        hostDevice.setVendorName(NAMES[index + 1]);
        hostDevice.setVmId(VM_ID);

        return hostDevice;
    }

    protected void setUpGetVmHostDevicesExpectations() {
        setUpEntityQueryExpectations(
                VdcQueryType.GetExtendedVmHostDevicesByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getHostDeviceCollection());
    }

    protected List<org.ovirt.engine.core.common.businessentities.HostDevice> getHostDeviceCollection() {
        return Collections.singletonList(getEntity(0));
    }

    protected static void verifyHostDevices(List<HostDevice> devices) {
        assertEquals(1, devices.size());

        verifyHostDevice(devices.get(0));
    }

    protected static void verifyHostDevice(HostDevice device) {
        assertEquals(VM_ID.toString(), device.getVm().getId());
        assertEquals(HOST_ID.toString(), device.getHost().getId());
        assertEquals(DEVICE_NAME, device.getName());
        assertEquals(DEVICE_NAME, HexUtils.hex2string(device.getId()));
        assertEquals(PARENT_NAME, HexUtils.hex2string(device.getParentDevice().getId()));
        assertEquals(NAMES[0], device.getProduct().getName());
        assertEquals(NAMES[1], device.getVendor().getName());
    }
}
