package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class updates VM to the required template version for stateless VMs
 */
@InternalCommandAttribute
public class UpdateVmVersionCommand<T extends UpdateVmVersionParameters> extends VmCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(UpdateVmVersionCommand.class);

    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private DiskProfileDao diskProfileDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected UpdateVmVersionCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateVmVersionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, getParameters().getVmId()));

        if (getVm() != null) {
            if (getParameters().getNewTemplateVersion() != null) {
                setVmTemplate(vmTemplateDao.get(getParameters().getNewTemplateVersion()));
            } else {
                setVmTemplate(vmTemplateDao.getTemplateWithLatestVersionInChain(getVm().getVmtGuid()));
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
        vmHandler.updateVmInitFromDB(getVmTemplate(), false);
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

            ActionReturnValue result = runInternalActionWithTasksContext(
                    ActionType.RemoveVmFromPool,
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

        ActionReturnValue result = runInternalActionWithTasksContext(
                ActionType.RemoveVm,
                buildRemoveVmParameters(),
                getLock());

        if (!result.getSucceeded()) {
            log.error("Could not remove vm '{}' ({})", getVm().getName(), getVmId());
            return;
        }

        getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
        if (getTaskIdList().isEmpty()) {
            endVmCommand();
        }
        setSucceeded(true);
    }

    private Guid getIdOfDiskOperator() {
        List<Disk> diskIds = diskDao.getAllForVm(getVmId());
        if (diskIds.isEmpty()) {
            return null;
        }

        List<Permission> perms = permissionDao.getAllForRoleAndObject(PredefinedRoles.DISK_OPERATOR.getId(), diskIds.iterator().next().getId());
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
        runInternalAction(ActionType.AddVm,
                buildAddVmParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext(), getLock()));
    }

    private AddVmParameters buildAddVmParameters() {
        AddVmParameters addVmParams = new AddVmParameters(getParameters().getVmStaticData());
        addVmParams.setPoolId(getParameters().getVmPoolId());
        addVmParams.setDiskInfoDestinationMap(buildDiskInfoDestinationMap());
        addVmParams.setConsoleEnabled(deviceExists(VmDeviceGeneralType.CONSOLE));
        addVmParams.setTpmEnabled(deviceExists(VmDeviceGeneralType.TPM));
        addVmParams.setSoundDeviceEnabled(deviceExists(VmDeviceGeneralType.SOUND));
        addVmParams.setVirtioScsiEnabled(deviceExists(VmDeviceGeneralType.CONTROLLER, VmDeviceType.VIRTIOSCSI));

        List<VmWatchdog> watchdogs = runInternalQuery(QueryType.GetWatchdog,
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

    private Map<Guid, DiskImage> buildDiskInfoDestinationMap() {
        Map<Guid, DiskImage> destinationMap = new HashMap<>();

        if (getParameters().getVmPoolId() == null) {
            return destinationMap;
        }
        VmPool vmPool = vmPoolDao.get(getParameters().getVmPoolId());
        if (vmPool == null || !vmPool.isAutoStorageSelect()) {
            return destinationMap;
        }

        List<Disk> templateDisks = diskDao.getAllForVm(getParameters().getVmStaticData().getVmtGuid());
        Map<Guid, List<Guid>> diskToProfileMap = templateDisks.stream()
                .collect(Collectors.toMap(Disk::getId, disk -> ((DiskImage) disk).getDiskProfileIds()));
        Map<Guid, List<Guid>> diskToStorageIds = templateDisks.stream()
                .collect(Collectors.toMap(Disk::getId, disk -> ((DiskImage) disk).getStorageIds()));
        Map<Guid, Long> targetDomainsSize = diskToStorageIds.values().stream()
                .flatMap(List::stream)
                .distinct()
                .map(storageDomainDao::get)
                .collect(Collectors.toMap(StorageDomain::getId, StorageDomain::getAvailableDiskSizeInBytes));

        for (Disk disk : templateDisks) {
            DiskImage diskImage = (DiskImage) disk;
            Guid storageId =
                    findAvailableStorageDomain(targetDomainsSize, disk.getSize(), diskToStorageIds.get(disk.getId()));
            diskToProfileMap.get(disk.getId()).stream()
                    .map(profileId -> diskProfileDao.get(profileId))
                    .filter(profile -> profile.getStorageDomainId().equals(storageId))
                    .findFirst()
                    .ifPresent(profile -> diskImage.setDiskProfileId(profile.getId()));
            // Set target domain
            ArrayList<Guid> storageIds = new ArrayList<>();
            storageIds.add(storageId);
            diskImage.setStorageIds(storageIds);
            // Set volume format.
            // Note that the disks of VMs in a pool are essentially snapshots of the template's disks.
            // therefore when creating the VM's disks, the image parameters are overridden anyway.
            // We were required to change only the VolumeFormat here for passing the AddVMCommand's
            // validation
            if (diskImage.getDiskStorageType() == DiskStorageType.CINDER) {
                diskImage.setVolumeFormat(VolumeFormat.RAW);
            } else {
                diskImage.setVolumeFormat(VolumeFormat.COW);
            }

            destinationMap.put(disk.getId(), diskImage);
        }

        return destinationMap;
    }

    private Guid findAvailableStorageDomain(
            Map<Guid, Long> targetDomainsSize,
            long diskSize,
            List<Guid> storageIds) {

        Guid dest = storageIds.stream()
                .max(Comparator.comparingLong(targetDomainsSize::get))
                .orElse(storageIds.get(0));
        long destSize = targetDomainsSize.get(dest);
        targetDomainsSize.put(dest, destSize - diskSize);
        return dest;
    }

    private void loadVmPayload(AddVmParameters addVmParams) {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdAndType(getVmTemplateId(), VmDeviceGeneralType.DISK);
        for (VmDevice vmDevice : vmDevices) {
            if (VmPayload.isPayload(vmDevice.getSpecParams())) {
                addVmParams.setVmPayload(new VmPayload(vmDevice));
                return;
            }
        }
    }

    private boolean deviceExists(VmDeviceGeneralType generalType, VmDeviceType deviceType) {
        return !vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                getVmTemplateId(), generalType, deviceType).isEmpty();
    }

    private boolean deviceExists(VmDeviceGeneralType generalType) {
        return !vmDeviceDao.getVmDeviceByVmIdAndType(getVmTemplateId(), generalType).isEmpty();
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
