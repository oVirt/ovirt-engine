package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendHostDevicesResourceTest<R extends AbstractBackendResource<HostDevice, D>, D extends org.ovirt.engine.core.common.businessentities.HostDevice>
        extends AbstractBackendResourceTest<HostDevice, D> {

    protected static final Guid VM_ID = GUIDS[0];
    protected static final Guid HOST_ID = GUIDS[1];
    protected static final String DEVICE_NAME = "pci_0000_00_09_0";
    protected static final String PARENT_NAME = "computer";

    protected final R resource;

    protected AbstractBackendHostDevicesResourceTest(R resource) {
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
    protected D getEntity(int index) {
        D hostDevice = createDevice();
        hostDevice.setHostId(HOST_ID);
        hostDevice.setDeviceName(DEVICE_NAME);
        hostDevice.setParentDeviceName(PARENT_NAME);
        hostDevice.setProductName(NAMES[index]);
        hostDevice.setVendorName(NAMES[index + 1]);
        hostDevice.setVmId(VM_ID);

        return hostDevice;
    }

    protected abstract D createDevice();

    protected void setUpGetVmHostDevicesExpectations() {
        setUpEntityQueryExpectations(
                QueryType.GetExtendedVmHostDevicesByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getHostDeviceCollection());
    }

    protected List<D> getHostDeviceCollection() {
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
