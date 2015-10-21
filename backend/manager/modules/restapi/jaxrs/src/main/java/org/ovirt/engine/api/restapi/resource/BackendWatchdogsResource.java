package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.api.model.Watchdogs;
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
        extends AbstractBackendDevicesResource<Watchdog, Watchdogs, VmWatchdog>
        implements WatchdogsResource {

    private boolean parentIsVm;
    private Guid parentId;

    public BackendWatchdogsResource(
            boolean parentIsVm,
            Guid parentId,
            VdcQueryType queryType,
            VdcQueryParametersBase queryParams) {
        super(
            Watchdog.class,
            Watchdogs.class,
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
    public WatchdogResource getWatchdogResource(String watchdogId) {
        return inject(
            new BackendWatchdogResource(
                parentIsVm,
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
    protected VdcActionParametersBase getAddParameters(VmWatchdog entity, Watchdog device) {
        WatchdogParameters watchdogParameters = new WatchdogParameters();
        validateEnums(Watchdog.class, device);
        watchdogParameters.setAction(getMapper(WatchdogAction.class, VmWatchdogAction.class).map(WatchdogAction.fromValue(device.getAction()),
                null));
        watchdogParameters.setModel(getMapper(WatchdogModel.class, VmWatchdogType.class).map(WatchdogModel.fromValue(device.getModel()),
                null));
        watchdogParameters.setId(parentId);
        watchdogParameters.setVm(parentIsVm);
        return watchdogParameters;
    }

    @Override
    protected ParametersProvider<Watchdog, VmWatchdog> getUpdateParametersProvider() {
        return new ParametersProvider<Watchdog, VmWatchdog>() {
            public VdcActionParametersBase getParameters(Watchdog model, VmWatchdog entity) {
                validateEnums(Watchdog.class, model);
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
