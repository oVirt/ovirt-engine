package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.api.model.WatchDogs;
import org.ovirt.engine.api.resource.WatchdogResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.core.Response;

public class BackendWatchdogResource extends BackendDeviceResource<WatchDog, WatchDogs, VmWatchdog> implements WatchdogResource {

    private boolean parentIsVm;
    private Guid parentId;

    public BackendWatchdogResource(
            boolean parentIsVm,
            Guid parentId,
            Guid watchdogId,
            AbstractBackendReadOnlyDevicesResource<WatchDog, WatchDogs, VmWatchdog> collection,
            VdcActionType updateType,
            org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider<WatchDog, VmWatchdog> updateParametersProvider,
            String[] requiredUpdateFields) {
        super(
            WatchDog.class,
            VmWatchdog.class,
            watchdogId,
            collection,
            updateType,
            updateParametersProvider,
            requiredUpdateFields,
            new String[] {}
        );
        this.parentIsVm = parentIsVm;
        this.parentId = parentId;
    }

    @Override
    public Response remove() {
        get();
        WatchdogParameters parameters = new WatchdogParameters();
        parameters.setId(parentId);
        parameters.setVm(parentIsVm);
        return performAction(VdcActionType.RemoveWatchdog, parameters);
    }
}
