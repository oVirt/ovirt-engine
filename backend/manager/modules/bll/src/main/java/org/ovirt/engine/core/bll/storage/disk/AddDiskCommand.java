package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddDiskCommand<T extends AddDiskParameters> extends AbstractDiskVmCommand<T>
        implements QuotaStorageDependent {

    private LUNs lunFromStorage;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddDiskCommand(Guid commandId) {
        super(commandId);
    }

    public AddDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected boolean validateInputs() {
        return super.validateInputs();
    }

    @Override
    protected boolean validate() {
        if (!isFloatingDisk() && !validate(new VmValidator(getVm()).isVmExists()) && !validateDiskVmData()) {
            return false;
        }

        Disk diskInfo = getParameters().getDiskInfo();
        if (diskInfo.getDiskStorageType() == DiskStorageType.IMAGE ||
                diskInfo.getDiskStorageType() == DiskStorageType.CINDER) {
            getDiskImageInfo().setDiskSnapshot(false);
        }

        VM vm = getVm();

        DiskValidator diskValidator = getDiskValidator(getParameters().getDiskInfo());
        if (vm != null) {
            if (!validateDiskVmData() || !canRunActionOnNonManagedVm()) {
                return false;
            }

            updateDisksFromDb();

            if (getDiskVmElement().isBoot() && !validate(diskValidator.isVmNotContainsBootDisk(vm))) {
                return false;
            }

            // if user sent drive check that its not in use
            if (!isDiskPassPciAndIdeLimit()) {
                return false;
            }
        }
        else if (Boolean.TRUE.equals(getParameters().getPlugDiskToVm())) {
            return failValidation(EngineMessage.CANNOT_ADD_FLOATING_DISK_WITH_PLUG_VM_SET);
        }

        if (!validate(diskValidator.isReadOnlyPropertyCompatibleWithInterface(getDiskVmElement()))) {
            return false;
        }

        if (DiskStorageType.IMAGE == getParameters().getDiskInfo().getDiskStorageType()) {
            if (!checkIfImageDiskCanBeAdded(vm, diskValidator)) {
                return false;
            }

            return setAndValidateDiskProfiles();
        }

        if (DiskStorageType.LUN == getParameters().getDiskInfo().getDiskStorageType()) {
            return checkIfLunDiskCanBeAdded(diskValidator);
        }

        if (DiskStorageType.CINDER == getParameters().getDiskInfo().getDiskStorageType()) {
            CinderDisk cinderDisk = (CinderDisk) getParameters().getDiskInfo();
            cinderDisk.setStorageIds(new ArrayList<>(Collections.singletonList(getStorageDomainId())));
            StorageDomainValidator storageDomainValidator = createStorageDomainValidator();
            CinderDisksValidator cinderDisksValidator = new CinderDisksValidator(cinderDisk);
            return validate(storageDomainValidator.isDomainExistAndActive()) &&
                    validate(cinderDisksValidator.validateCinderDiskLimits()) &&
                    validate(cinderDisksValidator.validateCinderVolumeTypesExist());
        }

        return true;
    }

    protected boolean checkIfLunDiskCanBeAdded(DiskValidator diskValidator) {
        LunDisk lunDisk = (LunDisk) getParameters().getDiskInfo();
        LUNs lun = lunDisk.getLun();

        switch (lun.getLunType()) {
        case UNKNOWN:
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_HAS_NO_VALID_TYPE);
        case ISCSI:
            if (lun.getLunConnections() == null || lun.getLunConnections().isEmpty()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
            }

            for (StorageServerConnections conn : lun.getLunConnections()) {
                if (StringUtils.isEmpty(conn.getIqn()) || StringUtils.isEmpty(conn.getConnection())
                        || StringUtils.isEmpty(conn.getPort())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
                }
            }
            break;
        default:
            break;
        }

        if (getDiskLunMapDao().getDiskIdByLunId(lun.getLUNId()) != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_IS_ALREADY_IN_USE);
        }

        if (getVm() != null && !(validate(new VmValidator(getVm()).vmNotLocked()) && isVmNotInPreviewSnapshot())) {
            return false;
        }

        if (!validate(diskValidator.isVirtIoScsiValid(getVm(), getDiskVmElement()))) {
            return false;
        }

        if (!validate(diskValidator.isDiskInterfaceSupported(getVm(), getDiskVmElement()))) {
            return false;
        }

        if (getVds() != null) {
            lunFromStorage = getLunDisk(lun, getVds());
            if (lunFromStorage == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_INVALID);
            }
        }

        if (!validate(diskValidator.isUsingScsiReservationValid(getVm(), lunDisk))) {
            return false;
        }

        return true;
    }

    /**
     * Retrieves the specified LUN if it's visible to the specified host, or null otherwise.
     *
     * @param lun the LUN to examine.
     * @param vds the host to query from.
     *
     * @return the specified LUN if it's visible to the specified host, or null otherwise.
     */
    protected LUNs getLunDisk(final LUNs lun, VDS vds) {
        return executeGetDeviceList(vds.getId(), lun.getLunType(), lun.getLUNId()).stream().findFirst().orElse(null);
    }

    protected List<LUNs> executeGetDeviceList(Guid vdsId, StorageType storageType, String lunId) {
        GetDeviceListVDSCommandParameters parameters =
                new GetDeviceListVDSCommandParameters(vdsId, storageType, false, Collections.singletonList(lunId));
        return (List<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();
    }

    protected boolean checkIfImageDiskCanBeAdded(VM vm, DiskValidator diskValidator) {
        if (Guid.Empty.equals(getStorageDomainId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_SPECIFIED);
        }

        boolean returnValue;
        StorageDomainValidator storageDomainValidator = createStorageDomainValidator();
        // vm agnostic checks
        returnValue =
                (getParameters().isSkipDomainCheck() || validate(storageDomainValidator.isDomainExistAndActive())) &&
                !isShareableDiskOnGlusterDomain() &&
                checkImageConfiguration() &&
                validate(storageDomainValidator.hasSpaceForNewDisk(getDiskImageInfo())) &&
                validate(storageDomainValidator.isDomainWithinThresholds()) &&
                checkExceedingMaxBlockDiskSize() &&
                canAddShareableDisk() &&
                validate(diskValidator.isVirtIoScsiValid(vm, getDiskVmElement())) &&
                validate(diskValidator.isDiskInterfaceSupported(getVm(), getDiskVmElement()));

        if (returnValue && vm != null) {
            StoragePool sp = getStoragePool(); // Note this is done according to the VM's spId.
            returnValue =
                    validate(new StoragePoolValidator(sp).isUp()) &&
                    isStoragePoolMatching(vm) &&
                    validate(new VmValidator(getVm()).vmNotLocked()) &&
                    isVmNotInPreviewSnapshot();
        }

        return returnValue;
    }

    private boolean isShareableDiskOnGlusterDomain() {
        if (getParameters().getDiskInfo().isShareable() && getStorageDomain().getStorageType() == StorageType.GLUSTERFS) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
            return true;
        }

        return false;
    }

    private boolean canAddShareableDisk() {
        if (getParameters().getDiskInfo().isShareable()) {
            if (!isVolumeFormatSupportedForShareable(((DiskImage) getParameters().getDiskInfo()).getVolumeFormat())) {
                return failValidation(EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
            }
        }
        return true;
    }

    private boolean checkExceedingMaxBlockDiskSize() {
        if (isExceedMaxBlockDiskSize()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED);
            getReturnValue().getValidationMessages().add(
                    String.format("$max_disk_size %1$s", Config.<Integer> getValue(ConfigValues.MaxBlockDiskSize)));
            return false;
        }
        return true;
    }

    private boolean isStoragePoolMatching(VM vm) {
        if (getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
            getStorageDomainId(), vm.getStoragePoolId())) == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_OF_VM_NOT_MATCH);
        }
        return true;
    }


    /** Checks if the image's configuration is legal */
    protected boolean checkImageConfiguration() {
        return ImagesHandler.checkImageConfiguration(
                getStorageDomain().getStorageStaticData(),
                getDiskImageInfo(),
                getReturnValue().getValidationMessages());
    }

    private double getRequestDiskSpace() {
        if (getParameters().getDiskInfo().getDiskStorageType().isInternal()) {
            return getDiskImageInfo().getSizeInGigabytes();
        }
        return 0;
    }

    private boolean isFloatingDisk() {
        return Guid.isNullOrEmpty(getParameters().getVmId());
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
        if (isFloatingDisk()) {
            listPermissionSubjects = new ArrayList<>();
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
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    protected void executeVmCommand() {
        createNewDiskId();
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getDiskInfo().getId()));
        ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());
        switch (getParameters().getDiskInfo().getDiskStorageType()) {
            case IMAGE:
                createDiskBasedOnImage();
                break;
            case LUN:
                createDiskBasedOnLun();
                break;
            case CINDER:
                createDiskBasedOnCinder();
                break;
        }
    }

    private void createNewDiskId() {
        Guid newDiskId = Guid.newGuid();
        getParameters().getDiskInfo().setId(newDiskId);
        if (!isFloatingDisk()) {
            getDiskVmElement().getId().setDeviceId(newDiskId);
        }
    }

    private void createDiskBasedOnLun() {
        final LUNs lun;
        if (lunFromStorage == null) {
            lun = ((LunDisk) getParameters().getDiskInfo()).getLun();
        } else {
            lun = lunFromStorage;
        }
        TransactionSupport.executeInNewTransaction(() -> {
            StorageDomainCommandBase.proceedLUNInDb(lun, lun.getLunType());
            getBaseDiskDao().save(getParameters().getDiskInfo());
            getDiskLunMapDao().save(new DiskLunMap(getParameters().getDiskInfo().getId(), lun.getLUNId()));
            if (getVm() != null) {
                // The disk VM element has to be added before the VM device since as a part of the VM device creation the
                // boot order is determined so the VM device creation depends on the existance of the disk VM element
                addDiskVmElementForDisk(getDiskVmElement());
                addManagedDeviceForDisk(getParameters().getDiskInfo().getId(), ((LunDisk) getParameters().getDiskInfo()).isUsingScsiReservation());
            }
            return null;
        });
        getReturnValue().setActionReturnValue(getParameters().getDiskInfo().getId());
        plugDiskToVmIfNeeded();
        setSucceeded(true);
    }

    private VmDevice addManagedDeviceForDisk(Guid diskId, Boolean isUsingScsiReservation) {
        return  VmDeviceUtils.addDiskDevice(
                getVmId(),
                diskId,
                shouldDiskBePlugged(),
                Boolean.TRUE.equals(getParameters().getDiskInfo().getReadOnly()),
                Boolean.TRUE.equals(isUsingScsiReservation));
    }

    protected VmDevice addManagedDeviceForDisk(Guid diskId) {
        return addManagedDeviceForDisk(diskId, false);
    }

    protected DiskVmElement addDiskVmElementForDisk(DiskVmElement diskVmElement) {
        getDiskVmElementDao().save(diskVmElement);
        return diskVmElement;
    }

    protected boolean shouldDiskBePlugged() {
        return getVm().getStatus() == VMStatus.Down && !Boolean.FALSE.equals(getParameters().getPlugDiskToVm());
    }

    protected boolean useCallback() {
        return getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.IMAGE
                && (parentHasCallback() || !isExecutedAsChildCommand());
    }

    private void createDiskBasedOnImage() {
        if(!getParameters().getDiskInfo().isWipeAfterDeleteSet()) {
            getParameters().getDiskInfo().setWipeAfterDelete(getStorageDomain().getWipeAfterDelete());
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

        if (useCallback()) {
            parameters.setParentCommand(VdcActionType.AddDisk);
            parameters.setParentParameters(getParameters());
        } else {
            parameters.setParentCommand(getParameters().getParentCommand());
            parameters.setParentParameters(getParameters().getParentParameters());
        }

        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setStoragePoolId(getStorageDomain().getStoragePoolId());
        if (getVm() != null) {
            setVmSnapshotIdForDisk(parameters);
        }
        VdcReturnValueBase tmpRetValue =
                runInternalActionWithTasksContext(VdcActionType.AddImageFromScratch,
                        parameters,
                        getLock());
        // Setting lock to null because the lock is released in the child command
        setLock(null);
        getTaskIdList().addAll(tmpRetValue.getInternalVdsmTaskIdList());

        if (getVm() != null) {
            // The disk VM element has to be added before the VM device since as a part of the VM device creation the
            // boot order is determined so the VM device creation depends on the existance of the disk VM element
            getCompensationContext().snapshotEntity(addDiskVmElementForDisk(getDiskVmElement()));
            getCompensationContext().snapshotNewEntity(addManagedDeviceForDisk(getParameters().getDiskInfo().getId()));
            getCompensationContext().stateChanged();
        }

        if (tmpRetValue.getActionReturnValue() != null) {
            DiskImage diskImage = tmpRetValue.getActionReturnValue();
            addDiskPermissions(diskImage);
            getReturnValue().setActionReturnValue(diskImage.getId());
        }
        getReturnValue().setFault(tmpRetValue.getFault());
        setSucceeded(tmpRetValue.getSucceeded());
    }

    @Override
    public CommandCallback getCallback() {
        return useCallback() ? new ConcurrentChildCommandsExecutionCallback() :  null;
    }

    private void createDiskBasedOnCinder() {
        // ToDo: upon using CoCo infra in this commnad, move this logic.
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.AddCinderDisk,
                buildAddCinderDiskParameters(),
                cloneContextAndDetachFromParent());
        try {
            setReturnValue(future.get());
            setSucceeded(getReturnValue().getSucceeded());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error creating Cinder disk '{}': {}",
                    getParameters().getDiskInfo().getDiskAlias(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

    private VdcActionParametersBase buildAddCinderDiskParameters() {
        AddDiskParameters parameters = new AddDiskParameters(new DiskVmElement(null, getVmId()), getParameters().getDiskInfo());
        parameters.setPlugDiskToVm(getParameters().getPlugDiskToVm());
        parameters.setStorageDomainId(getParameters().getStorageDomainId());
        parameters.setQuotaId(getQuotaId());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        if (getVm() != null) {
            parameters.setVmSnapshotId(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE));
            parameters.setDiskVmElement(getParameters().getDiskVmElement());
        }
        return parameters;
    }

    /**
     * If disk is not allow to have snapshot no VM snapshot Id should be updated.
     */
    private void setVmSnapshotIdForDisk(AddImageFromScratchParameters parameters) {
        if (getParameters().getDiskInfo().isAllowSnapshot()) {
            parameters.setVmSnapshotId(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE));
        }
    }

    private void addDiskPermissions(Disk disk) {
        if (getCurrentUser() != null) {
            Permission perms =
                    new Permission(getCurrentUser().getId(),
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
            if (isDiskStorageTypeRequiresExecuteState()) {
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

    private boolean isDiskStorageTypeRequiresExecuteState() {
        return getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.IMAGE ||
                getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.CINDER;
    }

    private AuditLogType getExecuteAuditLogTypeValue(boolean successful) {
        boolean isVmNameExist = StringUtils.isNotEmpty(getVmName());
        if (successful) {
            if (isInternalExecution()) {
                return AuditLogType.ADD_DISK_INTERNAL;
            }
            if (isVmNameExist) {
                return AuditLogType.USER_ADD_DISK_TO_VM;
            } else {
                return AuditLogType.USER_ADD_DISK;
            }
        } else {
            if (isInternalExecution()) {
                return AuditLogType.ADD_DISK_INTERNAL_FAILURE;
            }
            if (isVmNameExist) {
                return AuditLogType.USER_FAILED_ADD_DISK_TO_VM;
            } else {
                return AuditLogType.USER_FAILED_ADD_DISK;
            }
        }
    }

    protected AuditLogType getEndSuccessAuditLogTypeValue(boolean successful) {
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
        // Validation of parameters is only required for VM disks as the rest is validated in the validate() phase
        if (!isFloatingDisk()) {
            addValidationGroup(UpdateEntity.class);
        }
        return super.getValidationGroups();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!isFloatingDisk() && getDiskVmElement() != null && getDiskVmElement().isBoot()) {
            return Collections.singletonMap(getParameters().getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_BOOT, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (!Guid.isNullOrEmpty(getParameters().getVmId()) && !isInternalExecution()) {
            return Collections.singletonMap(getParameters().getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    @Override
    protected void setLoggingForCommand() {
        setCommandShouldBeLogged(true);
    }

    private Guid getQuotaId() {
        if (getParameters().getDiskInfo() != null && getParameters().getDiskInfo().getDiskStorageType().isInternal()) {
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
            VmDiskOperationParameterBase params = new VmDiskOperationParameterBase(new DiskVmElement(getParameters().getDiskInfo().getId(), getVmId()));
            params.setShouldBeLogged(false);
            VdcReturnValueBase returnValue = runInternalAction(VdcActionType.HotPlugDiskToVm, params);
            if (!returnValue.getSucceeded()) {
                auditLogDirector.log(this, AuditLogType.USER_FAILED_HOTPLUG_DISK);
            }
        }
    }

    protected boolean setAndValidateDiskProfiles() {
        return validate(DiskProfileHelper.setAndValidateDiskProfiles(Collections.singletonMap(getDiskImageInfo(),
                getStorageDomainId()), getCurrentUser()));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        if (getParameters().getDiskInfo().getDiskStorageType().isInternal()) {
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
