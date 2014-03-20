package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.CopyOnNewVersion;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This class updates VM to the latest template version for stateless vms that has newer template version
 */
@InternalCommandAttribute
public class UpdateVmVersionCommand<T extends UpdateVmVersionParameters> extends VmCommand<T> {

    private static final Log log = LogFactory.getLog(UpdateVmVersionCommand.class);
    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected UpdateVmVersionCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateVmVersionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, parameters.getVmId()));

        // vm should be filled in end action
        if (parameters.getVm() != null) {
            setVmTemplateId(parameters.getVm().getVmtGuid());
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVm().getStatus() != VMStatus.Down) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        }

        if (!getVm().isUseLatestVersion()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_SET_FOR_LATEST);
        }

        if (getVmTemplate() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (getVm().getVmtGuid().equals(getVmTemplate().getId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_ALREADY_IN_LATEST_VERSION);
        }

        getVm().setVmtGuid(getVmTemplate().getId());

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE_VM_VERSION);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected void executeVmCommand() {
        if (!copyData(getVmTemplate(), getVm().getStaticData())) {
            return;
        }
        getParameters().setVmStaticData(getVm().getStaticData());

        if (getVm().getVmPoolId() != null) {
            getParameters().setVmPoolId(getVm().getVmPoolId());
            RemoveVmFromPoolParameters removeVmFromPoolParas = new RemoveVmFromPoolParameters(getVmId(), false);
            removeVmFromPoolParas.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            VdcReturnValueBase result = runInternalActionWithTasksContext(
                    VdcActionType.RemoveVmFromPool,
                    removeVmFromPoolParas,
                    getLock());
            if (!result.getSucceeded()) {
                log.errorFormat("Could not detach vm {0} ({1}) from vm-pool {2}.",
                        getVm().getName(),
                        getVmId(),
                        getVm().getVmPoolName());
                return;
            }
        }

        RemoveVmParameters removeParams = new RemoveVmParameters(getVmId(), false);
        removeParams.setParentCommand(getActionType());
        removeParams.setParentParameters(getParameters());
        removeParams.setEntityInfo(getParameters().getEntityInfo());
        VdcReturnValueBase result = runInternalActionWithTasksContext(VdcActionType.RemoveVm, removeParams, getLock());

        if (result.getSucceeded()) {
            if (result.getHasAsyncTasks()) {
                getReturnValue().getVdsmTaskIdList().addAll(result.getInternalVdsmTaskIdList());
            } else {
                endVmCommand();
            }
            setSucceeded(true);
        }
    }

    private void addUpdatedVm() {
        VmManagementParametersBase addVmParams;
        VdcActionType action;
        if (getParameters().getVmPoolId() != null) {
            addVmParams =
                    new AddVmAndAttachToPoolParameters(getParameters().getVmStaticData(),
                            getParameters().getVmPoolId(),
                            getParameters().getVmStaticData().getName(),
                            new HashMap<Guid, DiskImage>());
            action = VdcActionType.AddVmAndAttachToPool;
        } else {
            addVmParams = new VmManagementParametersBase(getParameters().getVmStaticData());
            action = VdcActionType.AddVm;
        }

        addVmParams.setDiskInfoDestinationMap(new HashMap<Guid, DiskImage>());
        addVmParams.setConsoleEnabled(deviceExists(VmDeviceGeneralType.CONSOLE, VmDeviceType.CONSOLE));
        addVmParams.setBalloonEnabled(deviceExists(VmDeviceGeneralType.BALLOON, VmDeviceType.BALLOON));
        addVmParams.setSoundDeviceEnabled(deviceExists(VmDeviceGeneralType.SOUND, VmDeviceType.SOUND));
        addVmParams.setVirtioScsiEnabled(deviceExists(VmDeviceGeneralType.CONTROLLER, VmDeviceType.VIRTIOSCSI));

        List<VmWatchdog> watchdogs = runInternalQuery(VdcQueryType.GetWatchdog,
                new IdQueryParameters(getVmTemplateId())).getReturnValue();
        if (!watchdogs.isEmpty()) {
            addVmParams.setWatchdog(watchdogs.get(0));
        }

        if (!StringUtils.isEmpty(getParameters().getSessionId())) {
            VmPayload payload = runInternalQuery(VdcQueryType.GetVmPayload,
                    new IdQueryParameters(getVmTemplateId())).getReturnValue();

            if (payload != null) {
                addVmParams.setVmPayload(payload);
            }
        }

        // when this initiated from down vm event (restore stateless vm)
        // then there is no session, so using the current user.
        if (StringUtils.isEmpty(getParameters().getSessionId())) {
            addVmParams.setParametersCurrentUser(getCurrentUser());
        } else {
            addVmParams.setSessionId(getParameters().getSessionId());
        }
        runInternalAction(action, addVmParams,
                ExecutionHandler.createDefaultContextForTasks(getContext(), getLock()));
    }

    /**
     * Copy fields that annotated with {@link CopyOnNewVersion} from the new template version to the vm
     *
     * @param source
     *            - template to copy data from
     * @param dest
     *            - vm to copy data to
     */
    private boolean copyData(VmBase source, VmBase dest) {
        for (Field srcFld : VmBase.class.getDeclaredFields()) {
            try {
                if (srcFld.getAnnotation(CopyOnNewVersion.class) != null) {
                    srcFld.setAccessible(true);

                    Field dstFld = VmBase.class.getDeclaredField(srcFld.getName());
                    dstFld.setAccessible(true);
                    dstFld.set(dest, srcFld.get(source));
                }
            } catch (Exception exp) {
                log.errorFormat("Failed to copy field {0} of new version to VM {1} ({2}), error: {3}",
                        srcFld.getName(),
                        source.getName(),
                        source.getId(),
                        exp.getMessage());
                return false;
            }
        }
        return true;
    }

    private boolean deviceExists(VmDeviceGeneralType generalType, VmDeviceType deviceType) {
        return !getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(
                getVmTemplateId(), generalType, deviceType.getName()).isEmpty();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        // take shared lock on latest template, since we will add vm from it
        if (getVm() != null) {
            VmTemplate latest = getVmTemplateDAO().getTemplateWithLatestVersionInChain(getVm().getVmtGuid());
            if (latest != null) {
                setVmTemplateId(latest.getId());
                setVmTemplate(latest);
                return Collections.singletonMap(latest.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }
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
