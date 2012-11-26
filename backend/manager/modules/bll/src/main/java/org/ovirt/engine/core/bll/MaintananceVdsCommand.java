package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MaintananceVdsParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmsComparer;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

public class MaintananceVdsCommand<T extends MaintananceVdsParameters> extends VdsCommand<T> {

    private static final long serialVersionUID = -7604781532599945079L;
    private final boolean _isInternal;
    private List<VM> vms;

    public MaintananceVdsCommand(T parameters) {
        super(parameters);
        _isInternal = parameters.getIsInternal();
    }

    @Override
    protected void executeCommand() {
        if (getVds().getstatus() == VDSStatus.Maintenance) {
            // nothing to do
            setSucceeded(true);
        } else {
            setSucceeded(MigrateAllVms(getExecutionContext()));

            /**
             * if non responsive move directly to maintenance
             */
            if (getVds().getstatus() == VDSStatus.NonResponsive
                    || getVds().getstatus() == VDSStatus.Connecting
                    || getVds().getstatus() == VDSStatus.Down) {
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.SetVdsStatus,
                                new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Maintenance));
            }
        }
        if (vms != null && !vms.isEmpty()) {
            ExecutionHandler.setAsyncJob(getExecutionContext(), true);
        }
    }

    protected void orderListOfRunningVmsOnVds(Guid vdsId) {
        vms = DbFacade.getInstance().getVmDao().getAllRunningForVds(vdsId);
        Collections.sort(vms, Collections.reverseOrder(new VmsComparer()));
    }

    protected boolean MigrateAllVms(ExecutionContext parentContext) {
        return MigrateAllVms(parentContext, false);
    }

    protected boolean MigrateAllVms(ExecutionContext parentContext, boolean HAOnly) {
        orderListOfRunningVmsOnVds(getVdsId());

        boolean succeeded = true;

        for (VM vm : vms) {
            // if HAOnly is true check that vm is HA (auto_startup should be
            // true)
            if (vm.getStatus() != VMStatus.MigratingFrom && (!HAOnly || (HAOnly && vm.isAutoStartup()))) {
                MigrateVmParameters tempVar = new MigrateVmParameters(false, vm.getId());
                tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                ExecutionContext ctx = createMigrateVmContext(parentContext, vm);
                VdcReturnValueBase result =
                        Backend.getInstance().runInternalAction(VdcActionType.InternalMigrateVm,
                                tempVar,
                                new CommandContext(ctx));
                if (!result.getCanDoAction() || !(((Boolean) result.getActionReturnValue()).booleanValue())) {
                    succeeded = false;
                    AppendCustomValue("failedVms", vm.getVmName(), ",");
                    log.errorFormat("ResourceManager::vdsMaintenance - Failed migrating desktop '{0}'", vm.getVmName());
                }
            }
        }
        return succeeded;
    }

    private ExecutionContext createMigrateVmContext(ExecutionContext parentContext,VM vm) {
        ExecutionContext ctx = new ExecutionContext();
        try {
            Map<String, String> values = new HashMap<String, String>();
            values.put(VdcObjectType.VM.name().toLowerCase(), vm.getVmName());
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
        return ctx;
    }

    @Override
    protected boolean canDoAction() {
        return CanMaintananceVds(getVdsId(), getReturnValue().getCanDoActionMessages());
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

    public boolean CanMaintananceVds(Guid vdsId, java.util.ArrayList<String> reasons) {
        boolean returnValue = true;
        // VDS vds = ResourceManager.Instance.getVds(vdsId);
        VDS vds = DbFacade.getInstance().getVdsDao().get(vdsId);
        // we can get here when vds status was set already to Maintenance
        if ((vds.getstatus() != VDSStatus.Maintenance) && (vds.getstatus() != VDSStatus.NonResponsive)
                && (vds.getstatus() != VDSStatus.Up) && (vds.getstatus() != VDSStatus.Error)
                && (vds.getstatus() != VDSStatus.PreparingForMaintenance) && (vds.getstatus() != VDSStatus.Down)) {
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

    public static void ProcessStorageOnVdsInactive(VDS vds) {

        // Clear the problematic timers since the VDS is in maintenance so it doesn't make sense to check it
        // anymore.
        IrsBrokerCommand.clearVdsFromCache(vds.getStoragePoolId(), vds.getId(), vds.getvds_name());

        if (!vds.getStoragePoolId().equals(Guid.Empty)
                && StoragePoolStatus.Uninitialized != DbFacade.getInstance()
                        .getStoragePoolDao()
                        .get(vds.getStoragePoolId())
                        .getstatus()
                && Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.DisconnectStoragePool,
                                new DisconnectStoragePoolVDSCommandParameters(vds.getId(),
                                        vds.getStoragePoolId(), vds.getvds_spm_id())).getSucceeded()) {
            StoragePoolParametersBase tempVar = new StoragePoolParametersBase(vds.getStoragePoolId());
            tempVar.setVdsId(vds.getId());
            tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            Backend.getInstance().runInternalAction(VdcActionType.DisconnectHostFromStoragePoolServers, tempVar);
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = Collections.singletonMap(VdcObjectType.VDS.name().toLowerCase(), getVdsName());
        }

        return jobProperties;
    }

}
