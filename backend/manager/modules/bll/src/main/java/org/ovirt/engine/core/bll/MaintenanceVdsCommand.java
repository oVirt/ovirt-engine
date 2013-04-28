package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.InternalMigrateVmParameters;
import org.ovirt.engine.core.common.action.MaintenanceVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparer;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

@NonTransactiveCommandAttribute
public class MaintenanceVdsCommand<T extends MaintenanceVdsParameters> extends VdsCommand<T> {

    private static final long serialVersionUID = -7604781532599945079L;
    private final boolean _isInternal;
    private List<VM> vms;

    public MaintenanceVdsCommand(T parameters) {
        super(parameters);
        _isInternal = parameters.getIsInternal();
    }

    @Override
    protected void executeCommand() {
        if (getVds().getStatus() == VDSStatus.Maintenance) {
            // nothing to do
            setSucceeded(true);
        } else {
            orderListOfRunningVmsOnVds(getVdsId());
            setSucceeded(MigrateAllVms(getExecutionContext()));

            // if non responsive move directly to maintenance
            if (getVds().getStatus() == VDSStatus.NonResponsive
                    || getVds().getStatus() == VDSStatus.Connecting
                    || getVds().getStatus() == VDSStatus.Down) {
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.SetVdsStatus,
                                new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Maintenance));
            }
        }
        // if there's VM(s) in this VDS which is migrating, mark this command as async
        // as the migration(s) is a step of this job, so this job must not be cleaned yet
        if (isVmsExist()) {
            ExecutionHandler.setAsyncJob(getExecutionContext(), true);
        }
    }

    protected boolean isVmsExist() {
        return vms != null && !vms.isEmpty();
    }

    protected void orderListOfRunningVmsOnVds(Guid vdsId) {
        vms = DbFacade.getInstance().getVmDao().getAllRunningForVds(vdsId);
        Collections.sort(vms, Collections.reverseOrder(new VmsComparer()));
    }

    /**
     * Note: you must call {@link #orderListOfRunningVmsOnVds(Guid)} before calling this method
     */
    protected boolean MigrateAllVms(ExecutionContext parentContext) {
        return MigrateAllVms(parentContext, false);
    }

    /**
     * Note: you must call {@link #orderListOfRunningVmsOnVds(Guid)} before calling this method
     */
    protected boolean MigrateAllVms(ExecutionContext parentContext, boolean HAOnly) {

        boolean succeeded = true;

        for (VM vm : vms) {
            // if HAOnly is true check that vm is HA (auto_startup should be true)
            if (vm.getStatus() != VMStatus.MigratingFrom && (!HAOnly || (HAOnly && vm.isAutoStartup()))) {
                VdcReturnValueBase result =
                        Backend.getInstance().runInternalAction(VdcActionType.InternalMigrateVm,
                                new InternalMigrateVmParameters(vm.getId(), getActionType()),
                                createMigrateVmContext(parentContext, vm));
                if (!result.getCanDoAction() || !(((Boolean) result.getActionReturnValue()).booleanValue())) {
                    succeeded = false;
                    appendCustomValue("failedVms", vm.getName(), ",");
                    log.errorFormat("ResourceManager::vdsMaintenance - Failed migrating desktop '{0}'", vm.getName());
                }
            }
        }
        return succeeded;
    }

    protected CommandContext createMigrateVmContext(ExecutionContext parentContext,VM vm) {
        ExecutionContext ctx = new ExecutionContext();
        try {
            Map<String, String> values = new HashMap<String, String>();
            values.put(VdcObjectType.VM.name().toLowerCase(), vm.getName());
            values.put(VdcObjectType.VDS.name().toLowerCase(), vm.getRunOnVdsName());
            Step step = ExecutionHandler.addSubStep(getExecutionContext(),
                    parentContext.getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.MIGRATE_VM,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.MIGRATE_VM, values));
            ctx.setStep(step);
            ctx.setMonitored(true);
        } catch (RuntimeException e) {
            log.error("Failed to create ExecutionContext for MigrateVmCommand", e);
        }
        return new CommandContext(ctx);
    }

    @Override
    protected boolean canDoAction() {
        return CanMaintenanceVds(getVdsId(), getReturnValue().getCanDoActionMessages());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (_isInternal) {
            if (getSucceeded()) {
                return AuditLogType.VDS_MAINTENANCE;
            } else {
                return AuditLogType.VDS_MAINTENANCE_FAILED;
            }
        } else {
            if (getSucceeded()) {
                return AuditLogType.USER_VDS_MAINTENANCE;
            } else {
                return AuditLogType.USER_VDS_MAINTENANCE_MIGRATION_FAILED;
            }
        }
    }

    public boolean CanMaintenanceVds(Guid vdsId, java.util.ArrayList<String> reasons) {
        boolean returnValue = true;
        // VDS vds = ResourceManager.Instance.getVds(vdsId);
        VDS vds = DbFacade.getInstance().getVdsDao().get(vdsId);
        // we can get here when vds status was set already to Maintenance
        if ((vds.getStatus() != VDSStatus.Maintenance) && (vds.getStatus() != VDSStatus.NonResponsive)
                && (vds.getStatus() != VDSStatus.Up) && (vds.getStatus() != VDSStatus.Error)
                && (vds.getStatus() != VDSStatus.PreparingForMaintenance) && (vds.getStatus() != VDSStatus.Down)) {
            returnValue = false;
            reasons.add(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_OPERATIONAL.toString());
        }

        orderListOfRunningVmsOnVds(vdsId);

        for (VM vm : vms) {
            if (vm.getMigrationSupport() != MigrationSupport.MIGRATABLE) {
                reasons.add(VdcBllMessages.VDS_CANNOT_MAINTENANCE_IT_INCLUDES_NON_MIGRATABLE_VM.toString());
                return false;
            }
        }

        return returnValue;
    }

    public static void ProcessStorageOnVdsInactive(final VDS vds) {

        // Clear the problematic timers since the VDS is in maintenance so it doesn't make sense to check it
        // anymore.
        if (!Guid.Empty.equals(vds.getStoragePoolId())) {
            clearDomainCache(vds);

            StoragePool storage_pool = DbFacade.getInstance()
                    .getStoragePoolDao()
                    .get(vds.getStoragePoolId());
            if (StoragePoolStatus.Uninitialized != storage_pool
                    .getstatus()) {
                Backend.getInstance().getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.DisconnectStoragePool,
                                new DisconnectStoragePoolVDSCommandParameters(vds.getId(),
                                        vds.getStoragePoolId(), vds.getVdsSpmId()));
                HostStoragePoolParametersBase params =
                        new HostStoragePoolParametersBase(storage_pool, vds);
                Backend.getInstance().runInternalAction(VdcActionType.DisconnectHostFromStoragePoolServers, params);
            }
        }
    }

    /**
     * The following method will clear a cache for problematic domains, which were reported by vds
     * @param vds
     */
    private static void clearDomainCache(final VDS vds) {
        ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(vds.getStoragePoolId(),
                null, vds.getId(), EventType.VDSCLEARCACHE),
                new Callable<EventResult>() {
                    @Override
                    public EventResult call() {
                        IrsBrokerCommand.clearVdsFromCache(vds.getStoragePoolId(), vds.getId(), vds.getName());
                        return new EventResult(true, EventType.VDSCLEARCACHE);
                    }
                });
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = Collections.singletonMap(VdcObjectType.VDS.name().toLowerCase(), getVdsName());
        }

        return jobProperties;
    }

}
