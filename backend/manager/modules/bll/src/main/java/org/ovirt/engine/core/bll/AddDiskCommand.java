package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.utils.WipeAfterDeleteUtils;
import org.ovirt.engine.core.bll.validator.DiskValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddDiskCommand<T extends AddDiskParameters> extends AbstractDiskVmCommand<T>
        implements QuotaStorageDependent {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddDiskCommand(Guid commandId) {
        super(commandId);
    }

    public AddDiskCommand(T parameters) {
        this(parameters, null);
    }

    public AddDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected boolean canDoAction() {
        if (!isVmExist() || !acquireLockInternal()) {
            return false;
        }

        Disk diskInfo = getParameters().getDiskInfo();
        if (diskInfo.getDiskStorageType() == DiskStorageType.IMAGE) {
            getDiskImageInfo().setDiskSnapshot(false);
        }

        VM vm = getVm();

        if (vm != null) {
            if (!canRunActionOnNonManagedVm()) {
                return false;
            }

            updateDisksFromDb();
            // if user sent drive check that its not in use
            if (!isDiskCanBeAddedToVm(getParameters().getDiskInfo(), vm) ||
                    !isDiskPassPciAndIdeLimit(getParameters().getDiskInfo())) {
                return false;
            }
        }
        else if (Boolean.TRUE.equals(getParameters().getPlugDiskToVm())) {
            return failCanDoAction(VdcBllMessages.CANNOT_ADD_FLOATING_DISK_WITH_PLUG_VM_SET);
        }

        DiskValidator diskValidator = getDiskValidator(getParameters().getDiskInfo());
        if (!validate(diskValidator.isReadOnlyPropertyCompatibleWithInterface())) {
            return false;
        }

        if (!validate(diskValidator.areBootableAndSharableCompatibleWithDisk())) {
            return false;
        }

        if (DiskStorageType.IMAGE == getParameters().getDiskInfo().getDiskStorageType()) {
            return checkIfImageDiskCanBeAdded(vm, diskValidator);
        }

        if (DiskStorageType.LUN == getParameters().getDiskInfo().getDiskStorageType()) {
            return checkIfLunDiskCanBeAdded(diskValidator);
        }

        return true;
    }

    protected boolean checkIfLunDiskCanBeAdded(DiskValidator diskValidator) {
        LUNs lun = ((LunDisk) getParameters().getDiskInfo()).getLun();

        switch (lun.getLunType()) {
        case UNKNOWN:
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_HAS_NO_VALID_TYPE);
        case ISCSI:
            if (lun.getLunConnections() == null || lun.getLunConnections().isEmpty()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
            }

            for (StorageServerConnections conn : lun.getLunConnections()) {
                if (StringUtils.isEmpty(conn.getiqn()) || StringUtils.isEmpty(conn.getconnection())
                        || StringUtils.isEmpty(conn.getport())) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
                }
            }
            break;
        }

        if (getDiskLunMapDao().getDiskIdByLunId(lun.getLUN_id()) != null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_IS_ALREADY_IN_USE);
        }

        if (getVm() != null && !(isVmNotLocked() && isVmNotInPreviewSnapshot())) {
            return false;
        }

        if (!validate(diskValidator.isVirtIoScsiValid(getVm()))) {
            return false;
        }

        if (!validate(diskValidator.isDiskInterfaceSupported(getVm()))) {
            return false;
        }

        return true;
    }

    protected boolean checkIfImageDiskCanBeAdded(VM vm, DiskValidator diskValidator) {
        if (Guid.Empty.equals(getStorageDomainId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_SPECIFIED);
        }

        boolean returnValue;
        StorageDomainValidator storageDomainValidator = createStorageDomainValidator();
        // vm agnostic checks
        returnValue =
                validate(storageDomainValidator.isDomainExistAndActive()) &&
                !isShareableDiskOnGlusterDomain() &&
                checkImageConfiguration() &&
                validate(storageDomainValidator.hasSpaceForNewDisk(getDiskImageInfo())) &&
                validate(storageDomainValidator.isDomainWithinThresholds()) &&
                checkExceedingMaxBlockDiskSize() &&
                canAddShareableDisk() &&
                validate(diskValidator.isVirtIoScsiValid(vm)) &&
                validate(diskValidator.isDiskInterfaceSupported(getVm()));

        if (returnValue && vm != null) {
            StoragePool sp = getStoragePool(); // Note this is done according to the VM's spId.
            returnValue =
                    validate(new StoragePoolValidator(sp).isUp()) &&
                    isStoragePoolMatching(vm) &&
                    isVmNotLocked() &&
                    isVmNotInPreviewSnapshot();
        }

        return returnValue;
    }

    private boolean isShareableDiskOnGlusterDomain() {
        if (getParameters().getDiskInfo().isShareable() && getStorageDomain().getStorageType() == StorageType.GLUSTERFS) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
            return true;
        }

        return false;
    }

    private boolean canAddShareableDisk() {
        if (getParameters().getDiskInfo().isShareable()) {
            if (!Config.<Boolean> getValue(ConfigValues.ShareableDiskEnabled,
                    getStoragePool().getcompatibility_version().getValue())) {
                return failCanDoAction(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
            } else if (!isVolumeFormatSupportedForShareable(((DiskImage) getParameters().getDiskInfo()).getVolumeFormat())) {
                return failCanDoAction(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
            }
        }
        return true;
    }

    private boolean checkExceedingMaxBlockDiskSize() {
        if (isExceedMaxBlockDiskSize()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED);
            getReturnValue().getCanDoActionMessages().add(
                    String.format("$max_disk_size %1$s", Config.<Integer> getValue(ConfigValues.MaxBlockDiskSize)));
            return false;
        }
        return true;
    }

    private boolean isStoragePoolMatching(VM vm) {
        if (getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
            getStorageDomainId(), vm.getStoragePoolId())) == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_OF_VM_NOT_MATCH);
        }
        return true;
    }


    /** Checks if the image's configuration is legal */
    protected boolean checkImageConfiguration() {
        return ImagesHandler.checkImageConfiguration(
                getStorageDomain().getStorageStaticData(),
                getDiskImageInfo(),
                getReturnValue().getCanDoActionMessages());
    }

    private double getRequestDiskSpace() {
        if (getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.IMAGE) {
            return getDiskImageInfo().getSizeInGigabytes();
        }
        return 0;
    }

    @Override
    protected boolean isVmExist() {
        return getParameters().getVmId() == null || Guid.Empty.equals(getParameters().getVmId()) || super.isVmExist();
    }

    /** @return The disk from the parameters, cast to a {@link DiskImage} */
    private DiskImage getDiskImageInfo() {
        return (DiskImage) getParameters().getDiskInfo();
    }

    private boolean isExceedMaxBlockDiskSize() {
        if (getStorageDomain().getStorageType().isBlockDomain()) {
            return getRequestDiskSpace() > Config.<Integer> getValue(ConfigValues.MaxBlockDiskSize);
        }

        return false;
    }

    @Override
    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    protected DiskLunMapDao getDiskLunMapDao() {
        return DbFacade.getInstance().getDiskLunMapDao();
    }

    /**
     * @return The id of the storage domain where the first encountered VM image disk reside, if the vm doesn't have no
     *         image disks then Guid.Empty will be returned.
     */
    private Guid getDisksStorageDomainId() {
        for (Disk disk : getVm().getDiskMap().values()) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                return ((DiskImage) disk).getStorageIds().get(0);
            }
        }
        return Guid.Empty;
    }

    @Override
    public Guid getStorageDomainId() {
        if (super.getStorageDomainId() == null) {
            Guid storageDomainId = getParameters().getStorageDomainId();
            if (Guid.Empty.equals(storageDomainId) &&
                    getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.IMAGE &&
                    getVm() != null) {

                updateDisksFromDb();
                storageDomainId = getDisksStorageDomainId();

                // this set is done so that in case we will execute an async task
                // the correct storage domain id will be set during the call to the end methods
                getParameters().setStorageDomainId(storageDomainId);
            } else if (storageDomainId == null) {
                storageDomainId = Guid.Empty;

                // this set is done so that in case we will execute an async task
                // the correct storage domain id will be set during the call to the end methods
                getParameters().setStorageDomainId(storageDomainId);
            }
            setStorageDomainId(storageDomainId);
            return storageDomainId;
        }
        return super.getStorageDomainId();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> listPermissionSubjects;
        if (getParameters().getVmId() == null || Guid.Empty.equals(getParameters().getVmId())) {
            listPermissionSubjects = new ArrayList<PermissionSubject>();
        } else {
            listPermissionSubjects = super.getPermissionCheckSubjects();
        }
        // If the storage domain ID is empty/null, it means we are going to create an external disk
        // In order to do that we need CREATE_DISK permissions on System level
        if (getParameters().getStorageDomainId() == null || Guid.Empty.equals(getParameters().getStorageDomainId())) {
            listPermissionSubjects.add(new PermissionSubject(Guid.SYSTEM,
                    VdcObjectType.System,
                    ActionGroup.CREATE_DISK));
            if (getParameters().getDiskInfo().getSgio() == ScsiGenericIO.UNFILTERED) {
                listPermissionSubjects.add(new PermissionSubject(Guid.SYSTEM,
                        VdcObjectType.System,
                        ActionGroup.CONFIGURE_SCSI_GENERIC_IO));
            }
        } else {
            listPermissionSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                    VdcObjectType.Storage,
                    ActionGroup.CREATE_DISK));
        }
        return listPermissionSubjects;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected void executeVmCommand() {
        getParameters().getDiskInfo().setId(Guid.newGuid());
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getDiskInfo().getId()));
        ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());
        if (DiskStorageType.IMAGE == getParameters().getDiskInfo().getDiskStorageType()) {
            createDiskBasedOnImage();
        } else {
            createDiskBasedOnLun();
        }
    }

    private void createDiskBasedOnLun() {
        final LUNs lun = ((LunDisk) getParameters().getDiskInfo()).getLun();
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                StorageDomainCommandBase.proceedLUNInDb(lun, lun.getLunType());
                getBaseDiskDao().save(getParameters().getDiskInfo());
                getDiskLunMapDao().save(new DiskLunMap(getParameters().getDiskInfo().getId(), lun.getLUN_id()));
                if (getVm() != null) {
                    VmDeviceUtils.addManagedDevice(new VmDeviceId(getParameters().getDiskInfo().getId(), getVmId()),
                            VmDeviceGeneralType.DISK,
                            VmDeviceType.DISK,
                            null,
                            shouldDiskBePlugged(),
                            Boolean.TRUE.equals(getParameters().getDiskInfo().getReadOnly()),
                            null);
                }
                return null;
            }
        });
        getReturnValue().setActionReturnValue(getParameters().getDiskInfo().getId());
        plugDiskToVmIfNeeded();
        setSucceeded(true);
    }

    private boolean shouldDiskBePlugged() {
        return getVm().getStatus() == VMStatus.Down && !Boolean.FALSE.equals(getParameters().getPlugDiskToVm());
    }

    private void createDiskBasedOnImage() {
        if(!getParameters().getDiskInfo().isWipeAfterDeleteSet()) {
            StorageType storageType = getStorageDomain().getStorageType();
            getParameters().getDiskInfo().setWipeAfterDelete(WipeAfterDeleteUtils.getDefaultWipeAfterDeleteFlag(storageType));
        }
        // create from blank template, create new vm snapshot id
        AddImageFromScratchParameters parameters =
                new AddImageFromScratchParameters(Guid.Empty,
                        getParameters().getVmId(),
                        getDiskImageInfo());
        parameters.setQuotaId(getQuotaId());
        parameters.setDiskProfileId(getDiskImageInfo().getDiskProfileId());
        parameters.setDiskAlias(getDiskAlias());
        parameters.setShouldRemainIllegalOnFailedExecution(getParameters().isShouldRemainIllegalOnFailedExecution());
        parameters.setStorageDomainId(getStorageDomainId());

        if (isExecutedAsChildCommand()) {
            parameters.setParentCommand(getParameters().getParentCommand());
            parameters.setParentParameters(getParameters().getParentParameters());
        } else {
            parameters.setParentCommand(VdcActionType.AddDisk);
            parameters.setParentParameters(getParameters());
        }

        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setStoragePoolId(getStorageDomain().getStoragePoolId());
        if (getVm() != null) {
            setVmSnapshotIdForDisk(parameters);
            getCompensationContext().snapshotNewEntity(VmDeviceUtils.addManagedDevice(new VmDeviceId(getParameters().getDiskInfo()
                    .getId(),
                    getVmId()),
                    VmDeviceGeneralType.DISK,
                    VmDeviceType.DISK,
                    null,
                    shouldDiskBePlugged(),
                    Boolean.TRUE.equals(getParameters().getDiskInfo().getReadOnly()),
                    null));
            getCompensationContext().stateChanged();
        }
        VdcReturnValueBase tmpRetValue =
                runInternalActionWithTasksContext(VdcActionType.AddImageFromScratch,
                        parameters,
                        getLock());
        // Setting lock to null because the lock is released in the child command
        setLock(null);
        ArrayList<Guid> taskList = isExecutedAsChildCommand() ? getReturnValue().getInternalVdsmTaskIdList() : getReturnValue().getVdsmTaskIdList();
        taskList.addAll(tmpRetValue.getInternalVdsmTaskIdList());
        if (tmpRetValue.getActionReturnValue() != null) {
            DiskImage diskImage = (DiskImage) tmpRetValue.getActionReturnValue();
            addDiskPermissions(diskImage);
            getReturnValue().setActionReturnValue(diskImage.getId());
        }
        getReturnValue().setFault(tmpRetValue.getFault());
        setSucceeded(tmpRetValue.getSucceeded());
    }

    /**
     * If disk is not allow to have snapshot no VM snapshot Id should be updated.
     * @param parameters
     */
    private void setVmSnapshotIdForDisk(AddImageFromScratchParameters parameters) {
        if (getParameters().getDiskInfo().isAllowSnapshot()) {
            parameters.setVmSnapshotId(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE));
        }
    }

    private void addDiskPermissions(Disk disk) {
        if (getCurrentUser() != null) {
            Permissions perms =
                    new Permissions(getCurrentUser().getId(),
                            PredefinedRoles.DISK_OPERATOR.getId(),
                            disk.getId(),
                            VdcObjectType.Disk);
            MultiLevelAdministrationHandler.addPermission(perms);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.IMAGE) {
                return getExecuteAuditLogTypeValue(getSucceeded());
            } else {
                return getEndSuccessAuditLogTypeValue(getSucceeded());
            }
        case END_SUCCESS:
            return getEndSuccessAuditLogTypeValue(getSucceeded());

        default:
            return AuditLogType.USER_ADD_DISK_FINISHED_FAILURE;
        }
    }

    private AuditLogType getExecuteAuditLogTypeValue(boolean successful) {
        boolean isVmNameExist = StringUtils.isNotEmpty(getVmName());
        if (successful) {
            if (isVmNameExist) {
                return AuditLogType.USER_ADD_DISK_TO_VM;
            } else {
                return AuditLogType.USER_ADD_DISK;
            }
        } else {
            if (isVmNameExist) {
                return AuditLogType.USER_FAILED_ADD_DISK_TO_VM;
            } else {
                return AuditLogType.USER_FAILED_ADD_DISK;
            }
        }
    }

    private AuditLogType getEndSuccessAuditLogTypeValue(boolean successful) {
        boolean isVmNameExist = StringUtils.isNotEmpty(getVmName());
        if (successful) {
            if (isVmNameExist) {
                return AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_SUCCESS;
            } else {
                return AuditLogType.USER_ADD_DISK_FINISHED_SUCCESS;
            }
        } else {
            if (isVmNameExist) {
                return AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_FAILURE;
            } else {
                return AuditLogType.USER_ADD_DISK_FINISHED_FAILURE;
            }
        }
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.AddImageFromScratch;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getParameters().getDiskInfo().isBoot() && getParameters().getVmId() != null
                && !Guid.Empty.equals(getParameters().getVmId())) {
            return Collections.singletonMap(getParameters().getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_BOOT, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (getParameters().getVmId() != null && !Guid.Empty.equals(getParameters().getVmId())) {
            return Collections.singletonMap(getParameters().getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    @Override
    protected void setLoggingForCommand() {
        setCommandShouldBeLogged(true);
    }

    private Guid getQuotaId() {
        if (getParameters().getDiskInfo() != null
                && DiskStorageType.IMAGE == getParameters().getDiskInfo().getDiskStorageType()) {
            Guid quotaId = ((DiskImage) getParameters().getDiskInfo()).getQuotaId();
            if (!Guid.Empty.equals(quotaId)) {
                return quotaId;
            }
        }
        return null;
    }

    @Override
    protected void endSuccessfully() {
        plugDiskToVmIfNeeded();
        super.endSuccessfully();
    }

    private void plugDiskToVmIfNeeded() {
        if (Boolean.TRUE.equals(getParameters().getPlugDiskToVm()) && getVm() != null &&  getVm().getStatus() != VMStatus.Down)    {
            HotPlugDiskToVmParameters params = new HotPlugDiskToVmParameters(getVmId(), getParameters().getDiskInfo().getId());
            params.setShouldBeLogged(false);
            VdcReturnValueBase returnValue = runInternalAction(VdcActionType.HotPlugDiskToVm, params);
            if (!returnValue.getSucceeded()) {
                AuditLogDirector.log(this, AuditLogType.USER_FAILED_HOTPLUG_DISK);
            }
        }
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        if (getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.IMAGE) {
            list.add(new QuotaStorageConsumptionParameter(
                getQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getStorageDomainId(),
                getRequestDiskSpace()));
        }
        return list;
    }

    protected StorageDomainValidator createStorageDomainValidator() {
        return new StorageDomainValidator(getStorageDomain());
    }
}
