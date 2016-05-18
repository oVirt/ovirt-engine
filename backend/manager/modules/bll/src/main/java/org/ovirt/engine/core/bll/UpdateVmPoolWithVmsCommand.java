package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends CommonVmPoolWithVmsCommand<T>  implements RenamedEntityInfoProvider{

    private VmPool oldPool;
    @Inject
    private VmPoolMonitor vmPoolMonitor;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected UpdateVmPoolWithVmsCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateVmPoolWithVmsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected Guid getPoolId() {
        getVmPoolDao().update(getVmPool());
        return getVmPool().getVmPoolId();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmPoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL, getVmPoolIsBeingUpdatedMessage()));
    }

    private String getVmPoolIsBeingUpdatedMessage() {
        StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_BEING_UPDATED.name());
        if (getVmPool() != null) {
            builder.append(String.format("$VmPoolName %1$s", getVmPool().getName()));
        }
        return builder.toString();
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        oldPool = getVmPoolDao().get(getVmPool().getVmPoolId());
        if (oldPool == null) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_UPDATE_POOL_NOT_FOUND);
        }

        if (getParameters().getVmsCount() < 0) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_DECREASE_VMS_FROM_POOL);
        }

        if (oldPool.getVmPoolType() != getParameters().getVmPool().getVmPoolType()) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_CHANGE_POOL_TYPE);
        }

        if (oldPool.isStateful() != getParameters().getVmPool().isStateful()) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_CHANGE_POOL_STATEFUL_OPTION);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return isAddVmsSucceded() ? AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS
                : AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS_FAILED;
    }

    @Override
    protected void executeCommand() {
        VdcQueryReturnValue currentVmsInPoolQuery =
                runInternalQuery(VdcQueryType.GetAllPoolVms, new IdQueryParameters(getVmPool().getVmPoolId()));
        List<VM> poolVmsBeforeAdd = currentVmsInPoolQuery.getSucceeded() ? currentVmsInPoolQuery.<List<VM>>getReturnValue() : null;

        super.executeCommand();
        getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();

        if (getSucceeded()) {
            updatePoolVms(poolVmsBeforeAdd);
        }
        vmPoolMonitor.triggerPoolMonitoringJob();
    }

    private void updatePoolVms(List<VM> vmsInPool) {
        boolean isUpdatedPoolLatest = getParameters().getVmStaticData().isUseLatestVersion(); // new latest value

        if (vmsInPool != null && !vmsInPool.isEmpty()) {

            // Check one VM in order to decide if template version was changed
            boolean isUpdateNeeded = isUpdateVmRequired(vmsInPool.get(0));

            // if a change in template version was detected --> update all pool vms
            if (isUpdateNeeded) {
                VmTemplateHandler.lockVmTemplateInTransaction(getParameters().getVmStaticData().getVmtGuid(),
                        getCompensationContext());
                for (VM vm : vmsInPool) {
                    VmManagementParametersBase updateParams = new VmManagementParametersBase(vm);
                    updateParams.getVmStaticData().setUseLatestVersion(isUpdatedPoolLatest);
                    if (!isUpdatedPoolLatest) {
                        updateParams.getVmStaticData().setVmtGuid(getParameters().getVmStaticData().getVmtGuid());
                    }

                    VdcReturnValueBase result =
                            runInternalActionWithTasksContext(
                                    VdcActionType.UpdateVm,
                                    updateParams,
                                    getLock()
                            );
                    getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
                    setSucceeded(getSucceeded() && result.getSucceeded());
                }
                VmTemplateHandler.unlockVmTemplate(getParameters().getVmStaticData().getVmtGuid());
            }
        }
    }

    private boolean isUpdateVmRequired(VM poolVm) {
        Guid currentTempVersion = null; // old template version (based on current vm data)
        boolean isCurrentLatest = false; // old latest status
        if (poolVm.isNextRunConfigurationExists()) {
            VdcQueryReturnValue qRetNextRun =
                    getBackend().runInternalQuery(VdcQueryType.GetVmNextRunConfiguration,
                            new IdQueryParameters(poolVm.getId()));
            if (qRetNextRun.getSucceeded()) {
                final VM nextRunVm =
                        qRetNextRun.getReturnValue();
                if (nextRunVm != null) { // template version was changed, the cause still needs to be checked
                    currentTempVersion = nextRunVm.getVmtGuid();
                    isCurrentLatest = nextRunVm.isUseLatestVersion();
                }
            }
        } else {
            currentTempVersion = poolVm.getVmtGuid();
            isCurrentLatest = poolVm.isUseLatestVersion();
        }

        boolean isLatestPropertyChanged = isCurrentLatest != getParameters().getVmStaticData().isUseLatestVersion();

        // template ID changed but latest is not set, as it would cause false-positives
        boolean isTemplateIdChanged = false;
        Guid newPoolTemplateVersion = getParameters().getVmStaticData().getVmtGuid();
        if (newPoolTemplateVersion != null) {
            isTemplateIdChanged = !newPoolTemplateVersion.equals(currentTempVersion) && !isCurrentLatest;
        }

        return isLatestPropertyChanged || isTemplateIdChanged;
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.VmPool.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return oldPool.getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getVmPool().getName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }
}
