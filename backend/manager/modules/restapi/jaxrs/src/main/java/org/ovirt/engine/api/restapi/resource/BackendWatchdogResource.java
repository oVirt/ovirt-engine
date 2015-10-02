package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.Watchdogs;
import org.ovirt.engine.api.resource.WatchdogResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.compat.Guid;

public class BackendWatchdogResource extends BackendDeviceResource<Watchdog, Watchdogs, VmWatchdog> implements WatchdogResource {

    private boolean parentIsVm;
    private Guid parentId;

    public BackendWatchdogResource(
            boolean parentIsVm,
            Guid parentId,
            Guid watchdogId,
            AbstractBackendReadOnlyDevicesResource<Watchdog, Watchdogs, VmWatchdog> collection,
            VdcActionType updateType,
            org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider<Watchdog, VmWatchdog> updateParametersProvider,
            String[] requiredUpdateFields) {
        super(
            Watchdog.class,
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
