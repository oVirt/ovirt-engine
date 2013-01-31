package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.utils.WipeAfterDeleteUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddDiskCommand<T extends AddDiskParameters> extends AbstractDiskVmCommand<T>
        implements QuotaStorageDependent {

    private static final long serialVersionUID = 4499428315430159917L;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddDiskCommand(Guid commandId) {
        super(commandId);
    }

    public AddDiskCommand(T parameters) {
        super(parameters);
        parameters.getDiskInfo().setId(Guid.NewGuid());
        parameters.setEntityId(parameters.getDiskInfo().getId());
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = isVmExist() && acquireLockInternal();
        VM vm = getVm();

        if (returnValue && vm != null) {
            // if user sent drive check that its not in use
            returnValue = isDiskCanBeAddedToVm(getParameters().getDiskInfo()) &&
                    isDiskPassPciAndIdeLimit(getParameters().getDiskInfo());
        }

        if (returnValue && DiskStorageType.IMAGE == getParameters().getDiskInfo().getDiskStorageType()) {
            returnValue = checkIfImageDiskCanBeAdded(vm);
        }

        if (returnValue && DiskStorageType.LUN == getParameters().getDiskInfo().getDiskStorageType()) {
            returnValue = checkIfLunDiskCanBeAdded();
        }

        return returnValue;
    }

    protected boolean checkIfLunDiskCanBeAdded() {
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

        return true;
    }

    private boolean checkIfImageDiskCanBeAdded(VM vm) {
        boolean returnValue;

        // vm agnostic checks
        returnValue =
                validate(new StorageDomainValidator(getStorageDomain()).isDomainExistAndActive()) &&
                checkImageConfiguration() &&
                hasFreeSpace(getStorageDomain()) &&
                checkExceedingMaxBlockDiskSize() &&
                canAddShareableDisk();

        if (returnValue && vm != null) {
            storage_pool sp = getStoragePool(); // Note this is done according to the VM's spId.
            returnValue =
                    validate(new StoragePoolValidator(sp).isUp()) &&
                    isStoragePoolMatching(vm) &&
                    performImagesChecks(vm.getStoragePoolId()) &&
                    validate(getSnapshotValidator().vmNotDuringSnapshot(vm.getId())) &&
                    validate(getSnapshotValidator().vmNotInPreview(vm.getId())) &&
                    validate(new VmValidator(vm).vmNotLocked());
        }

        return returnValue;
    }

    private boolean canAddShareableDisk() {
        if (getParameters().getDiskInfo().isShareable()) {
            if (!Config.<Boolean> GetValue(ConfigValues.ShareableDiskEnabled,
                    getStoragePool().getcompatibility_version().getValue())) {
                return failCanDoAction(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
            } else if (!isVolumeFormatSupportedForShareable(((DiskImage) getParameters().getDiskInfo()).getvolume_format())) {
                return failCanDoAction(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
            }
        }
        return true;
    }

    private boolean checkExceedingMaxBlockDiskSize() {
        if (isExceedMaxBlockDiskSize()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED);
            getReturnValue().getCanDoActionMessages().add(
                    String.format("$max_disk_size %1$s", Config.<Integer> GetValue(ConfigValues.MaxBlockDiskSize)));
            return false;
        }
        return true;
    }

    private boolean isStoragePoolMatching(VM vm) {
        if (getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
            getStorageDomainId().getValue(), vm.getStoragePoolId())) == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_OF_VM_NOT_MATCH);
        }
        return true;
    }

    protected SnapshotsValidator getSnapshotValidator() {
        return new SnapshotsValidator();
    }

    /** Checks if the image's configuration is legal */
    protected boolean checkImageConfiguration() {
        return ImagesHandler.CheckImageConfiguration(
                getStorageDomain().getStorageStaticData(),
                getDiskImageInfo(),
                getReturnValue().getCanDoActionMessages());
    }

    protected boolean performImagesChecks(Guid spId) {
        return ImagesHandler.PerformImagesChecks(
                getReturnValue().getCanDoActionMessages(),
                spId,
                getStorageDomainId().getValue(),
                false,
                true,
                false,
                false,
                false,
                true,
                Collections.<Disk> emptyList());
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

    private boolean hasFreeSpace(storage_domains storageDomain) {
        if (getDiskImageInfo().getvolume_type() == VolumeType.Preallocated) {
            return doesStorageDomainhaveSpaceForRequest(storageDomain);
        }
        return isStorageDomainWithinThresholds(storageDomain);
    }

    protected boolean doesStorageDomainhaveSpaceForRequest(storage_domains storageDomain) {
        return validate(new StorageDomainValidator(storageDomain).isDomainHasSpaceForRequest(getDiskImageInfo().getSizeInGigabytes()));
    }

    protected boolean isStorageDomainWithinThresholds(storage_domains storageDomain) {
        return validate(new StorageDomainValidator(storageDomain).isDomainWithinThresholds());
    }

    /** @return The disk from the parameters, cast to a {@link DiskImage} */
    private DiskImage getDiskImageInfo() {
        return (DiskImage) getParameters().getDiskInfo();
    }

    private boolean isExceedMaxBlockDiskSize() {
        StorageType storageType = getStorageDomain().getstorage_type();
        boolean isBlockStorageDomain = storageType == StorageType.ISCSI || storageType == StorageType.FCP;
        boolean isRequestedLargerThanMaxSize = getRequestDiskSpace() > Config.<Integer> GetValue(ConfigValues.MaxBlockDiskSize);

        return isBlockStorageDomain && isRequestedLargerThanMaxSize;
    }

    /**
     * @return The StorageDomainStaticDAO
     */
    protected StorageDomainStaticDAO getStorageDomainStaticDao() {
        return DbFacade.getInstance().getStorageDomainStaticDao();
    }

    @Override
    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    protected BaseDiskDao getBaseDiskDao() {
        return DbFacade.getInstance().getBaseDiskDao();
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
                return ((DiskImage) disk).getstorage_ids().get(0);
            }
        }
        return Guid.Empty;
    }

    @Override
    public NGuid getStorageDomainId() {
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
                            VmDeviceType.DISK,
                            VmDeviceType.DISK,
                            null,
                            getVm().getStatus() == VMStatus.Down,
                            false);
                }
                return null;
            }
        });
        getReturnValue().setActionReturnValue(getParameters().getDiskInfo().getId());
        setSucceeded(true);
    }

    private void createDiskBasedOnImage() {
        if(!getParameters().getDiskInfo().isWipeAfterDeleteSet()) {
            StorageType storageType = getStorageDomain().getstorage_type();
            getParameters().getDiskInfo().setWipeAfterDelete(WipeAfterDeleteUtils.getDefaultWipeAfterDeleteFlag(storageType));
        }
        // create from blank template, create new vm snapshot id
        AddImageFromScratchParameters parameters =
                new AddImageFromScratchParameters(Guid.Empty,
                        getParameters().getVmId(),
                        getDiskImageInfo());
        parameters.setQuotaId(getQuotaId());
        parameters.setDiskAlias(getDiskAlias());
        parameters.setStorageDomainId(getStorageDomainId().getValue());
        parameters.setParentCommand(VdcActionType.AddDisk);
        parameters.setEntityId(getParameters().getEntityId());
        parameters.setStoragePoolId(getStorageDomain().getstorage_pool_id().getValue());
        getParameters().getImagesParameters().add(parameters);
        parameters.setParentParameters(getParameters());
        if (getVm() != null) {
            setVmSnapshotIdForDisk(parameters);
            getCompensationContext().snapshotNewEntity(VmDeviceUtils.addManagedDevice(new VmDeviceId(getParameters().getDiskInfo()
                    .getId(),
                    getVmId()),
                    VmDeviceType.DISK,
                    VmDeviceType.DISK,
                    null,
                    getVm().getStatus() == VMStatus.Down,
                    false));
            getCompensationContext().stateChanged();
        }
        VdcReturnValueBase tmpRetValue =
                Backend.getInstance().runInternalAction(VdcActionType.AddImageFromScratch,
                        parameters,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext(), getLock()));
        // Setting lock to null because the lock is released in the child command
        setLock(null);
        getReturnValue().getTaskIdList().addAll(tmpRetValue.getInternalTaskIdList());
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
        permissions perms =
                new permissions(getCurrentUser().getUserId(),
                        PredefinedRoles.DISK_OPERATOR.getId(),
                        disk.getId(),
                        VdcObjectType.Disk);
        MultiLevelAdministrationHandler.addPermission(perms);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getExecuteAuditLogTypeValue(getSucceeded());

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
    protected Map<String, String> getExclusiveLocks() {
        if (getParameters().getDiskInfo().isBoot() && getParameters().getVmId() != null
                && !Guid.Empty.equals(getParameters().getVmId())) {
            return Collections.singletonMap(getParameters().getVmId().toString(), LockingGroup.VM_DISK_BOOT.name());
        }
        return null;
    }

    @Override
    protected Map<String, String> getSharedLocks() {
        if (getParameters().getVmId() != null && !Guid.Empty.equals(getParameters().getVmId())) {
            return Collections.singletonMap(getParameters().getVmId().toString(), LockingGroup.VM.name());
        }
        return null;
    }

    private Guid getQuotaId() {
        if (getParameters().getDiskInfo() != null
                && DiskStorageType.IMAGE == getParameters().getDiskInfo().getDiskStorageType()) {
            return ((DiskImage) getParameters().getDiskInfo()).getQuotaId();
        }
        return null;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        if (getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.IMAGE) {
            list.add(new QuotaStorageConsumptionParameter(
                getQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getStorageDomainId().getValue(),
                getRequestDiskSpace()));
        }
        return list;
    }
}
