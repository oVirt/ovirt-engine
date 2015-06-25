package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.api.model.WatchDogs;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.api.resource.WatchdogResource;
import org.ovirt.engine.api.resource.WatchdogsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendWatchdogsResource
        extends AbstractBackendDevicesResource<WatchDog, WatchDogs, VmWatchdog> implements WatchdogsResource {

    private boolean parentIsVm;
    private Guid parentId;

    public BackendWatchdogsResource(
            boolean parentIsVm,
            Guid parentId,
            VdcQueryType queryType,
            VdcQueryParametersBase queryParams) {
        super(
            WatchDog.class,
            WatchDogs.class,
            VmWatchdog.class,
            parentId,
            queryType,
            queryParams,
            VdcActionType.AddWatchdog,
            VdcActionType.UpdateWatchdog
        );
        this.parentIsVm = parentIsVm;
        this.parentId = parentId;
    }

    @Override
    public WatchdogResource getDeviceSubResource(String watchdogId) {
        return inject(
            new BackendWatchdogResource(
                true,
                parentId,
                asGuidOr404(watchdogId),
                this,
                updateType,
                getUpdateParametersProvider(),
                getRequiredUpdateFields()
            )
        );
    }

    @Override
    protected <T> boolean matchEntity(VmWatchdog entity, T id) {
        // There is only one watchdog:
        return true;
    }

    @Override
    protected boolean matchEntity(VmWatchdog entity, String name) {
        // Watchdogs don't have a name:
        return false;
    }

    @Override
    protected String[] getRequiredAddFields() {
        return new String[] { "action", "model" };
    }

    @Override
    protected String[] getRequiredUpdateFields() {
        return new String[] {};
    }

    @Override
    protected VdcActionParametersBase getAddParameters(VmWatchdog entity, WatchDog device) {
        WatchdogParameters watchdogParameters = new WatchdogParameters();
        validateEnums(WatchDog.class, device);
        watchdogParameters.setAction(getMapper(WatchdogAction.class, VmWatchdogAction.class).map(WatchdogAction.fromValue(device.getAction()),
                null));
        watchdogParameters.setModel(getMapper(WatchdogModel.class, VmWatchdogType.class).map(WatchdogModel.fromValue(device.getModel()),
                null));
        watchdogParameters.setId(parentId);
        watchdogParameters.setVm(parentIsVm);
        return watchdogParameters;
    }

    @Override
    protected ParametersProvider<WatchDog, VmWatchdog> getUpdateParametersProvider() {
        return new ParametersProvider<WatchDog, VmWatchdog>() {
            public VdcActionParametersBase getParameters(WatchDog model, VmWatchdog entity) {
                validateEnums(WatchDog.class, model);
                WatchdogParameters params = new WatchdogParameters();
                params.setModel(VmWatchdogType.getByName(model.getModel()));
                params.setAction(VmWatchdogAction.getByName(model.getAction()));
                params.setId(parentId);
                params.setVm(parentIsVm);
                return params;
            }
        };
    }
}
