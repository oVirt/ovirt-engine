package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmPoolDao;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmPoolCommand<T extends AddVmPoolParameters> extends CommonVmPoolCommand<T>
        implements RenamedEntityInfoProvider {

    @Inject
    private VmPoolMonitor vmPoolMonitor;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private VmDao vmDao;

    private VmPool oldPool;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected UpdateVmPoolCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateVmPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void createOrUpdateVmPool() {
        vmPoolDao.update(getVmPool());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmPoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL,
                        new LockMessage(EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_BEING_UPDATED)
                                .withOptional("VmPoolName", getVmPool() != null ? getVmPool().getName() : null)));
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        oldPool = vmPoolDao.get(getVmPool().getVmPoolId());
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

        if (!oldPool.getName().equals(getParameters().getVmPool().getName())) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_CHANGE_POOL_NAME);
        }

        if (oldPool.isAutoStorageSelect() != getParameters().getVmPool().isAutoStorageSelect()) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_CHANGE_AUTO_STORAGE_SELECT);
        }

        if (isVmDeleteProtected()) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_CHANGE_TEMPLATE_WHEN_DELETE_PROTECTED);
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
        return isAllAddVmsSucceeded() ? AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS
                : AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS_FAILED;
    }

    @Override
    protected void executeCommand() {
        QueryReturnValue currentVmsInPoolQuery =
                runInternalQuery(QueryType.GetAllPoolVms, new IdQueryParameters(getVmPool().getVmPoolId()));
        List<VM> poolVmsBeforeAdd =
                currentVmsInPoolQuery.getSucceeded() ? currentVmsInPoolQuery.getReturnValue() : null;

        super.executeCommand();
        getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();

        if (getSucceeded() && isUpdateVmRequired(poolVmsBeforeAdd)) {
            // if a change in template version was detected --> update all pool vms
            updatePoolVms(poolVmsBeforeAdd);
        }
        vmPoolMonitor.triggerPoolMonitoringJob();
    }

    private boolean isVmDeleteProtected() {
        List<VM> vmsInPool = vmDao.getAllForVmPool(oldPool.getVmPoolId());

        for (VM vm : vmsInPool) {
            if ((!vm.getVmtGuid().equals(getParameters().getVmStaticData().getVmtGuid()) ||
                    vm.isUseLatestVersion() != getParameters().getVmStaticData().isUseLatestVersion()) &&
                    vm.isDeleteProtected()) {
                return true;
            }
        }
        return false;
    }

    private void updatePoolVms(List<VM> vmsInPool) {
        boolean isUpdatedPoolLatest = getParameters().getVmStaticData().isUseLatestVersion(); // new latest value

        vmTemplateHandler.lockVmTemplateInTransaction(getParameters().getVmStaticData().getVmtGuid(),
                getCompensationContext());
        for (VM vm : vmsInPool) {
            VmManagementParametersBase updateParams = new VmManagementParametersBase(vm);
            updateParams.getVmStaticData().setUseLatestVersion(isUpdatedPoolLatest);
            if (!isUpdatedPoolLatest) {
                updateParams.getVmStaticData().setVmtGuid(getParameters().getVmStaticData().getVmtGuid());
            }

            ActionReturnValue result =
                    runInternalActionWithTasksContext(
                            ActionType.UpdateVm,
                            updateParams,
                            getLock());
            getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
            setSucceeded(getSucceeded() && result.getSucceeded());
        }
        vmTemplateHandler.unlockVmTemplate(getParameters().getVmStaticData().getVmtGuid());
    }

    private boolean isUpdateVmRequired(List<VM> vmsInPool) {
        if (vmsInPool == null || vmsInPool.isEmpty()) {
            return false;
        }

        // Check one VM in order to decide if template version was changed
        VM poolVm = vmsInPool.get(0);

        Guid currentTemplateVersion = null; // old template version (based on current vm data)
        boolean isCurrentLatest = false; // old latest status
        if (poolVm.isNextRunConfigurationExists()) {
            QueryReturnValue qRetNextRun =
                    backend.runInternalQuery(QueryType.GetVmNextRunConfiguration,
                            new IdQueryParameters(poolVm.getId()));
            if (qRetNextRun.getSucceeded()) {
                final VM nextRunVm =
                        qRetNextRun.getReturnValue();
                if (nextRunVm != null) { // template version was changed, the cause still needs to be checked
                    currentTemplateVersion = nextRunVm.getVmtGuid();
                    isCurrentLatest = nextRunVm.isUseLatestVersion();
                }
            }
        } else {
            currentTemplateVersion = poolVm.getVmtGuid();
            isCurrentLatest = poolVm.isUseLatestVersion();
        }

        boolean isLatestPropertyChanged = isCurrentLatest != getParameters().getVmStaticData().isUseLatestVersion();

        // template ID changed but latest is not set, as it would cause false-positives
        boolean isTemplateIdChanged = false;
        Guid newPoolTemplateVersion = getParameters().getVmStaticData().getVmtGuid();
        if (newPoolTemplateVersion != null) {
            isTemplateIdChanged = !newPoolTemplateVersion.equals(currentTemplateVersion) && !isCurrentLatest;
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
    public void setEntityId(AuditLogable logable) {
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void updateVmInitPassword() {
        // We are not passing the VmInit password to the UI,
        // so we need to update the VmInit password from one of the VMs in the pool.
        if (getParameters().getVmStaticData().getVmInit() != null &&
                getParameters().getVmStaticData().getVmInit().isPasswordAlreadyStored()) {
            VM temp = vmPoolDao.getVmDataFromPoolByPoolGuid(getParameters().getVmPoolId(), null, false);
            vmHandler.updateVmInitFromDB(temp.getStaticData(), false);
            String password = temp.getVmInit() != null ? temp.getVmInit().getRootPassword() : null;
            getParameters().getVmStaticData().getVmInit().setRootPassword(password);
            getParameters().getVmStaticData().getVmInit().setPasswordAlreadyStored(false);
        }
    }

}
