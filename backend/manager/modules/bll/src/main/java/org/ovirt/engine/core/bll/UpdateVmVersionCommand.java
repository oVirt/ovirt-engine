package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class updates VM to the required template version for stateless VMs
 */
@InternalCommandAttribute
public class UpdateVmVersionCommand<T extends UpdateVmVersionParameters> extends VmCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(UpdateVmVersionCommand.class);

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected UpdateVmVersionCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateVmVersionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, parameters.getVmId()));

        if (getVm() != null) {
            if (parameters.getNewTemplateVersion() != null) {
                setVmTemplate(getVmTemplateDao().get(parameters.getNewTemplateVersion()));
            } else {
                setVmTemplate(getVmTemplateDao().getTemplateWithLatestVersionInChain(getVm().getVmtGuid()));
            }
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVm().getStatus() != VMStatus.Down) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        }

        if (!getVm().isUseLatestVersion() && getParameters().getNewTemplateVersion() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_SET_FOR_LATEST);
        }

        if (getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (getVmTemplateId().equals(getVm().getVmtGuid())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_ALREADY_IN_LATEST_VERSION);
        }

        getVm().setVmtGuid(getVmTemplate().getId());

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE_VM_VERSION);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void executeVmCommand() {
        // load vm init from db
        VmHandler.updateVmInitFromDB(getVmTemplate(), false);
        if (!VmHandler.copyData(getVmTemplate(), getVm().getStaticData())) {
            return;
        }

        getParameters().setPreviousDiskOperatorAuthzPrincipalDbId(getIdOfDiskOperator());
        getParameters().setVmStaticData(getVm().getStaticData());

        if (getParameters().getUseLatestVersion() != null) {
            getParameters().getVmStaticData().setUseLatestVersion(getParameters().getUseLatestVersion());
        }

        if (getVm().getVmPoolId() != null) {
            getParameters().setVmPoolId(getVm().getVmPoolId());

            VdcReturnValueBase result = runInternalActionWithTasksContext(
                    VdcActionType.RemoveVmFromPool,
                    buildRemoveVmFromPoolParameters(),
                    getLock());
            if (!result.getSucceeded()) {
                log.error("Could not detach vm '{}' ({}) from vm-pool '{}'.",
                        getVm().getName(),
                        getVmId(),
                        getVm().getVmPoolName());
                return;
            }
        }

        VdcReturnValueBase result = runInternalActionWithTasksContext(
                VdcActionType.RemoveVm,
                buildRemoveVmParameters(),
                getLock());

        if (result.getSucceeded()) {
            if (result.getHasAsyncTasks()) {
                getReturnValue().getVdsmTaskIdList().addAll(result.getInternalVdsmTaskIdList());
            } else {
                endVmCommand();
            }
            setSucceeded(true);
        }
    }

    private Guid getIdOfDiskOperator() {
        List<Disk> diskIds = getDbFacade().getDiskDao().getAllForVm(getVmId());
        if (diskIds.isEmpty()) {
            return null;
        }

        List<Permission> perms = getPermissionDao().getAllForRoleAndObject(PredefinedRoles.DISK_OPERATOR.getId(), diskIds.iterator().next().getId());
        if (perms.isEmpty()) {
            return null;
        }

        return perms.iterator().next().getAdElementId();
    }

    private RemoveVmFromPoolParameters buildRemoveVmFromPoolParameters() {
        RemoveVmFromPoolParameters parameters = new RemoveVmFromPoolParameters(getVmId(), false, false);
        parameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
        return parameters;
    }

    private RemoveVmParameters buildRemoveVmParameters() {
        RemoveVmParameters parameters = new RemoveVmParameters(getVmId(), false);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setRemovePermissions(false);
        return parameters;
    }


    private void addUpdatedVm() {
        runInternalAction(VdcActionType.AddVm,
                buildAddVmParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext(), getLock()));
    }

    private AddVmParameters buildAddVmParameters() {
        AddVmParameters addVmParams = new AddVmParameters(getParameters().getVmStaticData());
        addVmParams.setPoolId(getParameters().getVmPoolId());
        addVmParams.setDiskInfoDestinationMap(new HashMap<>());
        addVmParams.setConsoleEnabled(deviceExists(VmDeviceGeneralType.CONSOLE));
        addVmParams.setBalloonEnabled(deviceExists(VmDeviceGeneralType.BALLOON, VmDeviceType.MEMBALLOON));
        addVmParams.setSoundDeviceEnabled(deviceExists(VmDeviceGeneralType.SOUND));
        addVmParams.setVirtioScsiEnabled(deviceExists(VmDeviceGeneralType.CONTROLLER, VmDeviceType.VIRTIOSCSI));

        List<VmWatchdog> watchdogs = runInternalQuery(VdcQueryType.GetWatchdog,
                new IdQueryParameters(getVmTemplateId())).getReturnValue();
        if (!watchdogs.isEmpty()) {
            addVmParams.setWatchdog(watchdogs.get(0));
        }

        loadVmPayload(addVmParams);

        // when this initiated from down vm event (restore stateless vm)
        // then there is no session, so using the current user.
        if (StringUtils.isEmpty(getParameters().getSessionId())) {
            addVmParams.setParametersCurrentUser(getCurrentUser());
        } else {
            addVmParams.setSessionId(getParameters().getSessionId());
        }
        addVmParams.setDiskOperatorAuthzPrincipalDbId(getParameters().getPreviousDiskOperatorAuthzPrincipalDbId());

        // reset vm to not initialized
        addVmParams.getVmStaticData().setInitialized(false);

        return addVmParams;
    }

    private void loadVmPayload(AddVmParameters addVmParams) {
        List<VmDevice> vmDevices = getVmDeviceDao()
                .getVmDeviceByVmIdAndType(getVmTemplateId(),
                        VmDeviceGeneralType.DISK);
        for (VmDevice vmDevice : vmDevices) {
            if (VmPayload.isPayload(vmDevice.getSpecParams())) {
                addVmParams.setVmPayload(new VmPayload(vmDevice));
                return;
            }
        }
    }

    private boolean deviceExists(VmDeviceGeneralType generalType, VmDeviceType deviceType) {
        return !getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(
                getVmTemplateId(), generalType, deviceType.getName()).isEmpty();
    }

    private boolean deviceExists(VmDeviceGeneralType generalType) {
        return !getVmDeviceDao().getVmDeviceByVmIdAndType(
                getVmTemplateId(), generalType).isEmpty();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getParameters().isLockVm() && getVmId() != null) {
            return Collections.singletonMap(getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }

        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        // take shared lock on required template, since we will add vm from it
        if (getVmTemplateId() != null) {
            return Collections.singletonMap(getVmTemplateId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }

        return null;
    }

    @Override
    protected void endVmCommand() {
        addUpdatedVm();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        // nothing to do
    }

}
