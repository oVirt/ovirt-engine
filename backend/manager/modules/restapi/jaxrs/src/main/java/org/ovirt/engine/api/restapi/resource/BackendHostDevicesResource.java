package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.model.HostDevices;
import org.ovirt.engine.api.resource.HostDeviceResource;
import org.ovirt.engine.api.resource.HostDevicesResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostDevicesResource
        extends AbstractBackendCollectionResource<HostDevice, org.ovirt.engine.core.common.businessentities.HostDevice>
        implements HostDevicesResource {

    private final Guid hostId;

    protected BackendHostDevicesResource(Guid hostId) {
        super(HostDevice.class, org.ovirt.engine.core.common.businessentities.HostDevice.class);
        this.hostId = hostId;
    }

    public Guid getHostId() {
        return hostId;
    }

    @Override
    protected HostDevice addParents(HostDevice model) {
        model.setHost(new Host());
        model.getHost().setId(hostId.toString());
        return super.addParents(model);
    }

    @Override
    public HostDevices list() {
        HostDevices model = new HostDevices();
        for (org.ovirt.engine.core.common.businessentities.HostDevice hostDevice : getCollection()) {
            model.getHostDevices().add(addLinks(map(hostDevice, new HostDevice())));
        }

        return model;
    }

    @Override
    public HostDeviceResource getDeviceResource(String id) {
        return inject(new BackendHostDeviceResource(id, this));
    }

    protected List<org.ovirt.engine.core.common.businessentities.HostDevice> getCollection() {
        return getBackendCollection(QueryType.GetHostDevicesByHostId, new IdQueryParameters(hostId));
    }
}
