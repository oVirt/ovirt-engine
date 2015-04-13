package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.AddVmToPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;

/**
 * This class adds a thinly provisioned VM based on disks list or over a template.
 * The VM is created as a member of a VM pool.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class AddVmAndAttachToPoolCommand<T extends AddVmAndAttachToPoolParameters> extends AddVmCommand<T> {
    private VmPool vmPool;

    protected VmPool getVmPool() {
        if (vmPool == null && getParameters().getPoolId() != null) {
            vmPool = getDbFacade().getVmPoolDao().get(getParameters().getPoolId());
        }
        return vmPool;
    }

    public AddVmAndAttachToPoolCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getPoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL, getVmIsBeingCreatedAndAttachedMessage()));
    }

    private String getVmIsBeingCreatedAndAttachedMessage() {
        StringBuilder builder = new StringBuilder(
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_CREATED_AND_ATTACHED_TO_POOL.name());
        if (getVmPool() != null) {
            builder.append(String.format("$VmPoolName %1$s", getVmPool().getName()));
        }
        return builder.toString();
    }

    /**
     * This operation may take much time.
     */
    @Override
    protected void executeCommand() {
        VmStatic vmStatic = getParameters().getVmStaticData();
        VdcActionType action = VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(vmStatic.getVmtGuid()) ?
                VdcActionType.AddVmFromScratch : VdcActionType.AddVm;

        VdcReturnValueBase returnValueFromAddVm = runInternalActionWithTasksContext(
                action,
                buildAddVmParameters(action));

        if (returnValueFromAddVm.getSucceeded()) {
            getTaskIdList().addAll(returnValueFromAddVm.getInternalVdsmTaskIdList());
            addVmToPool(vmStatic);
        }
    }

    private AddVmParameters buildAddVmParameters(VdcActionType action) {
        AddVmParameters parameters = new AddVmParameters(getParameters().getVmStaticData());
        parameters.setDiskOperatorAuthzPrincipalDbId(getParameters().getDiskOperatorAuthzPrincipalDbId());
        parameters.getGraphicsDevices().putAll(getParameters().getGraphicsDevices());
        parameters.setPoolId(getParameters().getPoolId());

        if (action == VdcActionType.AddVmFromScratch) {
            parameters.setDiskInfoList(getParameters().getDiskInfoList());
            parameters.setStorageDomainId(getParameters().getStorageDomainId());
            parameters.setSessionId(getParameters().getSessionId());
            parameters.setDontAttachToDefaultTag(true);
        } else {
            if (StringUtils.isEmpty(getParameters().getSessionId())) {
                parameters.setParametersCurrentUser(getCurrentUser());
            } else {
                parameters.setSessionId(getParameters().getSessionId());
            }
            parameters.setDontAttachToDefaultTag(true);
            parameters.setDiskInfoDestinationMap(diskInfoDestinationMap);
            parameters.setSoundDeviceEnabled(getParameters().isSoundDeviceEnabled());
            parameters.setConsoleEnabled(getParameters().isConsoleEnabled());
            parameters.setVirtioScsiEnabled(getParameters().isVirtioScsiEnabled());
            parameters.setBalloonEnabled(getParameters().isBalloonEnabled());

            if (getParameters().isUpdateRngDevice()) {
                parameters.setUpdateRngDevice(true);
                parameters.setRngDevice(getParameters().getRngDevice());
            }
        }

        return parameters;
    }

    private void addVmToPool(VmStatic vmStatic) {
        AddVmToPoolParameters parameters = new AddVmToPoolParameters(getParameters().getPoolId(),
                vmStatic.getId());
        parameters.setShouldBeLogged(false);
        VdcReturnValueBase result = runInternalActionWithTasksContext(
                VdcActionType.AddVmToPool,
                parameters);
        setSucceeded(result.getSucceeded());
        addVmPermission();
    }
}
