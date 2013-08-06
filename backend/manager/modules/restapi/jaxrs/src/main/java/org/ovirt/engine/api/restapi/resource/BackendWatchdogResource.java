package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.api.model.WatchDogs;
import org.ovirt.engine.api.resource.WatchdogResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.compat.Guid;

public class BackendWatchdogResource extends BackendDeviceResource<WatchDog, WatchDogs, VmWatchdog> implements WatchdogResource {

    public BackendWatchdogResource(
            Guid guid,
            AbstractBackendReadOnlyDevicesResource<WatchDog, WatchDogs, VmWatchdog> collection,
            VdcActionType updateType,
            org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider<WatchDog, VmWatchdog> updateParametersProvider,
            String[] requiredUpdateFields) {
        super(WatchDog.class,
                VmWatchdog.class,
                guid,
                collection,
                updateType,
                updateParametersProvider,
                requiredUpdateFields,
                new String[] {});
    }

}
