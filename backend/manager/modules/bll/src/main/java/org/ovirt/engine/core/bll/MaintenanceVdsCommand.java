package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.InternalMigrateVmParameters;
import org.ovirt.engine.core.common.action.MaintenanceVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparer;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;

@NonTransactiveCommandAttribute
public class MaintenanceVdsCommand<T extends MaintenanceVdsParameters> extends VdsCommand<T> {

    private final boolean _isInternal;
    private List<VM> vms;
    private boolean haMaintenanceFailed;
    @Inject
    private SchedulingManager schedulingManager;

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
                setVdsStatus(VDSStatus.Maintenance);
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
        List<Guid> blacklist = new ArrayList<>();
        if (getVdsId() != null) {
            blacklist.add(getVdsId());
        }
        return schedulingManager.canSchedule(
                getCluster(),
                vm,
                blacklist, //blacklist only contains the host we're putting to maintenance
                Collections.<Guid>emptyList(), //no whitelist
                vm.getDedicatedVmForVdsList(),
                new ArrayList<>()
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
                    log.error("There is no host capable of running the hosted engine VM");
                }
                // The Hosted Engine vm is migrated by the HA agent
                continue;
            }
            // if HAOnly is true check that vm is HA (auto_startup should be true)
            if (vm.getStatus() != VMStatus.MigratingFrom && (!HAOnly || vm.isAutoStartup())) {
                if (!migrateVm(vm, parentContext)) {
                    succeeded = false;
                    appendCustomValue("failedVms", vm.getName(), ",");
                    log.error("Failed to migrate VM '{}'", vm.getName());
                }
            }
        }
        return succeeded;
    }

    protected boolean migrateVm(VM vm, ExecutionContext parentContext) {
        return runInternalAction(VdcActionType.InternalMigrateVm,
                new InternalMigrateVmParameters(vm.getId(), getActionType()),
                createMigrateVmContext(parentContext, vm))
                .getSucceeded();
    }

    protected CommandContext createMigrateVmContext(ExecutionContext parentContext, VM vm) {
        ExecutionContext ctx = new ExecutionContext();
        try {
            Map<String, String> values = new HashMap<>();
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
    protected boolean validate() {
        return canMaintenanceVds(getVdsId(), getReturnValue().getValidationMessages());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (_isInternal) {
            if (isSucceededWithHA()) {
                return AuditLogType.VDS_MAINTENANCE;
            } else if (getSucceeded()) {
                return AuditLogType.VDS_MAINTENANCE_MANUAL_HA;
            } else {
                return AuditLogType.VDS_MAINTENANCE_FAILED;
            }
        } else {
            if (isSucceededWithReasonGiven()){
                addCustomValue("Reason", getVds().getMaintenanceReason());
                return AuditLogType.USER_VDS_MAINTENANCE;
            } else if(isSucceededWithoutReasonGiven()) {
                return AuditLogType.USER_VDS_MAINTENANCE_WITHOUT_REASON;
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
            reasons.add(EngineMessage.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_OPERATIONAL.toString());
        }

        orderListOfRunningVmsOnVds(vdsId);

        for (VM vm : vms) {
            if (vm.isHostedEngine()) {
                // The Hosted Engine vm is migrated by the HA agent
                continue;
            }
            if (vm.getMigrationSupport() != MigrationSupport.MIGRATABLE) {
                reasons.add(EngineMessage.VDS_CANNOT_MAINTENANCE_IT_INCLUDES_NON_MIGRATABLE_VM.toString());
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

    @Override
    public CommandCallback getCallback() {
        if (getVds().getClusterSupportsGlusterService() && getParameters().isStopGlusterService()) {
            return new HostMaintenanceCallback();
        } else {
            return super.getCallback();
        }
    }

    private boolean isSucceededWithHA() {
        return getSucceeded() && !haMaintenanceFailed;
    }

    private boolean isSucceededWithReasonGiven(){
        return isSucceededWithHA() && StringUtils.isNotEmpty(getVds().getMaintenanceReason());
    }

    private boolean isSucceededWithoutReasonGiven(){
        return isSucceededWithHA() && !haMaintenanceFailed && StringUtils.isEmpty(getVds().getMaintenanceReason());
    }
}
