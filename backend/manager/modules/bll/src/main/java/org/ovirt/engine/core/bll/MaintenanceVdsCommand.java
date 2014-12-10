package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.InternalMigrateVmParameters;
import org.ovirt.engine.core.common.action.MaintenanceVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparer;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;

@NonTransactiveCommandAttribute
public class MaintenanceVdsCommand<T extends MaintenanceVdsParameters> extends VdsCommand<T> {

    private final boolean _isInternal;
    private List<VM> vms;
    private boolean haMaintenanceFailed;

    public MaintenanceVdsCommand(T parameters) {
        this(parameters, null);
    }

    public MaintenanceVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        _isInternal = parameters.getIsInternal();
        haMaintenanceFailed = false;
    }

    @Override
    protected void executeCommand() {
        if (getVds().getStatus() == VDSStatus.Maintenance) {
            // nothing to do
            setSucceeded(true);
        } else {
            orderListOfRunningVmsOnVds(getVdsId());

            if (getVds().getHighlyAvailableIsConfigured()) {
                SetHaMaintenanceModeVDSCommandParameters params
                        = new SetHaMaintenanceModeVDSCommandParameters(getVds(), HaMaintenanceMode.LOCAL, true);
                if (!runVdsCommand(VDSCommandType.SetHaMaintenanceMode, params).getSucceeded()) {
                    haMaintenanceFailed = true;
                    // HA maintenance failure is fatal only if the Hosted Engine vm is running on this host
                    if (isHostedEngineOnVds()) {
                        setSucceeded(false);
                        return;
                    }
                }
            }

            setSucceeded(migrateAllVms(getExecutionContext()));

            // if non responsive move directly to maintenance
            if (getVds().getStatus() == VDSStatus.NonResponsive
                    || getVds().getStatus() == VDSStatus.Connecting
                    || getVds().getStatus() == VDSStatus.Down) {
                runVdsCommand(VDSCommandType.SetVdsStatus,
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
    protected boolean migrateAllVms(ExecutionContext parentContext) {
        return migrateAllVms(parentContext, false);
    }

    private boolean canScheduleVm(VM vm) {
        List<Guid> blacklist = new ArrayList<Guid>();
        if (getVdsId() != null) {
            blacklist.add(getVdsId());
        }
        return SchedulingManager.getInstance().canSchedule(
                        getVdsGroup(),
                        vm,
                        blacklist, //blacklist only contains the host we're putting to maintenance
                        Collections.<Guid> emptyList(), //no whitelist
                        vm.getDedicatedVmForVds(),
                        new ArrayList<String>()
                        );
    }
    /**
     * Note: you must call {@link #orderListOfRunningVmsOnVds(Guid)} before calling this method
     */
    protected boolean migrateAllVms(ExecutionContext parentContext, boolean HAOnly) {

        boolean succeeded = true;

        for (VM vm : vms) {
            if (vm.isHostedEngine()) {
                // check if there is host which can be used for HE
                if (!canScheduleVm(vm)) {
                    succeeded = false;
                    appendCustomValue("failedVms", vm.getName(), ",");
                    log.error("ResourceManager::vdsMaintenance - There is not host capable of running the hosted engine VM");
                }
                // The Hosted Engine vm is migrated by the HA agent
                continue;
            }
            // if HAOnly is true check that vm is HA (auto_startup should be true)
            if (vm.getStatus() != VMStatus.MigratingFrom && (!HAOnly || (HAOnly && vm.isAutoStartup()))) {
                VdcReturnValueBase result =
                        runInternalAction(VdcActionType.InternalMigrateVm,
                                new InternalMigrateVmParameters(vm.getId(), getActionType()),
                                createMigrateVmContext(parentContext, vm));
                if (!result.getCanDoAction() || !(((Boolean) result.getActionReturnValue()).booleanValue())) {
                    succeeded = false;
                    appendCustomValue("failedVms", vm.getName(), ",");
                    log.error("ResourceManager::vdsMaintenance - Failed migrating desktop '{}'", vm.getName());
                }
            }
        }
        return succeeded;
    }

    protected CommandContext createMigrateVmContext(ExecutionContext parentContext, VM vm) {
        ExecutionContext ctx = new ExecutionContext();
        try {
            Map<String, String> values = new HashMap<String, String>();
            values.put(VdcObjectType.VM.name().toLowerCase(), vm.getName());
            values.put(VdcObjectType.VDS.name().toLowerCase(), vm.getRunOnVdsName());
            Step step = ExecutionHandler.addSubStep(getExecutionContext(),
                    parentContext.getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.MIGRATE_VM,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.MIGRATE_VM, values));
            ctx.setJob(parentContext.getJob());
            ctx.setStep(step);
            ctx.setMonitored(true);
        } catch (RuntimeException e) {
            log.error("Failed to create ExecutionContext for MigrateVmCommand", e);
        }
        return cloneContextAndDetachFromParent().withExecutionContext(ctx);
    }

    @Override
    protected boolean canDoAction() {
        return canMaintenanceVds(getVdsId(), getReturnValue().getCanDoActionMessages());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (_isInternal) {
            if (getSucceeded() && !haMaintenanceFailed) {
                return AuditLogType.VDS_MAINTENANCE;
            } else if (getSucceeded()) {
                return AuditLogType.VDS_MAINTENANCE_MANUAL_HA;
            } else {
                return AuditLogType.VDS_MAINTENANCE_FAILED;
            }
        } else {
            if (getSucceeded() && !haMaintenanceFailed) {
                return AuditLogType.USER_VDS_MAINTENANCE;
            } else if (getSucceeded()) {
                return AuditLogType.USER_VDS_MAINTENANCE_MANUAL_HA;
            } else {
                return AuditLogType.USER_VDS_MAINTENANCE_MIGRATION_FAILED;
            }
        }
    }

    public boolean canMaintenanceVds(Guid vdsId, ArrayList<String> reasons) {
        boolean returnValue = true;
        // VDS vds = ResourceManager.Instance.getVds(vdsId);
        VDS vds = DbFacade.getInstance().getVdsDao().get(vdsId);
        // we can get here when vds status was set already to Maintenance
        if ((vds.getStatus() != VDSStatus.Maintenance) && (vds.getStatus() != VDSStatus.NonResponsive)
                && (vds.getStatus() != VDSStatus.Up) && (vds.getStatus() != VDSStatus.Error)
                && (vds.getStatus() != VDSStatus.PreparingForMaintenance) && (vds.getStatus() != VDSStatus.Down
                && (vds.getStatus() != VDSStatus.InstallFailed))) {
            returnValue = false;
            reasons.add(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_OPERATIONAL.toString());
        }

        orderListOfRunningVmsOnVds(vdsId);

        for (VM vm : vms) {
            if (vm.isHostedEngine()) {
                // The Hosted Engine vm is migrated by the HA agent
                continue;
            }
            if (vm.getMigrationSupport() != MigrationSupport.MIGRATABLE) {
                reasons.add(VdcBllMessages.VDS_CANNOT_MAINTENANCE_IT_INCLUDES_NON_MIGRATABLE_VM.toString());
                return false;
            }
        }

        return returnValue;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = Collections.singletonMap(VdcObjectType.VDS.name().toLowerCase(), getVdsName());
        }

        return jobProperties;
    }

    private boolean isHostedEngineOnVds() {
        for (VM vm : vms) {
            if (vm.isHostedEngine()) {
                return true;
            }
        }
        return false;
    }

}
