package org.ovirt.engine.core.bll;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.CopyOnNewVersion;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
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
 * This class updates VM to the latest template version for stateless vms that has newer template version
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
            setVmTemplate(getVmTemplateDAO().getTemplateWithLatestVersionInChain(getVm().getVmtGuid()));
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

        if (getVmTemplateId().equals(getVm().getVmtGuid())) {
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
        // load vm init from db
        VmHandler.updateVmInitFromDB(getVmTemplate(), false);
        if (!copyData(getVmTemplate(), getVm().getStaticData())) {
            return;
        }

        getParameters().setPreviousDiskOperatorAuthzPrincipalDbId(getIdOfDiskOperator());
        getParameters().setVmStaticData(getVm().getStaticData());

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

        List<Permissions> perms = getPermissionDAO().getAllForRoleAndObject(PredefinedRoles.DISK_OPERATOR.getId(), diskIds.iterator().next().getId());
        if (perms.isEmpty()) {
            return null;
        }

        return perms.iterator().next().getad_element_id();
    }

    private RemoveVmFromPoolParameters buildRemoveVmFromPoolParameters() {
        RemoveVmFromPoolParameters parameters = new RemoveVmFromPoolParameters(getVmId(), false);
        parameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
        return parameters;
    }

    private RemoveVmParameters buildRemoveVmParameters() {
        RemoveVmParameters parameters = new RemoveVmParameters(getVmId(), false);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        return parameters;
    }

    private void addUpdatedVm() {
        VdcActionType action = getParameters().getVmPoolId() == null ? VdcActionType.AddVm : VdcActionType.AddVmAndAttachToPool;
        runInternalAction(action,
                buildAddVmParameters(action),
                ExecutionHandler.createDefaultContextForTasks(getContext(), getLock()));
    }

    private AddVmParameters buildAddVmParameters(VdcActionType action) {
        AddVmParameters addVmParams;
        if (action == VdcActionType.AddVmAndAttachToPool) {
            addVmParams = new AddVmAndAttachToPoolParameters(getParameters().getVmStaticData(),
                    getParameters().getVmPoolId(),
                    getParameters().getVmStaticData().getName(),
                    new HashMap<Guid, DiskImage>());
        }
        else {
            addVmParams = new AddVmParameters(getParameters().getVmStaticData());
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
                log.error("Failed to copy field '{}' of new version to VM '{}' ({}): {}",
                        srcFld.getName(),
                        source.getName(),
                        source.getId(),
                        exp.getMessage());
                log.debug("Exception", exp);
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
        if (getVmId() != null) {
            return Collections.singletonMap(getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }

        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        // take shared lock on latest template, since we will add vm from it
        if (getVmTemplateId() != null) {
            return Collections.singletonMap(getVmTemplateId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
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
