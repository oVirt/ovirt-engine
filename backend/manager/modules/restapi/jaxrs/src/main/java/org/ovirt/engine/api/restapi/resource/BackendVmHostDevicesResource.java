package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.model.HostDevices;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmHostDeviceResource;
import org.ovirt.engine.api.resource.VmHostDevicesResource;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmHostDevicesResource
        extends AbstractBackendCollectionResource<HostDevice, HostDeviceView>
        implements VmHostDevicesResource {

    private Guid vmId;

    public BackendVmHostDevicesResource(Guid parentId) {
        super(HostDevice.class, HostDeviceView.class);
        vmId = parentId;
    }

    @Override
    protected HostDevice addParents(HostDevice model) {
        model.setVm(new Vm());
        model.getVm().setId(vmId.toString());
        return super.addParents(model);
    }

    @Override
    public HostDevices list() {
        HostDevices model = new HostDevices();
        for (HostDeviceView hostDevice : getCollection()) {
            model.getHostDevices().add(addLinks(map(hostDevice, new HostDevice())));
        }

        return model;
    }

    @Override
    public Response add(final HostDevice hostDevice) {
        validateParameters(hostDevice, "id|name");

        String deviceName = hostDevice.getName();
        if (hostDevice.isSetId()) {
            // in case both 'name' and 'id' is set, 'id' takes priority
            deviceName = HexUtils.hex2string(hostDevice.getId());
        }

        return performCreate(ActionType.AddVmHostDevices,
                new VmHostDevicesParameters(vmId, deviceName),
                new DeviceNameResolver(deviceName));
    }

    @Override
    public VmHostDeviceResource getDeviceResource(String deviceId) {
        return inject(new BackendVmHostDeviceResource(deviceId, this));
    }

    protected List<HostDeviceView> getCollection() {
        return getBackendCollection(QueryType.GetExtendedVmHostDevicesByVmId, new IdQueryParameters(vmId));
    }

    public Guid getVmId() {
        return vmId;
    }

    private class DeviceNameResolver extends EntityIdResolver<Void> {

        private final String deviceName;

        public DeviceNameResolver(String deviceName) {
            this.deviceName = deviceName;
        }

        @Override
        public HostDeviceView lookupEntity(Void ignored) throws BackendFailureException {
            for (HostDeviceView device : getCollection()) {
                if (device.getDeviceName().equals(deviceName)) {
                    return device;
                }
            }
            return null;
        }
    }
}
