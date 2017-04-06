package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.VmNicDao;

public abstract class VmTemplateManagementCommand<T extends VmTemplateManagementParameters> extends VmTemplateCommand<T> {

    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private VmDeviceDao vmDeviceDao;

    public VmTemplateManagementCommand(Guid commandId) {
        super(commandId);
    }

    public VmTemplateManagementCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected void removeNetwork() {
        List<VmNic> list = vmNicDao.getAllForTemplate(getVmTemplateId());
        for (VmNic iface : list) {
            vmDeviceDao.remove(new VmDeviceId(iface.getId(), getVmTemplateId()));
            vmNicDao.remove(iface.getId());
        }
    }

    protected void updateWatchdog(Guid templateId) {
        // do not update if this flag is not set
        if (getParameters().isUpdateWatchdog()) {
            VdcQueryReturnValue query =
                    runInternalQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(templateId));
            List<VmWatchdog> watchdogs = query.getReturnValue();
            if (watchdogs.isEmpty()) {
                if (getParameters().getWatchdog() != null) {
                    WatchdogParameters parameters = new WatchdogParameters();
                    parameters.setVm(false);
                    parameters.setClusterIndependent(getVmTemplate().getTemplateType() == VmEntityType.INSTANCE_TYPE || isBlankTemplate());

                    parameters.setId(templateId);
                    parameters.setAction(getParameters().getWatchdog().getAction());
                    parameters.setModel(getParameters().getWatchdog().getModel());
                    runInternalAction(VdcActionType.AddWatchdog, parameters, cloneContextAndDetachFromParent());
                }
            } else {
                WatchdogParameters watchdogParameters = new WatchdogParameters();
                watchdogParameters.setVm(false);
                watchdogParameters.setClusterIndependent(getVmTemplate().getTemplateType() == VmEntityType.INSTANCE_TYPE || isBlankTemplate());

                watchdogParameters.setId(templateId);
                if (getParameters().getWatchdog() == null) {
                    // there is a watchdog in the vm, there should not be any, so let's delete
                    runInternalAction(VdcActionType.RemoveWatchdog, watchdogParameters, cloneContextAndDetachFromParent());
                } else {
                    // there is a watchdog in the vm, we have to update.
                    watchdogParameters.setAction(getParameters().getWatchdog().getAction());
                    watchdogParameters.setModel(getParameters().getWatchdog().getModel());
                    runInternalAction(VdcActionType.UpdateWatchdog, watchdogParameters, cloneContextAndDetachFromParent());
                }
            }
        }
    }

    protected void updateRngDevice(Guid templateId) {
        // do not update if this flag is not set
        if (getParameters().isUpdateRngDevice()) {
            VdcQueryReturnValue query =
                    runInternalQuery(VdcQueryType.GetRngDevice, new IdQueryParameters(templateId));

            List<VmRngDevice> rngDevs = query.getReturnValue();

            if (getParameters().getRngDevice() != null) {
                getParameters().getRngDevice().setVmId(templateId);
            }

            VdcReturnValueBase rngCommandResult = null;
            if (rngDevs.isEmpty()) {
                if (getParameters().getRngDevice() != null) {
                    RngDeviceParameters params = new RngDeviceParameters(getParameters().getRngDevice(), false);
                    rngCommandResult = runInternalAction(VdcActionType.AddRngDevice, params, cloneContextAndDetachFromParent());
                }
            } else {
                if (getParameters().getRngDevice() == null) {
                    RngDeviceParameters params = new RngDeviceParameters(rngDevs.get(0), false);
                    rngCommandResult = runInternalAction(VdcActionType.RemoveRngDevice, params, cloneContextAndDetachFromParent());
                } else {
                    RngDeviceParameters params = new RngDeviceParameters(getParameters().getRngDevice(), false);
                    params.getRngDevice().setDeviceId(rngDevs.get(0).getDeviceId());
                    rngCommandResult = runInternalAction(VdcActionType.UpdateRngDevice, params, cloneContextAndDetachFromParent());
                }
            }

            if (rngCommandResult != null && !rngCommandResult.getSucceeded()) {
                getReturnValue().setSucceeded(false);
            }
        }
    }

}
