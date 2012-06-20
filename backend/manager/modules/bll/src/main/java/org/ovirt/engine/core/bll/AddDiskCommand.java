package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
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
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@CustomLogFields({ @CustomLogField("DiskAlias") })
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddDiskCommand<T extends AddDiskParameters> extends AbstractDiskVmCommand<T> {

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
        if (returnValue && (vm != null && vm.getstatus() != VMStatus.Down)) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        } else {
            // if user sent drive check that its not in use
            returnValue = returnValue && (vm == null || isDiskCanBeAddedToVm(getParameters().getDiskInfo()));
            if (returnValue && DiskStorageType.IMAGE == getParameters().getDiskInfo().getDiskStorageType()) {
                returnValue = checkIfImageDiskCanBeAdded(vm);
            }
            if (returnValue && DiskStorageType.LUN == getParameters().getDiskInfo().getDiskStorageType()) {
                returnValue = checkIfLunDiskCanBeAdded();
            }
            if (returnValue && getParameters().getDiskInfo().isShareable()
                    && (getParameters().getDiskInfo().isBoot())) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
            }
            if (returnValue
                    && getParameters().getDiskInfo().isShareable()
                    && !isVersionSupportedForShareable(getParameters().getDiskInfo(),
                            getStoragePool().getcompatibility_version().getValue())) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
            }
        }
        return returnValue;
    }

    private boolean checkIfLunDiskCanBeAdded() {
        boolean returnValue = true;
        if (getDiskLunMapDao().getDiskIdByLunId(((LunDisk) getParameters().getDiskInfo()).getLun().getLUN_id()) != null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_IS_ALREADY_IN_USE);
        }
        return returnValue;
    }

    private boolean checkIfImageDiskCanBeAdded(VM vm) {
        boolean returnValue;
        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        returnValue = validator.isDomainExistAndActive(getReturnValue().getCanDoActionMessages());
        if (returnValue && vm != null && getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
                getStorageDomainId().getValue(), vm.getstorage_pool_id())) == null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }
        returnValue = returnValue
                && checkImageConfiguration()
                && (vm == null || isDiskPassPCIAndIDELimit(getParameters().getDiskInfo()));
        returnValue = returnValue && (vm == null || performImagesChecks(vm));

        if (returnValue && !hasFreeSpace(getStorageDomain())) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
        }
        if (returnValue && isExceedMaxBlockDiskSize()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED);
            getReturnValue().getCanDoActionMessages().add(
                    String.format("$max_disk_size %1$s", Config.<Integer> GetValue(ConfigValues.MaxBlockDiskSize)));
            returnValue = false;
        }
        return returnValue && (vm == null || validate(new SnapshotsValidator().vmNotDuringSnapshot(vm.getId())));
    }

    /** Checks if the iamge's configuration is legal */
    protected boolean checkImageConfiguration() {
        return ImagesHandler.CheckImageConfiguration(
                getStorageDomain().getStorageStaticData(),
                getDiskImageInfo(),
                getReturnValue().getCanDoActionMessages());
    }

    protected boolean performImagesChecks(VM vm) {
        return ImagesHandler.PerformImagesChecks(vm,
                getReturnValue().getCanDoActionMessages(),
                vm.getstorage_pool_id(),
                getStorageDomainId().getValue(),
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                Collections.emptyList());
    }

    private void setAllowSnapshotForDisk() {
        if (getParameters().getDiskInfo().isShareable()) {
            getParameters().getDiskInfo().setAllowSnapshot(false);
        } else {
            getParameters().getDiskInfo().setAllowSnapshot(true);
        }
    }

    private long getRequestDiskSpace() {
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
        return isStorageDomainBelowThresholds(storageDomain);
    }

    protected boolean doesStorageDomainhaveSpaceForRequest(storage_domains storageDomain) {
        return StorageDomainSpaceChecker.hasSpaceForRequest(storageDomain, getDiskImageInfo().getSizeInGigabytes());
    }

    protected boolean isStorageDomainBelowThresholds(storage_domains storageDomain) {
        return StorageDomainSpaceChecker.isBelowThresholds(storageDomain);
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
        return DbFacade.getInstance().getStorageDomainStaticDAO();
    }

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
     * @return The ID of the storage domain where the VM's disks reside.
     */
    private Guid getDisksStorageDomainId() {
        return ((DiskImage) getVm().getDiskMap().values().iterator().next()).getstorage_ids().get(0);
    }

    @Override
    public NGuid getStorageDomainId() {
        Guid storageDomainId = getParameters().getStorageDomainId();
        if (Guid.Empty.equals(storageDomainId) &&
                getVm() != null &&
                getVm().getDiskMap() != null &&
                !getVm().getDiskMap().isEmpty()) {
            return getDisksStorageDomainId();
        }

        return storageDomainId == null ? Guid.Empty : storageDomainId;
    }

    @Override
    protected boolean validateQuota() {
        if (getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.IMAGE) {
            // Set default quota id if storage pool enforcement is disabled.
            getParameters().setQuotaId(QuotaHelper.getInstance()
                    .getQuotaIdToConsume(getDiskImageInfo().getQuotaId(),
                            getStoragePool()));

            return (QuotaManager.validateStorageQuota(getStorageDomainId().getValue(),
                    getParameters().getQuotaId(),
                    getStoragePool().getQuotaEnforcementType(),
                    new Double(getRequestDiskSpace()),
                    getCommandId(),
                    getReturnValue().getCanDoActionMessages()));
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> listPermissionSubjects;
        if (getParameters().getVmId() == null) {
            listPermissionSubjects = new ArrayList<PermissionSubject>();
        } else {
            listPermissionSubjects = super.getPermissionCheckSubjects();
        }
        listPermissionSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                ActionGroup.CREATE_DISK));
        listPermissionSubjects =
                QuotaHelper.getInstance().addQuotaPermissionSubject(listPermissionSubjects,
                        getStoragePool(),
                        getQuotaId());
        return listPermissionSubjects;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected void ExecuteVmCommand() {
        ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());
        if (DiskStorageType.IMAGE == getParameters().getDiskInfo().getDiskStorageType()) {
            setAllowSnapshotForDisk();
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
                            true,
                            false);
                }
                return null;
            }
        });
        setSucceeded(true);
    }

    private void createDiskBasedOnImage() {
        // create from blank template, create new vm snapshot id
        AddImageFromScratchParameters parameters =
                new AddImageFromScratchParameters(Guid.Empty,
                        getParameters().getVmId(),
                        getDiskImageInfo());
        parameters.setQuotaId(getParameters().getQuotaId());
        parameters.setStorageDomainId(getStorageDomainId().getValue());
        parameters.setParentCommand(VdcActionType.AddDisk);
        parameters.setEntityId(getParameters().getEntityId());
        parameters.setStoragePoolId(getStorageDomain().getstorage_pool_id().getValue());
        getParameters().getImagesParameters().add(parameters);
        parameters.setParentParemeters(getParameters());
        if (getVm() != null) {
            setVmSnapshotIdForDisk(parameters);
            getCompensationContext().snapshotNewEntity(VmDeviceUtils.addManagedDevice(new VmDeviceId(getParameters().getDiskInfo()
                    .getId(),
                    getVmId()),
                    VmDeviceType.DISK,
                    VmDeviceType.DISK,
                    null,
                    true,
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
            return getSucceeded() ? AuditLogType.USER_ADD_DISK_TO_VM : AuditLogType.USER_FAILED_ADD_DISK_TO_VM;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_SUCCESS
                    : AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_FAILURE;
        }
    }

    @Override
    protected void removeQuotaCommandLeftOver() {
        QuotaManager.removeStorageDeltaQuotaCommand(getQuotaId(),
                getStorageDomainId().getValue(),
                getStoragePool().getQuotaEnforcementType(),
                getCommandId());
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
    protected Map<Guid, String> getExclusiveLocks() {
        if (getParameters().getDiskInfo().isBoot() && getParameters().getVmId() != null
                && !Guid.Empty.equals(getParameters().getVmId())) {
            return Collections.singletonMap(getParameters().getVmId(), LockingGroup.VM_DISK_BOOT.name());
        }
        return null;
    }

    @Override
    protected Map<Guid, String> getSharedLocks() {
        if (getParameters().getVmId() != null && !Guid.Empty.equals(getParameters().getVmId())) {
            return Collections.singletonMap(getParameters().getVmId(), LockingGroup.VM.name());
        }
        return null;
    }
}
