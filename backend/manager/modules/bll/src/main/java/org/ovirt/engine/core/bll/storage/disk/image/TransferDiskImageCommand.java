package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.CommandHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.ManagedBlockStorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConnectManagedBlockStorageDeviceCommandParameters;
import org.ovirt.engine.core.common.action.DisconnectManagedBlockStorageDeviceParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.action.TransferImageStatusParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackupMode;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageTicket;
import org.ovirt.engine.core.common.businessentities.storage.ImageTicketInformation;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferBackend;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.TimeoutPolicyType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.businessentities.storage.VmBackupType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockInfo;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor.CinderlibCommand;
import org.ovirt.engine.core.common.vdscommands.AddImageTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ConvertManagedBlockVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ExtendImageTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImageTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.NbdServerVDSParameters;
import org.ovirt.engine.core.common.vdscommands.PrepareImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveImageTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVolumeLegalityVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

@NonTransactiveCommandAttribute
public class TransferDiskImageCommand<T extends TransferDiskImageParameters> extends BaseImagesCommand<T> implements QuotaStorageDependent {
    private static final boolean LEGAL_IMAGE = true;
    private static final boolean ILLEGAL_IMAGE = false;
    private static final int PROXY_DATA_PORT = 54323;
    private static final int PROXY_CONTROL_PORT = 54324;
    private static final String HTTPS_SCHEME = "https://";
    private static final String IMAGES_PATH = "/images";
    private static final String FILE_URL_SCHEME = "file://";
    private static final String IMAGE_TYPE = "disk";

    @Inject
    private ImageTransferDao imageTransferDao;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private DiskDao diskDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private ImageTransferUpdater imageTransferUpdater;
    @Inject
    private ImageDao imageDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VmBackupDao vmBackupDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(TransferImageCommandCallback.class)
    private Instance<TransferImageCommandCallback> callbackProvider;
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private CinderStorageDao cinderStorageDao;
    @Inject
    private CinderlibExecutor cinderlibExecutor;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private DiskLunMapDao diskLunMapDao;

    private ImageioClient proxyClient;
    private VmBackup backup;
    private VM backupVm;
    private Guid backupDiskSnapshotId;

    // Used by engine to abort inactive transfer, and passed to imageio server for disconnecting inactive
    // connections.
    private int clientInactivityTimeout;

    public TransferDiskImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        super.init();
        clientInactivityTimeout = getParameters().getClientInactivityTimeout() != null ?
                getParameters().getClientInactivityTimeout() :
                getTransferImageClientInactivityTimeoutInSeconds();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
        addValidationMessage(EngineMessage.VAR__ACTION__TRANSFER);
    }

    protected boolean validateCreateImage() {
        StorageDomain storageDomain = storageDomainDao.get(getParameters().getStorageDomainId());
        if (!isSupportedByManagedBlockStorageDomain(storageDomain)) {
            return false;
        }

        ActionReturnValue returnValue = CommandHelper.validate(ActionType.AddDisk, getAddDiskParameters(),
                getContext().clone());
        getReturnValue().setValidationMessages(returnValue.getValidationMessages());
        return returnValue.isValid();
    }

    protected void createImage() {
        runInternalAction(ActionType.AddDisk, getAddDiskParameters(), cloneContextAndDetachFromParent());
    }

    protected String prepareImage(Guid vdsId) {
        if (isLiveBackup()) {
            return vmBackupDao.getBackupUrlForDisk(
                    getParameters().getBackupId(), getDiskImage().getId());
        }
        if (usingNbdServer()) {
            StorageDomain sd = getStorageDomain();
            if (sd != null && StorageType.MANAGED_BLOCK_STORAGE.equals(sd.getStorageType())) {
                return null;
            }
        }
        // For MBS, VDSM does not have the domain in sdCache; connect and attach the volume on the host
        // and use the path from the attach result instead of calling PrepareImageVDS.
        if (isManagedBlockStorageForTransfer()) {
            validateHostConnectorForMbs();
            DiskImage disk = getDiskImage();
            if (disk instanceof ManagedBlockStorageDisk) {
                String path = connectAttachAndGetMbsVolumePath((ManagedBlockStorageDisk) disk);
                if (path != null) {
                    return path;
                }
            }
        }

        VDSReturnValue vdsRetVal = runVdsCommand(VDSCommandType.PrepareImage,
                getPrepareParameters(vdsId));
        String path = ((PrepareImageReturn) vdsRetVal.getReturnValue()).getImagePath();
        return path != null ? FILE_URL_SCHEME + path : null;
    }

    protected boolean validateImageTransfer() {
        DiskImage diskImage = getDiskImage();
        DiskValidator diskValidator = getDiskValidator(diskImage);
        DiskImagesValidator diskImagesValidator = getDiskImagesValidator(diskImage);
        StorageDomainValidator storageDomainValidator = getStorageDomainValidator(
                storageDomainDao.getForStoragePool(diskImage.getStorageIds().get(0), diskImage.getStoragePoolId()));
        boolean isValid =
                validate(diskValidator.isDiskExists())
                && validate(diskImagesValidator.diskImagesNotIllegal())
                && validate(storageDomainValidator.isDomainExistAndActive());

        if (diskImage.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
            return validate(ManagedBlockStorageDomainValidator.isOperationSupportedByManagedBlockStorage(getActionType()));
        }

        if (isBackup()) {
            if (isHybridBackup()) {
                if (!snapshotDao.exists(getBackup().getVmId(), getBackup().getSnapshotId())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
                }
            }
            return isValid && validate(isVmBackupReady()) && validate(isFormatApplicableForBackup());
        }
        return isValid
                && validateActiveDiskPluggedToAnyNonDownVm(diskImage, diskValidator)
                && validate(diskImagesValidator.diskImagesNotLocked());
    }

    private boolean validateActiveDiskPluggedToAnyNonDownVm(DiskImage diskImage, DiskValidator diskValidator) {
        return diskImage.isDiskSnapshot() || validate(diskValidator.isDiskPluggedToAnyNonDownVm(false));
    }

    private ValidationResult isVmBackupReady() {
        if (getBackup() == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_BACKUP_NOT_EXIST);
        }
        if (!getBackup().getPhase().equals(VmBackupPhase.READY)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_BACKUP_NOT_READY,
                    ReplacementUtils.createSetVariableString("vmBackupPhase", getBackup().getPhase()));
        }
        return ValidationResult.VALID;
    }

    private ValidationResult isFormatApplicableForBackup() {
        if (getParameters().getVolumeFormat() == VolumeFormat.COW) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_FORMAT_NOT_APPLICABLE_FOR_BACKUP);
        }
        return ValidationResult.VALID;
    }

    protected DiskImagesValidator getDiskImagesValidator(DiskImage diskImage) {
        return new DiskImagesValidator(diskImage);
    }

    protected DiskValidator getDiskValidator(DiskImage diskImage) {
        return new DiskValidator(diskImage);
    }

    protected StorageDomainValidator getStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }

    private PrepareImageVDSCommandParameters getPrepareParameters(Guid vdsId) {
        return new PrepareImageVDSCommandParameters(vdsId,
                getStoragePool().getId(),
                getStorageDomainId(),
                getDiskImage().getId(),
                getDiskImage().getImageId(), true);
    }

    private Guid getBitmap() {
        if (isHybridBackup() && getDiskImage().getBackupMode() == DiskBackupMode.Incremental) {
            return getBackup().getFromCheckpointId();
        }

        if (getParameters().getTransferType() == TransferType.Download
                && getBackupVm() != null
                && getBackupVm().isDown()
                && getDiskImage().isQcowFormat()
                && getDiskImage().getBackupMode() == DiskBackupMode.Incremental) {
            return getBackup().getFromCheckpointId();
        }
        return null;
    }

    private NbdServerVDSParameters getStartNbdServerParameters(Guid vdsId) {
        NbdServerVDSParameters nbdServerVDSParameters = new NbdServerVDSParameters(vdsId);
        nbdServerVDSParameters.setServerId(getCommandId());
        nbdServerVDSParameters.setStorageDomainId(getParameters().getStorageDomainId());
        nbdServerVDSParameters.setImageId(getDiskImage().getId());
        nbdServerVDSParameters.setVolumeId(getDiskImage().getImageId());
        nbdServerVDSParameters.setReadonly(getParameters().getTransferType() == TransferType.Download);

        // The "detectZeroes" and "discard" options work together. When detectZeroes is enabled without discard,
        // uploading actual zeroes to the image allocate space on storage without writing actual zeroes to storage.
        // When both detectZeroes and discard are enabled, uploading actual zeroes to the image deallocate space on
        // storage. When detectZeroes is disabled, the discard option has no effect since imageio does not support
        // discard.
        nbdServerVDSParameters.setDetectZeroes(true);
        nbdServerVDSParameters.setDiscard(isSparseImage());

        nbdServerVDSParameters.setBackingChain(!getParameters().isShallow());
        nbdServerVDSParameters.setBitmap(getBitmap());
        return nbdServerVDSParameters;
    }

    /**
     * For NBD-based transfer of an MBS disk: connect the volume and attach it to the host so VDSM can
     * expose it via NBD. Returns false if the disk is MBS and connect/attach fails (caller should return false).
     * Returns true if not MBS, or connect+attach succeeded.
     */
    private boolean connectAndAttachManagedBlockVolumeForTransfer() {
        DiskImage disk = getDiskImage();
        if (!(disk instanceof ManagedBlockStorageDisk)) {
            return true;
        }
        ManagedBlockStorageDisk mbsDisk = (ManagedBlockStorageDisk) disk;
        Guid storageDomainId = mbsDisk.getStorageIds().isEmpty() ? getStorageDomainId() : mbsDisk.getStorageIds().get(0);

        ActionReturnValue connectResult = connectManagedBlockStorageDeviceForTransfer(mbsDisk, storageDomainId);
        if (!connectResult.getSucceeded()) {
            log.error("Failed to connect managed block volume for image transfer '{}': {}",
                    getCommandId(), connectResult.getFault());
            updateEntityPhaseToStoppedBySystem(
                    AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_FAILED_TO_CREATE_TICKET);
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> connectionInfo = (Map<String, Object>) connectResult.getActionReturnValue();
        if (connectionInfo != null && !attachManagedBlockVolumeToHostForTransfer(mbsDisk, storageDomainId, connectionInfo)) {
            return false;
        }
        return true;
    }

    private ActionReturnValue connectManagedBlockStorageDeviceForTransfer(ManagedBlockStorageDisk mbsDisk,
            Guid storageDomainId) {
        ConnectManagedBlockStorageDeviceCommandParameters connectParams =
                new ConnectManagedBlockStorageDeviceCommandParameters(storageDomainId,
                        getVds().getConnectorInfo(),
                        mbsDisk.getImageId());
        return runInternalAction(ActionType.ConnectManagedBlockStorageDevice, connectParams);
    }

    /**
     * Attach the MBS volume to the host so VDSM can expose it via NBD. On failure logs and updates
     * transfer phase; returns false.
     */
    private boolean attachManagedBlockVolumeToHostForTransfer(ManagedBlockStorageDisk mbsDisk,
            Guid storageDomainId,
            Map<String, Object> connectionInfo) {
        AttachManagedBlockStorageVolumeVDSCommandParameters attachParams =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(getVds(),
                        connectionInfo,
                        storageDomainId);
        attachParams.setVolumeId(mbsDisk.getImageId());
        VDSReturnValue attachResult = runVdsCommand(VDSCommandType.AttachManagedBlockStorageVolume, attachParams);
        if (!attachResult.getSucceeded()) {
            log.error("Failed to attach managed block volume to host for image transfer '{}': {}",
                    getCommandId(), attachResult.getVdsError());
            updateEntityPhaseToStoppedBySystem(
                    AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_FAILED_TO_CREATE_TICKET);
            return false;
        }
        return true;
    }

    protected void tearDownImage(Guid vdsId, Guid backupId) {
        if (backupId != null) {
            // shouldn't teardown as prepare wasn't invoked
            return;
        }

        DiskImage image = getDiskImage();
        if (image.isDiskSnapshot() && !isDiskSnapshotPluggedToDownVmsOnly(image)) {
            // shouldn't teardown snapshot disk that attached to a running VM
            return;
        }

        boolean tearDownFailed = false;

        // MBS: detach volume from host and disconnect (no TeardownImage; volume was attached for NBD transfer).
        if (isManagedBlockStorageForTransfer()) {
            ImageTransfer entity = imageTransferDao.get(getCommandId());
            if (entity != null && entity.getDiskId() != null) {
                detachManagedBlockVolumeFromHost(entity);
                disconnectManagedBlockVolumeForTransfer(entity);
            }
            return;
        }

        if (getTransferBackend() == ImageTransferBackend.FILE) {
            if (isTemplateBeingUsed(image)) {
                log.info("Transfer '{}': The template image is being used, skipping teardown", getCommandId());

                return;
            }

            VDSReturnValue teardownImageVdsRetVal = runVdsCommand(VDSCommandType.TeardownImage,
                    getImageActionsParameters(vdsId));
            if (!teardownImageVdsRetVal.getSucceeded()) {
                log.warn("Failed to tear down image '{}' for image transfer '{}': {}",
                        image, getCommandId(), teardownImageVdsRetVal.getVdsError());
                tearDownFailed = true;
            }
        }

        if (tearDownFailed) {
            // Invoke log method directly rather than relying on infra, because teardown
            // failure may occur during command execution, e.g. if the upload is paused.
            addCustomValue("DiskAlias", image.getDiskAlias());
            auditLogDirector.log(this, AuditLogType.TRANSFER_IMAGE_TEARDOWN_FAILED);
        }
    }

    private boolean isTemplateBeingUsed(DiskImage image) {
        if (!Guid.Empty.equals(image.getImageTemplateId())) {
            LockInfo lockInfo =
                    lockManager.getLockInfo(getDiskImage().getImageTemplateId() + LockingGroup.TEMPLATE.toString());
            if (lockInfo != null) {
                return true;
            }
        }

        return false;
    }

    private boolean stopNbdServer(Guid vdsId) {
        NbdServerVDSParameters nbdServerVDSParameters = new NbdServerVDSParameters(vdsId);
        nbdServerVDSParameters.setServerId(getCommandId());
        VDSReturnValue stopNbdServerVdsRetVal = runVdsCommand(VDSCommandType.StopNbdServer,
                nbdServerVDSParameters);
        if (!stopNbdServerVdsRetVal.getSucceeded()) {
            log.warn("Failed to stop NBD server for image transfer '{}': {}",
                    getCommandId(), stopNbdServerVdsRetVal.getVdsError());
            return false;
        }
        return true;
    }

    protected boolean isDiskSnapshotPluggedToDownVmsOnly(DiskImage diskImage) {
        return validate(getDiskValidator(diskImage).isDiskPluggedToAnyNonDownVm(false));
    }

    private AddDiskParameters getAddDiskParameters() {
        AddDiskParameters diskParameters = getParameters().getAddDiskParameters();
        diskParameters.setParentCommand(getActionType());
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setSkipDomainCheck(true);
        diskParameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        // When conversion will run after upload: first volume must be in source (upload) format so VDSM
        // receives the file as-is; we then create a second volume in destination format and convert.
        if (needsConversionAfterUpload() && diskParameters.getDiskInfo() instanceof DiskImage) {
            DiskImage diskInfo = (DiskImage) diskParameters.getDiskInfo();
            diskInfo.setVolumeFormat(getParameters().getSourceVolumeFormat());
            diskInfo.setActualSizeInBytes(getParameters().getTransferSize());
        }
        return diskParameters;
    }

    protected ImageActionsVDSCommandParameters getImageActionsParameters(Guid vdsId) {
        return new ImageActionsVDSCommandParameters(vdsId,
                getStoragePool().getId(),
                getStorageDomainId(),
                getDiskImage().getId(),
                getDiskImage().getImageId());
    }

    protected String getImageAlias() {
        return getParameters().getAddDiskParameters() != null ?
                getParameters().getAddDiskParameters().getDiskInfo().getDiskAlias() :
                getDiskImage().getDiskAlias();
    }

    protected DiskImage getDiskImage() {
        if (isHybridBackup()) {
            setImageId(getBackupDiskSnapshotId());
            return super.getDiskImage();
        }
        if (!Guid.isNullOrEmpty(getParameters().getImageId())) {
            setImageId(getParameters().getImageId());
            return super.getDiskImage();
        }
        DiskImage diskImage = super.getDiskImage();
        if (diskImage == null) {
            diskImage = (DiskImage) diskDao.get(getParameters().getImageGroupID());
        }
        return diskImage;
    }

    private VmBackup getBackup() {
        if (backup == null) {
            backup = vmBackupDao.get(getParameters().getBackupId());
        }
        return backup;
    }

    private VM getBackupVm() {
        if (backupVm == null && getBackup() != null) {
            backupVm = vmDao.get(getBackup().getVmId());
        }
        return backupVm;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        if (getParameters().getAddDiskParameters() != null) {
            AddDiskParameters parameters = getAddDiskParameters();
            list.add(new QuotaStorageConsumptionParameter(
                    ((DiskImage) parameters.getDiskInfo()).getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    getStorageDomainId(),
                    (double) parameters.getDiskInfo().getSize() / SizeConverter.BYTES_IN_GB));
        }

        return list;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> listPermissionSubjects = new ArrayList<>();
        if (isImageProvided()) {
            listPermissionSubjects.add(new PermissionSubject(getParameters().getImageGroupID(),
                    VdcObjectType.Disk,
                    ActionGroup.EDIT_DISK_PROPERTIES));
        } else {
            listPermissionSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                    VdcObjectType.Storage,
                    ActionGroup.CREATE_DISK));
        }

        return listPermissionSubjects;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();

        if (isBackup()) {
            // StartVmBackup should handle locks
            return locks;
        }

        if (!Guid.isNullOrEmpty(getParameters().getImageId()) && getDiskImage() != null) {
            List<VM> vms = vmDao.getVmsListForDisk(getDiskImage().getId(), true);
            vms.forEach(vm -> locks.put(vm.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getDiskIsBeingTransferredLockMessage())));
        }

        if (getDiskImage() != null && getParameters().getTransferType() == TransferType.Download) {
            locks.put(getDiskImage().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsBeingTransferredLockMessage()));
        }

        return locks;
    }

    private String getDiskIsBeingTransferredLockMessage() {
        return new LockMessage(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_TRANSFERRED)
                .withOptional("DiskName", getDiskImage().getDiskAlias() != null ? getDiskImage().getDiskAlias() : "")
                .toString();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        if (isBackup()) {
            // StartVmBackup should handle locks
            return locks;
        }

        if (getDiskImage() != null && getParameters().getTransferType() == TransferType.Upload) {
            locks.put(getDiskImage().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsBeingTransferredLockMessage()));
        }
        return locks;
    }

    @Override
    protected void executeCommand() {
        log.info("Creating ImageTransfer entity for command '{}', proxyEnabled: {}", getCommandId(), proxyEnabled());
        ImageTransfer entity = new ImageTransfer(getCommandId());
        entity.setCommandType(getActionType());
        entity.setPhase(ImageTransferPhase.INITIALIZING);
        entity.setType(getParameters().getTransferType());
        entity.setActive(false);
        entity.setLastUpdated(new Date());
        entity.setBytesTotal(isImageProvided() ? getTransferSize() : getParameters().getTransferSize());
        entity.setClientInactivityTimeout(clientInactivityTimeout);
        entity.setTimeoutPolicy(getParameters().getTimeoutPolicyType() != null ?
                getParameters().getTimeoutPolicyType() : TimeoutPolicyType.LEGACY);
        entity.setImageFormat(getTransferImageFormat());
        entity.setBackend(getTransferBackend());
        entity.setBackupId(getParameters().getBackupId());
        entity.setTransferClientType(getParameters().getTransferClientType());
        entity.setShallow(getParameters().isShallow());
        imageTransferDao.save(entity);

        log.info("Starting image transfer: {}", entity);

        if (isImageProvided()) {
            if (!handleImageIsReadyForTransfer()) {
                return;
            }
        } else {
            if (getParameters().getTransferType() == TransferType.Download) {
                failValidation(EngineMessage.ACTION_TYPE_FAILED_IMAGE_NOT_SPECIFIED_FOR_DOWNLOAD);
                setSucceeded(false);
                return;
            }
            log.info("Creating {} image", IMAGE_TYPE);
            createImage();
        }

        setActionReturnValue(getCommandId());
        setSucceeded(true);
    }

    private boolean isImageProvided() {
        return !Guid.isNullOrEmpty(getParameters().getImageId()) ||
                !Guid.isNullOrEmpty(getParameters().getImageGroupID());
    }

    private VolumeFormat getTransferImageFormat() {
        // When browser upload with format conversion: transfer uses source format (upload volume).
        if (getParameters().getSourceVolumeFormat() != null) {
            return getParameters().getSourceVolumeFormat();
        }
        if (getParameters().getVolumeFormat() != null) {
            return getParameters().getVolumeFormat();
        }
        if (isImageProvided()) {
            return getDiskImage().getVolumeFormat();
        }
        return ((DiskImage) getParameters().getAddDiskParameters().getDiskInfo()).getVolumeFormat();
    }

    protected ImageTransferBackend getTransferBackend() {
        if (isBackup()) {
            // Incremental backup uses NBD transfer backend
            return ImageTransferBackend.NBD;
        }
        VolumeFormat formatForBackend = getParameters().getSourceVolumeFormat() != null
                ? getParameters().getSourceVolumeFormat()
                : getParameters().getVolumeFormat();
        return formatForBackend == VolumeFormat.RAW ?
                ImageTransferBackend.NBD : ImageTransferBackend.FILE;
    }

    /**
     * True when upload needs format conversion: we upload to a volume in source format,
     * then convert to destination format and replace.
     * For MBS: also infer when sourceVolumeFormat is null (e.g. REST API sets only format=
     * source; MBS destination is always RAW).
     */
    private boolean needsConversionAfterUpload() {
        if (getParameters().getTransferType() != TransferType.Upload) {
            log.debug("needsConversionAfterUpload: false (not upload)");
            return false;
        }
        VolumeFormat srcFmt = getParameters().getSourceVolumeFormat();
        VolumeFormat dstFmt = getParameters().getVolumeFormat();
        boolean mbs = isManagedBlockStorageForTransfer();
        log.debug("needsConversionAfterUpload: srcFmt={} dstFmt={} mbs={}", srcFmt, dstFmt, mbs);
        if (srcFmt != null && dstFmt != null && !srcFmt.equals(dstFmt)) {
            log.debug("needsConversionAfterUpload: true (src != dst)");
            return true;
        }
        // MBS: REST API sets only volumeFormat (source format); dest is always RAW
        if (mbs && srcFmt == null && VolumeFormat.COW.equals(dstFmt)) {
            log.debug("needsConversionAfterUpload: true (MBS qcow2 upload)");
            return true;  // qcow2 upload to MBS needs convert to raw
        }
        log.debug("needsConversionAfterUpload: false");
        return false;
    }

    public void proceedCommandExecution(Guid childCmdId) {
        ImageTransfer entity = imageTransferDao.get(getCommandId());
        if (entity == null || entity.getPhase() == null) {
            log.error("Image transfer status entity is corrupted or missing from database for image transfer '{}'",
                    getCommandId());
            setCommandStatus(CommandStatus.FAILED);
            return;
        }
        if (entity.getDiskId() != null) {
            // Make the disk id available for all states below.  If the transfer is still
            // initializing, this may be set below in the INITIALIZING block instead.
            setImageGroupId(entity.getDiskId());
        }

        long ts = System.currentTimeMillis() / 1000;
        executeStateHandler(entity, ts, childCmdId);
    }

    public void executeStateHandler(ImageTransfer entity, long timestamp, Guid childCmdId) {
        StateContext context = new StateContext();
        context.entity = entity;
        context.iterationTimestamp = timestamp;
        context.childCmdId = childCmdId;

        // State handler methods are responsible for calling setCommandStatus
        // as well as updating the entity to reflect transitions.
        switch (entity.getPhase()) {
            case INITIALIZING:
                handleInitializing(context);
                break;
            case RESUMING:
                handleResuming(context);
                break;
            case TRANSFERRING:
                handleTransferring(context);
                break;
            case PAUSED_SYSTEM:
                handlePausedSystem(context);
                break;
            case PAUSED_USER:
                handlePausedUser(context);
                break;
            case CANCELLED_USER:
                handleCancelledUser();
                break;
            case CANCELLED_SYSTEM:
                handleCancelledSystem();
                break;
            case FINALIZING_SUCCESS:
                handleFinalizingSuccess(context);
                break;
            case FINALIZING_FAILURE:
                handleFinalizingFailure(context);
                break;
            case FINALIZING_CLEANUP:
                handleFinalizingCleanup(context);
                break;
            case FINISHED_CLEANUP:
                handleFinishedCleanup();
                break;
            case CONVERTING:
                handleConverting(context);
                break;
            default:
                // UNKNOWN, FINISHED_SUCCESS, FINISHED_FAILURE - no action
                break;
        }
    }

    private void handleInitializing(final StateContext context) {
        if (context.childCmdId == null) {
            // Guard against callback invocation before executeCommand() is complete
            return;
        }

        switch (commandCoordinatorUtil.getCommandStatus(context.childCmdId)) {
            case NOT_STARTED:
            case ACTIVE:
                log.info("Waiting for {} to be added for image transfer '{}'", IMAGE_TYPE, getCommandId());
                return;
            case SUCCEEDED:
            case ENDED_SUCCESSFULLY:
                break;
            default:
                log.error("Failed to add {} for image transfer '{}'", IMAGE_TYPE, getCommandId());
                setCommandStatus(CommandStatus.FAILED);
                return;
        }

        ActionReturnValue addDiskRetVal = commandCoordinatorUtil.getCommandReturnValue(context.childCmdId);
        if (addDiskRetVal == null || !addDiskRetVal.getSucceeded()) {
            log.error("Failed to add {} (command status was success, but return value was failed)"
                    + " for image transfer '{}'", IMAGE_TYPE, getCommandId());
            setReturnValue(addDiskRetVal);
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        Guid createdId = addDiskRetVal.getActionReturnValue();
        // Saving disk id in the parameters in order to persist it in command_entities table
        getParameters().setImageGroupID(createdId);
        if (!handleImageIsReadyForTransfer()) {
            setCommandStatus(CommandStatus.FAILED);
        }
    }

    protected boolean handleImageIsReadyForTransfer() {
        DiskImage image = getDiskImage();
        Guid domainId = image.getStorageIds().get(0);

        getParameters().setStorageDomainId(domainId);
        getParameters().setDestinationImageId(image.getImageId());

        // imageio server limits access to range 0-size.
        getParameters().setTransferSize(getTransferSize());

        persistCommand(getParameters().getParentCommand(), true);
        setImage(image);
        setStorageDomainId(domainId);

        log.info("Successfully added {} for image transfer '{}'", getTransferDescription(), getCommandId());

        ImageTransfer updates = new ImageTransfer();
        updates.setDiskId(getParameters().getImageGroupID());
        updateEntity(updates);

        // The image will remain locked until the transfer command has completed.
        lockImage();
        if (!startImageTransferSession()) {
            return false;
        }
        log.info("Returning from proceedCommandExecution() after starting image transfer '{}'", getCommandId());

        resetPeriodicPauseLogTime(0);
        return true;
    }

    /**
     * direction   storage    transfer    disk     ticket size
     * =================================================================================
     * upload      block      raw         *        virtual size
     * upload      block      -           raw      virtual size (lv size)
     * upload      block      -           qcow2    actual size (lv size)
     * ---------------------------------------------------------------------------------
     * upload      file       raw         *        virtual size
     * upload      file       -           raw      virtual size
     * upload      file       -           qcow2    virtual size + cow overhead
     * ---------------------------------------------------------------------------------
     * upload (ui) *          -           *        uploaded file size
     * ---------------------------------------------------------------------------------
     * download    block      raw         *        virtual size (using /map)
     * download    block      -           raw      virtual size
     * download    block      -           qcow2    image-end-offset (returned by
     *                                             "qemu-img check" - not implemented yet)
     * ---------------------------------------------------------------------------------
     * download    file       raw         *        virtual size (using /map)
     * download    file       -           raw      virtual size
     * download    file       -           qcow2    file size
     * ---------------------------------------------------------------------------------
     */
    private long getTransferSize() {
        DiskImage image = getDiskImage();

        if (getTransferBackend() == ImageTransferBackend.NBD) {
            // NBD always uses virtual size (raw format)
            return image.getSize();
        }

        if (getParameters().getTransferType() == TransferType.Download) {
            if (image.getVolumeFormat() == VolumeFormat.RAW) {
                return image.getSize();
            }
            if (image.getVolumeFormat() == VolumeFormat.COW) {
                return getImageApparentSize(image);
            }
            // Shouldn't happen
            throw new RuntimeException(String.format(
                    "Invalid volume format: %s", image.getVolumeFormat()));
        } else if (getParameters().getTransferType() == TransferType.Upload) {
            if (getParameters().getTransferClientType().isBrowserTransfer()) {
                return getParameters().getTransferSize();
            }
            if (image.getVolumeFormat() == VolumeFormat.RAW) {
                return image.getSize();
            }
            // COW volume format
            boolean isBlockDomain = image.getStorageTypes().get(0).isBlockDomain();
            if (isBlockDomain) {
                return image.getActualSizeInBytes();
            }
            // Needed to allow uploading fully allocated qcow (BZ#1697294)
            // Also, adding qcow header overhead to support small files.
            return (long) Math.ceil(image.getSize() * StorageConstants.QCOW_OVERHEAD_FACTOR)
                    + StorageConstants.QCOW_HEADER_OVERHEAD;
        }
        // Shouldn't happen
        throw new RuntimeException(String.format(
                "Invalid transfer type: %s", getParameters().getTransferType()));
    }

    protected long getImageApparentSize(DiskImage image) {
        Guid domainId = image.getStorageIds().get(0);
        DiskImage imageInfoFromVdsm = imagesHandler.getVolumeInfoFromVdsm(
                image.getStoragePoolId(), domainId, image.getId(), image.getImageId());
        return imageInfoFromVdsm.getApparentSizeInBytes();
    }

    private void handleResuming(final StateContext context) {
        lockImage();

        log.info("Resuming transfer '{}' for {}", getCommandId(), getTransferDescription());
        auditLog(this, AuditLogType.TRANSFER_IMAGE_RESUMED_BY_USER);
        extendTicketIfNecessary(context);
        updateEntityPhase(ImageTransferPhase.TRANSFERRING);

        resetPeriodicPauseLogTime(0);
    }

    private void handleTransferring(final StateContext context) {
        // While the transfer is in progress, we're responsible
        // for keeping the transfer session alive.
        extendTicketIfNecessary(context);
        resetPeriodicPauseLogTime(0);
        pollTransferStatus(context);
    }

    private void extendTicketIfNecessary(final StateContext context) {
        // The polling interval is user-configurable and grows
        // exponentially, make sure to set it with time to spare.
        if (context.iterationTimestamp
                >= getParameters().getSessionExpiration() - getHostTicketRefreshAllowance()) {
            log.info("Renewing transfer ticket for {}, image transfer '{}'", getTransferDescription(), getCommandId());
            boolean extendSucceeded = extendImageTransferSession(context.entity);
            if (!extendSucceeded) {
                log.warn("Failed to renew transfer ticket for {}, image transfer '{}'",
                        getTransferDescription(), getCommandId());
                if (getParameters().isRetryExtendTicket()) {
                    // Set 'extendTicketFailed' flag to true for giving a grace period
                    // for another extend attempt.
                    getParameters().setRetryExtendTicket(false);
                } else {
                    updateEntityPhaseToStoppedBySystem(
                            AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_TICKET_RENEW_FAILURE);
                    getParameters().setRetryExtendTicket(true);
                }
            }
        } else {
            log.debug("Not yet renewing transfer ticket for {}, image transfer '{}'",
                    getTransferDescription(), getCommandId());
        }
    }

    private void pollTransferStatus(final StateContext context) {
        if (context.entity.getVdsId() == null || context.entity.getImagedTicketId() == null) {
            // Old engines update the transfer status in UploadImageHandler::updateBytesSent.
            return;
        }
        ImageTicketInformation ticketInfo;
        try {
            ticketInfo = (ImageTicketInformation) runVdsCommand(VDSCommandType.GetImageTicket,
                    new GetImageTicketVDSCommandParameters(
                            context.entity.getVdsId(), context.entity.getImagedTicketId())).getReturnValue();
        } catch (EngineException e) {
            log.error("Could not get image ticket '{}' from vdsm, image transfer '{}': {}",
                    context.entity.getImagedTicketId(), getCommandId(), e);
            updateEntityPhaseToStoppedBySystem(
                    AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_MISSING_TICKET);
            return;
        }
        ImageTransfer upToDateImageTransfer = updateTransferStatusWithTicketInformation(context.entity, ticketInfo);
        if (getParameters().getTransferType() == TransferType.Download) {
            finalizeDownloadIfNecessary(context, upToDateImageTransfer);
        }

        // Check conditions for pausing the transfer (ie UI is MIA)
        stopTransferIfNecessary(upToDateImageTransfer, context.iterationTimestamp, ticketInfo.getIdleTime());
    }

    private ImageTransfer updateTransferStatusWithTicketInformation(ImageTransfer oldImageTransfer,
            ImageTicketInformation ticketInfo) {
        if (!Objects.equals(oldImageTransfer.getActive(), ticketInfo.isActive()) ||
                !Objects.equals(oldImageTransfer.getBytesSent(), ticketInfo.getTransferred())) {
            // At least one of the status fields (bytesSent or active) should be updated.
            ImageTransfer updatesFromTicket = new ImageTransfer();
            updatesFromTicket.setBytesSent(ticketInfo.getTransferred());
            updatesFromTicket.setActive(ticketInfo.isActive());
            ActionReturnValue returnValue = runInternalAction(ActionType.TransferImageStatus,
                    new TransferImageStatusParameters(getCommandId(), updatesFromTicket), cloneContext().withoutLock());
            if (returnValue == null || !returnValue.getSucceeded()) {
                log.debug("Failed to update status for image transfer '{}'", getCommandId());
                return oldImageTransfer;
            }
            return returnValue.getActionReturnValue();
        }
        return oldImageTransfer;
    }

    private void finalizeDownloadIfNecessary(final StateContext context, ImageTransfer upToDateImageTransfer) {
        if (upToDateImageTransfer.getBytesTotal() != 0 &&
                // Frontend flow (REST API should close the connection on its own).
                getParameters().getTransferSize() == upToDateImageTransfer.getBytesSent() &&
                !upToDateImageTransfer.getActive()) {
            // Heuristic - once the transfer is inactive, we want to wait another COCO iteration
            // to decrease the chances that the few last packets are still on the way to the client.
            if (!context.entity.getActive()) { // The entity from the previous COCO iteration.
                // This is the second COCO iteration that the transfer is inactive.
                ImageTransfer statusUpdate = new ImageTransfer();
                statusUpdate.setPhase(ImageTransferPhase.FINALIZING_SUCCESS);
                runInternalAction(ActionType.TransferImageStatus,
                        new TransferImageStatusParameters(getCommandId(), statusUpdate), cloneContext().withoutLock());
            }
        }
    }

    private void handlePausedUser(final StateContext context) {
        auditLog(this, AuditLogType.TRANSFER_IMAGE_PAUSED_BY_USER);
        handlePaused(context);
    }

    private void handlePausedSystem(final StateContext context) {
        handlePaused(context);
    }

    private void handleCancelledUser() {
        log.info("Image transfer '{}' cancelled by user for {}", getCommandId(), getTransferDescription());
        setAuditLogTypeFromPhase(ImageTransferPhase.CANCELLED_USER);
        updateEntityPhase(ImageTransferPhase.FINALIZING_CLEANUP);
    }

    private void handleCancelledSystem() {
        log.info("Image transfer '{}' cancelled by system for {}", getCommandId(), getTransferDescription());
        setAuditLogTypeFromPhase(ImageTransferPhase.CANCELLED_SYSTEM);
        updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
    }

    private void handleFinalizingSuccess(final StateContext context) {
        log.info("Finalizing successful image transfer '{}' for {}", getCommandId(), getTransferDescription());

        ImageStatus nextImageStatus = ImageStatus.OK;

        // If stopping the session did not succeed, don't change the transfer state.
        if (stopImageTransferSession(context.entity)) {
            Guid transferingVdsId = context.entity.getVdsId();

            // When browser upload and source format != destination: convert via qemu-img then replace volume.
            if (getParameters().getTransferType() == TransferType.Upload) {
                log.info("Upload transfer '{}': srcFmt={} destFmt={} browser={} needsConversion={}",
                        getCommandId(), getParameters().getSourceVolumeFormat(), getParameters().getVolumeFormat(),
                        getParameters().getTransferClientType().isBrowserTransfer(), needsConversionAfterUpload());
            }
            if (needsConversionAfterUpload()) {
                log.info("Upload transfer '{}' requires format conversion: {} -> {}",
                        getCommandId(), getParameters().getSourceVolumeFormat(), getParameters().getVolumeFormat());
                if (isManagedBlockStorageForTransfer()) {
                    boolean started = startMbsUploadConversion(context);
                    if (started) {
                        updateEntityPhase(ImageTransferPhase.CONVERTING);
                    } else {
                        nextImageStatus = ImageStatus.ILLEGAL;
                        updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
                        tearDownImage(context.entity.getVdsId(), context.entity.getBackupId());
                        setImageStatus(nextImageStatus);
                    }
                    return;
                }
            }

            // Verify image is relevant only on upload
            if (getParameters().getTransferType() == TransferType.Download) {
                setAuditLogTypeFromPhase(ImageTransferPhase.FINISHED_SUCCESS);
                setCommandStatus(CommandStatus.SUCCEEDED);
            } else if (verifyImage(transferingVdsId)) {
                // We want to use the transferring vds for image actions for having a coherent log when transferring.
                setVolumeLegalityInStorage(LEGAL_IMAGE);
                if (getDiskImage().getVolumeFormat().equals(VolumeFormat.COW)) {
                    setQcowCompat(getDiskImage().getImage(),
                            getStoragePool().getId(),
                            getDiskImage().getId(),
                            getDiskImage().getImageId(),
                            getStorageDomainId(),
                            transferingVdsId);
                    imageDao.update(getDiskImage().getImage());
                }
                setAuditLogTypeFromPhase(ImageTransferPhase.FINISHED_SUCCESS);
                setCommandStatus(CommandStatus.SUCCEEDED);
            } else {
                nextImageStatus = ImageStatus.ILLEGAL;
                updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
            }

            // Finished using the image, tear it down.
            tearDownImage(context.entity.getVdsId(), context.entity.getBackupId());

            // Moves Image status to OK or ILLEGAL
            setImageStatus(nextImageStatus);
        }
    }

    /**
     * How far MBS upload conversion got before failure; drives orphan cleanup order.
     */
    private enum MbsConversionStartProgress {
        VOLUME_CREATED,
        CONNECTED_ON_HOST,
        ATTACHED_ON_HOST
    }

    /**
     * Start MBS conversion: create destination volume, connect and attach on host.
     * Persists convertedVolumeId for handleConverting.
     */
    private boolean startMbsUploadConversion(StateContext context) {
        Guid sdId = getStorageDomainId();
        Guid dstVolId = Guid.newGuid();
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(sdId);
        if (managedBlockStorage == null) {
            log.error("Managed block storage domain '{}' not found for conversion", sdId);
            return false;
        }

        log.debug("MBS upload conversion for transfer '{}': disk={} srcVol={} -> dstVol={}, srcFmt={} destFmt={}",
                getCommandId(), getParameters().getImageGroupID(), getDiskImage().getImageId(), dstVolId,
                getParameters().getSourceVolumeFormat(), getParameters().getVolumeFormat());

        if (!mbsConversionCreateVolume(managedBlockStorage, dstVolId)) {
            return false;
        }

        VDS vds = vdsDao.get(context.entity.getVdsId());
        if (vds == null || vds.getConnectorInfo() == null) {
            log.error("Host or connector info missing for MBS conversion");
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, null,
                    MbsConversionStartProgress.VOLUME_CREATED);
            return false;
        }

        ActionReturnValue connectResult = mbsConversionConnectVolume(vds, sdId, dstVolId);
        if (!connectResult.getSucceeded()) {
            log.error("Connect failed for MBS conversion: {}", connectResult.getFault());
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, null,
                    MbsConversionStartProgress.VOLUME_CREATED);
            return false;
        }
        Map<String, Object> connectionInfo = connectResult.getActionReturnValue();
        if (connectionInfo == null) {
            log.error("No connection info returned for MBS conversion");
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, null,
                    MbsConversionStartProgress.VOLUME_CREATED);
            return false;
        }

        if (!mbsConversionAttachVolume(vds, connectionInfo, sdId, dstVolId)) {
            log.error("Attach failed for MBS conversion");
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, connectionInfo,
                    MbsConversionStartProgress.CONNECTED_ON_HOST);
            return false;
        }

        try {
            getParameters().setConvertedVolumeId(dstVolId);
            persistCommand(getParameters().getParentCommand(), true);
            log.info("Started MBS upload conversion for transfer '{}': convertedVolumeId={}",
                    getCommandId(), dstVolId);
            return true;
        } catch (Exception e) {
            log.error("Failed to persist MBS upload conversion for transfer '{}': {}", getCommandId(), e);
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, connectionInfo,
                    MbsConversionStartProgress.ATTACHED_ON_HOST);
            return false;
        }
    }

    private boolean mbsConversionCreateVolume(ManagedBlockStorage managedBlockStorage, Guid dstVolId) {
        try {
            long sizeGiB = SizeConverter.convert(getDiskImage().getSize(),
                    SizeConverter.SizeUnit.BYTES, SizeConverter.SizeUnit.GiB).longValue();
            List<String> extraParams = new ArrayList<>();
            extraParams.add(dstVolId.toString());
            extraParams.add(Long.toString(sizeGiB));
            CinderlibCommandParameters params = new CinderlibCommandParameters(
                    JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(), false),
                    extraParams, getCorrelationId());
            if (!cinderlibExecutor.runCommand(CinderlibCommand.CREATE_VOLUME, params).getSucceed()) {
                log.error("CREATE_VOLUME failed for transfer '{}'", getCommandId());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("CREATE_VOLUME raised for transfer '{}': {}", getCommandId(), e);
            return false;
        }
    }

    private ActionReturnValue mbsConversionConnectVolume(VDS vds, Guid sdId, Guid dstVolId) {
        ConnectManagedBlockStorageDeviceCommandParameters connectParams =
                new ConnectManagedBlockStorageDeviceCommandParameters(sdId, vds.getConnectorInfo(), dstVolId);
        return runInternalAction(ActionType.ConnectManagedBlockStorageDevice, connectParams);
    }

    private boolean mbsConversionAttachVolume(VDS vds, Map<String, Object> connectionInfo, Guid sdId,
            Guid dstVolId) {
        AttachManagedBlockStorageVolumeVDSCommandParameters attachParams =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(vds, connectionInfo, sdId);
        attachParams.setVolumeId(dstVolId);
        try {
            VDSReturnValue attachResult = runVdsCommand(VDSCommandType.AttachManagedBlockStorageVolume, attachParams);
            if (!attachResult.getSucceeded()) {
                log.error("Attach failed for MBS conversion: {}", attachResult.getVdsError());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Attach raised for MBS conversion: {}", e);
            return false;
        }
    }

    /**
     * Best-effort removal of a partially created conversion volume after startMbsUploadConversion fails.
     */
    private void cleanupOrphanMbsConversionVolume(ManagedBlockStorage managedBlockStorage, Guid sdId, VDS vds,
            Guid volId, Map<String, Object> connectionInfo, MbsConversionStartProgress progress) {
        if (managedBlockStorage == null) {
            return;
        }
        log.warn("Cleaning up orphan MBS conversion volume '{}' for transfer '{}' (progress={})",
                volId, getCommandId(), progress);
        if (progress == MbsConversionStartProgress.ATTACHED_ON_HOST && vds != null) {
            mbsConversionTryDetachVolume(vds, sdId, volId);
        }
        if (progress == MbsConversionStartProgress.CONNECTED_ON_HOST
                || progress == MbsConversionStartProgress.ATTACHED_ON_HOST) {
            if (vds != null && connectionInfo != null) {
                mbsConversionTryDisconnectDevice(sdId, vds, volId, connectionInfo);
            }
            mbsConversionTryCinderDisconnect(managedBlockStorage, volId);
        }
        mbsConversionTryCinderDeleteVolume(managedBlockStorage, volId);
    }

    private void mbsConversionTryDetachVolume(VDS vds, Guid sdId, Guid volId) {
        try {
            AttachManagedBlockStorageVolumeVDSCommandParameters detachParams =
                    new AttachManagedBlockStorageVolumeVDSCommandParameters(vds);
            detachParams.setVolumeId(volId);
            detachParams.setStorageDomainId(sdId);
            runVdsCommand(VDSCommandType.DetachManagedBlockStorageVolume, detachParams);
        } catch (Exception e) {
            log.warn("Orphan cleanup: detach volume {} failed: {}", volId, e);
        }
    }

    private void mbsConversionTryDisconnectDevice(Guid sdId, VDS vds, Guid volId,
            Map<String, Object> connectionInfo) {
        try {
            DisconnectManagedBlockStorageDeviceParameters disconnectParams =
                    new DisconnectManagedBlockStorageDeviceParameters(sdId, connectionInfo, volId, vds.getId());
            runInternalAction(ActionType.DisconnectManagedBlockStorageDevice, disconnectParams);
        } catch (Exception e) {
            log.warn("Orphan cleanup: disconnect device for volume {} failed: {}", volId, e);
        }
    }

    private void mbsConversionTryCinderDisconnect(ManagedBlockStorage managedBlockStorage, Guid volId) {
        List<String> extraParams = Collections.singletonList(volId.toString());
        try {
            cinderlibExecutor.runCommand(CinderlibCommand.DISCONNECT_VOLUME,
                    new CinderlibCommandParameters(
                            JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(), false),
                            extraParams, getCorrelationId()));
        } catch (Exception e) {
            log.warn("Orphan cleanup: cinderlib DISCONNECT_VOLUME for {} failed: {}", volId, e);
        }
    }

    private void mbsConversionTryCinderDeleteVolume(ManagedBlockStorage managedBlockStorage, Guid volId) {
        List<String> extraParams = Collections.singletonList(volId.toString());
        try {
            cinderlibExecutor.runCommand(CinderlibCommand.DELETE_VOLUME,
                    new CinderlibCommandParameters(
                            JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(), false),
                            extraParams, getCorrelationId()));
        } catch (Exception e) {
            log.warn("Orphan cleanup: cinderlib DELETE_VOLUME for {} failed: {}", volId, e);
        }
    }

    private void handleConverting(StateContext context) {
        // MBS: synchronous convert (no copy task)
        if (isManagedBlockStorageForTransfer() && getParameters().getConvertedVolumeId() != null) {
            // Run convert (synchronous) on host
            Guid sdId = getStorageDomainId();
            Guid srcVolId = getDiskImage().getImageId();
            Guid dstVolId = getParameters().getConvertedVolumeId();
            VolumeFormat srcFmt = getParameters().getSourceVolumeFormat() != null
                    ? getParameters().getSourceVolumeFormat()
                    : getParameters().getVolumeFormat();  // REST API: volumeFormat = source
            VolumeFormat dstFmt = getParameters().getSourceVolumeFormat() != null && getParameters().getVolumeFormat() != null
                    ? getParameters().getVolumeFormat()
                    : VolumeFormat.RAW;  // MBS dest is always RAW when inferred
            String srcFormat = srcFmt == VolumeFormat.COW ? "qcow2" : "raw";
            String dstFormat = dstFmt == VolumeFormat.COW ? "qcow2" : "raw";

            VDS vds = vdsDao.get(context.entity.getVdsId());
            if (vds == null) {
                log.error("Host not found for MBS conversion");
                updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
                setCommandStatus(CommandStatus.FAILED);
                return;
            }
            try {
                ConvertManagedBlockVolumeVDSCommandParameters convertParams =
                        new ConvertManagedBlockVolumeVDSCommandParameters(vds, sdId, srcVolId, dstVolId, srcFormat, dstFormat);
                VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.ConvertManagedBlockVolume, convertParams);
                if (!vdsReturnValue.getSucceeded()) {
                    log.error("ConvertManagedBlockVolume failed for transfer '{}': {}", getCommandId(), vdsReturnValue.getVdsError());
                    updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
                    setCommandStatus(CommandStatus.FAILED);
                    return;
                }
                log.info("MBS upload conversion completed for transfer '{}', finishing", getCommandId());
                finishMbsUploadConversion(context);
            } catch (Exception e) {
                log.error("Failed MBS conversion for transfer '{}': {}", getCommandId(), e);
                updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
                setCommandStatus(CommandStatus.FAILED);
            }
            return;
        }

        Guid copyTaskId = getParameters().getCopyTaskId();
        if (copyTaskId == null) {
            log.error("Conversion phase but no copy task id for transfer '{}'", getCommandId());
            updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
            setCommandStatus(CommandStatus.FAILED);
            return;
        }
        Guid spmId = getStoragePool().getSpmVdsId();
        if (spmId == null || Guid.Empty.equals(spmId)) {
            log.debug("Waiting for SPM for transfer '{}'", getCommandId());
            return;
        }
        try {
            VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.HSMGetTaskStatus,
                    new HSMTaskGuidBaseVDSCommandParameters(spmId, copyTaskId));
            if (!vdsReturnValue.getSucceeded()) {
                log.debug("Could not get copy task status for transfer '{}'", getCommandId());
                return;
            }
            AsyncTaskStatus taskStatus = (AsyncTaskStatus) vdsReturnValue.getReturnValue();
            if (taskStatus.getStatus() == AsyncTaskStatusEnum.finished) {
                log.info("Upload conversion copy task finished for transfer '{}', completing conversion", getCommandId());
                finishUploadConversion(context);
            } else if (taskStatus.getStatus() == AsyncTaskStatusEnum.unknown
                    || taskStatus.getStatus() == AsyncTaskStatusEnum.aborting) {
                log.error("Copy task failed for transfer '{}': {}", getCommandId(), taskStatus);
                updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
                setCommandStatus(CommandStatus.FAILED);
            }
        } catch (Exception e) {
            log.debug("Polling copy task for transfer '{}': {}", getCommandId(), e.getMessage());
        }
    }

    private void finishUploadConversion(StateContext context) {
        Guid oldImageId = getDiskImage().getImageId();
        Guid diskId = getParameters().getImageGroupID();
        Guid newVolId = getParameters().getConvertedVolumeId();
        DiskImage currentImage = getDiskImage();

        log.info("Finishing upload conversion for transfer '{}': replacing oldVol={} with newVol={} (disk={})",
                getCommandId(), oldImageId, newVolId, diskId);

        DiskImage newImage = new DiskImage();
        newImage.setId(diskId);
        newImage.setImageId(newVolId);
        newImage.setVolumeFormat(getParameters().getVolumeFormat());
        newImage.setVolumeType(currentImage.getVolumeType());
        newImage.setSize(currentImage.getSize());
        newImage.setDiskAlias(currentImage.getDiskAlias());
        newImage.setDiskDescription(currentImage.getDiskDescription());
        newImage.setStorageIds(currentImage.getStorageIds());
        newImage.setStoragePoolId(currentImage.getStoragePoolId());
        newImage.setActive(true);
        newImage.setParentId(Guid.Empty);
        newImage.setImageTemplateId(Guid.Empty);
        newImage.setQuotaId(currentImage.getQuotaId());
        newImage.setDiskProfileId(currentImage.getDiskProfileId());
        newImage.setWipeAfterDelete(currentImage.isWipeAfterDelete());
        if (VolumeFormat.COW.equals(getParameters().getVolumeFormat())) {
            newImage.setBackup(currentImage.getBackup());
        }

        imagesHandler.saveImage(newImage);
        baseDiskDao.update(newImage);  // Disk already exists from AddDisk; update, don't insert
        log.info("Upload conversion for transfer '{}': saved new image disk={} vol={}", getCommandId(), diskId, newVolId);

        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(newVolId);
        diskDynamic.setActualSize(currentImage.getActualSizeInBytes());
        diskImageDynamicDao.save(diskDynamic);

        log.info("Upload conversion for transfer '{}': removing old volume {}", getCommandId(), oldImageId);
        runInternalAction(ActionType.RemoveImage, new RemoveImageParameters(oldImageId));

        setImageId(newVolId);

        setVolumeLegalityInStorage(LEGAL_IMAGE);
        if (VolumeFormat.COW.equals(getParameters().getVolumeFormat())) {
            setQcowCompat(getDiskImage().getImage(), getStoragePool().getId(), getDiskImage().getId(),
                    getDiskImage().getImageId(), getStorageDomainId(), context.entity.getVdsId());
            imageDao.update(getDiskImage().getImage());
        }
        setImageStatus(ImageStatus.OK);
        tearDownImage(context.entity.getVdsId(), context.entity.getBackupId());
        setAuditLogTypeFromPhase(ImageTransferPhase.FINISHED_SUCCESS);
        setCommandStatus(CommandStatus.SUCCEEDED);
    }

    /**
     * Finish MBS conversion: detach/delete source volume, save new image, detach new volume.
     */
    private void finishMbsUploadConversion(StateContext context) {
        Guid oldImageId = getDiskImage().getImageId();
        Guid diskId = getParameters().getImageGroupID();
        Guid newVolId = getParameters().getConvertedVolumeId();
        Guid sdId = getStorageDomainId();
        DiskImage currentImage = getDiskImage();

        VDS vds = vdsDao.get(context.entity.getVdsId());
        if (vds == null) {
            log.error("Host not found for MBS conversion finish");
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        log.info("Finishing MBS upload conversion for transfer '{}': replacing oldVol={} with newVol={} (disk={})",
                getCommandId(), oldImageId, newVolId, diskId);

        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(sdId);
        detachAndDeleteMbsSourceVolume(vds, oldImageId, sdId, managedBlockStorage);
        replaceSourceDiskWithDestinationMbsDisk(oldImageId, diskId, newVolId, currentImage);
        getParameters().setImageGroupID(newVolId);
        setImageId(newVolId);
        setDiskImage(null);
        setImage(null);
        detachAndDisconnectMbsVolume(vds, newVolId, sdId, managedBlockStorage);
        completeMbsConversionSuccess(context);
    }

    private void detachAndDeleteMbsSourceVolume(VDS vds, Guid oldImageId, Guid sdId,
            ManagedBlockStorage managedBlockStorage) {
        AttachManagedBlockStorageVolumeVDSCommandParameters detachParams =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(vds);
        detachParams.setVolumeId(oldImageId);
        detachParams.setStorageDomainId(sdId);
        try {
            runVdsCommand(VDSCommandType.DetachManagedBlockStorageVolume, detachParams);
        } catch (Exception e) {
            log.warn("Detach source volume failed for MBS conversion: {}", e);
        }
        if (managedBlockStorage != null) {
            List<String> extraParams = new ArrayList<>();
            extraParams.add(oldImageId.toString());
            try {
                cinderlibExecutor.runCommand(CinderlibCommand.DISCONNECT_VOLUME,
                        new CinderlibCommandParameters(
                                JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(), false),
                                extraParams, getCorrelationId()));
            } catch (Exception e) {
                log.warn("Disconnect source volume failed for MBS conversion: {}", e);
            }
            try {
                cinderlibExecutor.runCommand(CinderlibCommand.DELETE_VOLUME,
                        new CinderlibCommandParameters(
                                JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(), false),
                                extraParams, getCorrelationId()));
            } catch (Exception e) {
                log.warn("Delete source volume failed for MBS conversion: {}", e);
            }
        }
    }

    /**
     * Two-disk flow (like copy): create destination disk for new volume, repoint all references
     * from source disk to destination, then remove source disk. No new DB procedures.
     */
    private void replaceSourceDiskWithDestinationMbsDisk(Guid oldImageId, Guid sourceDiskId, Guid newVolId,
            DiskImage currentImage) {
        imageStorageDomainMapDao.remove(oldImageId);
        diskImageDynamicDao.remove(oldImageId);
        imageDao.remove(oldImageId);

        DiskImage destImage = buildNewMbsDiskImage(newVolId, currentImage);
        imagesHandler.saveImage(destImage);
        baseDiskDao.save(new ManagedBlockStorageDisk(destImage));

        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(newVolId);
        diskDynamic.setActualSize(currentImage.getActualSizeInBytes());
        diskImageDynamicDao.save(diskDynamic);

        repointReferencesFromSourceToDestination(sourceDiskId, newVolId);
        baseDiskDao.remove(sourceDiskId);
    }

    /**
     * Repoint all references from source disk id to destination disk id using existing DAOs
     * (no new DB procedures). Order preserves FKs where applicable.
     */
    private void repointReferencesFromSourceToDestination(Guid sourceDiskId, Guid destDiskId) {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDevicesByDeviceId(sourceDiskId, null);
        for (VmDevice device : vmDevices) {
            vmDeviceDao.remove(device.getId());
            VmDevice newDevice = new VmDevice();
            newDevice.setId(new VmDeviceId(destDiskId, device.getVmId()));
            newDevice.setDevice(device.getDevice());
            newDevice.setType(device.getType());
            newDevice.setAddress(device.getAddress());
            newDevice.setSpecParams(device.getSpecParams());
            newDevice.setManaged(device.isManaged());
            newDevice.setPlugged(device.isPlugged());
            newDevice.setReadOnly(device.getReadOnly());
            newDevice.setSnapshotId(device.getSnapshotId());
            newDevice.setAlias(device.getAlias());
            newDevice.setCustomProperties(device.getCustomProperties());
            newDevice.setLogicalName(device.getLogicalName());
            newDevice.setHostDevice(device.getHostDevice());
            vmDeviceDao.save(newDevice);
        }

        List<DiskVmElement> diskVmElements = diskVmElementDao.getAllDiskVmElementsByDiskId(sourceDiskId);
        for (DiskVmElement dve : diskVmElements) {
            diskVmElementDao.remove(dve.getId());
            DiskVmElement newDve = new DiskVmElement(destDiskId, dve.getVmId());
            newDve.setBoot(dve.isBoot());
            newDve.setPassDiscard(dve.isPassDiscard());
            newDve.setDiskInterface(dve.getDiskInterface());
            newDve.setUsingScsiReservation(dve.isUsingScsiReservation());
            diskVmElementDao.save(newDve);
        }

        ImageTransfer transfer = imageTransferDao.getByDiskId(sourceDiskId);
        if (transfer != null) {
            transfer.setDiskId(destDiskId);
            imageTransferDao.update(transfer);
        }

        List<Permission> permissions = permissionDao.getAllForEntity(sourceDiskId);
        for (Permission perm : permissions) {
            perm.setObjectId(destDiskId);
            permissionDao.update(perm);
        }

        DiskLunMap lunMap = diskLunMapDao.getDiskLunMapByDiskId(sourceDiskId);
        if (lunMap != null) {
            diskLunMapDao.remove(lunMap.getId());
            DiskLunMap newMap = new DiskLunMap(destDiskId, lunMap.getLunId());
            diskLunMapDao.save(newMap);
        }

        List<Snapshot> snapshotsWithMemory = snapshotDao.getSnapshotsByMemoryDiskId(sourceDiskId);
        for (Snapshot snapshot : snapshotsWithMemory) {
            if (sourceDiskId.equals(snapshot.getMemoryDiskId())) {
                snapshot.setMemoryDiskId(destDiskId);
            }
            if (sourceDiskId.equals(snapshot.getMetadataDiskId())) {
                snapshot.setMetadataDiskId(destDiskId);
            }
            snapshotDao.update(snapshot);
        }
    }

    private DiskImage buildNewMbsDiskImage(Guid newVolId, DiskImage currentImage) {
        VolumeFormat destFormat = getParameters().getVolumeFormat() != null && getParameters().getSourceVolumeFormat() != null
                ? getParameters().getVolumeFormat()
                : VolumeFormat.RAW;
        DiskImage newImage = new DiskImage();
        newImage.setId(newVolId);
        newImage.setImageId(newVolId);
        newImage.setVolumeFormat(destFormat);
        newImage.setVolumeType(currentImage.getVolumeType());
        newImage.setSize(currentImage.getSize());
        newImage.setDiskAlias(currentImage.getDiskAlias());
        newImage.setDiskDescription(currentImage.getDiskDescription());
        newImage.setStorageIds(currentImage.getStorageIds());
        newImage.setStoragePoolId(currentImage.getStoragePoolId());
        newImage.setActive(true);
        newImage.setParentId(Guid.Empty);
        newImage.setImageTemplateId(Guid.Empty);
        newImage.setQuotaId(currentImage.getQuotaId());
        newImage.setDiskProfileId(currentImage.getDiskProfileId());
        newImage.setWipeAfterDelete(currentImage.isWipeAfterDelete());
        if (VolumeFormat.COW.equals(getParameters().getVolumeFormat())) {
            newImage.setBackup(currentImage.getBackup());
        }
        return newImage;
    }

    private void detachAndDisconnectMbsVolume(VDS vds, Guid volId, Guid sdId,
            ManagedBlockStorage managedBlockStorage) {
        AttachManagedBlockStorageVolumeVDSCommandParameters detachParams =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(vds);
        detachParams.setVolumeId(volId);
        detachParams.setStorageDomainId(sdId);
        try {
            runVdsCommand(VDSCommandType.DetachManagedBlockStorageVolume, detachParams);
        } catch (Exception e) {
            log.warn("Detach volume failed for MBS conversion: {}", e);
        }
        if (managedBlockStorage != null) {
            List<String> disconnectParams = new ArrayList<>();
            disconnectParams.add(volId.toString());
            try {
                cinderlibExecutor.runCommand(CinderlibCommand.DISCONNECT_VOLUME,
                        new CinderlibCommandParameters(
                                JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(), false),
                                disconnectParams, getCorrelationId()));
            } catch (Exception e) {
                log.warn("Disconnect volume failed for MBS conversion: {}", e);
            }
        }
    }

    private void completeMbsConversionSuccess(StateContext context) {
        setVolumeLegalityInStorage(LEGAL_IMAGE);
        if (VolumeFormat.COW.equals(getParameters().getVolumeFormat()) && getDiskImage() != null) {
            setQcowCompat(getDiskImage().getImage(), getStoragePool().getId(), getDiskImage().getId(),
                    getDiskImage().getImageId(), getStorageDomainId(), context.entity.getVdsId());
            imageDao.update(getDiskImage().getImage());
        }
        setImageStatus(ImageStatus.OK);
        setAuditLogTypeFromPhase(ImageTransferPhase.FINISHED_SUCCESS);
        setCommandStatus(CommandStatus.SUCCEEDED);
    }

    private boolean verifyImage(Guid transferingVdsId) {
        if (isManagedBlockStorageForTransfer()) {
            return true;
        }
        ImageActionsVDSCommandParameters parameters =
                new ImageActionsVDSCommandParameters(transferingVdsId, getStoragePool().getId(),
                        getStorageDomainId(),
                        getDiskImage().getId(),
                        getDiskImage().getImageId());

        try {
            // As we currently support a single volume image, we only need to verify that volume.
            vdsBroker.runVdsCommand(VDSCommandType.VerifyUntrustedVolume, parameters);
        } catch (RuntimeException e) {
            log.error("Failed to verify transferred image for image transfer '{}': {}", getCommandId(), e);
            return false;
        }
        return true;
    }

    private void handleFinalizingFailure(final StateContext context) {
        cleanup(context, true);
        log.error("Image transfer '{}' failed for {}", getCommandId(), getTransferDescription());
        setCommandStatus(CommandStatus.FAILED);
    }

    private void handleFinalizingCleanup(final StateContext context) {
        cleanup(context, false);
    }

    private void cleanup(final StateContext context, boolean failure) {
        if (failure) {
            log.error("Finalizing failed image transfer '{}' for {}", getCommandId(), getTransferDescription());
        } else {
            log.info("Cleaning up after cancelled image transfer '{}' for {}",
                    getCommandId(), getTransferDescription());
        }

        // If stopping the session did not succeed, don't change the transfer state.
        // TODO: refactor to reuse similar logic in handleFinalizingSuccess
        if (stopImageTransferSession(context.entity)) {
            // Setting disk status to ILLEGAL only on upload failure
            setImageStatus(getParameters().getTransferType() == TransferType.Upload ?
                    ImageStatus.ILLEGAL : ImageStatus.OK);
            Guid vdsId = context.entity.getVdsId() != null ? context.entity.getVdsId() : getVdsId();
            // Teardown is required for all scenarios as we call prepareImage when
            // starting a new session.
            tearDownImage(vdsId, context.entity.getBackupId());
            if (!failure) {
                updateEntityPhase(ImageTransferPhase.FINISHED_CLEANUP);
                setAuditLogTypeFromPhase(ImageTransferPhase.FINISHED_CLEANUP);
            }
        }
    }

    private void handleFinishedCleanup() {
        log.info("Cleanup after cancelled image transfer '{}' finished for {}",
                getCommandId(), getTransferDescription());
        setCommandStatus(CommandStatus.FAILED);
    }

    private void handlePaused(final StateContext context) {
        periodicPauseLog(context.entity, context.iterationTimestamp);
    }

    /**
     * Verify conditions for continuing the transfer, stopping it if necessary.
     */
    private void stopTransferIfNecessary(ImageTransfer entity, long ts, Integer idleTimeFromTicket) {
        if (shouldAbortOnClientInactivityTimeout(entity, ts, idleTimeFromTicket)) {
            switch (entity.getTimeoutPolicy()) {
                case LEGACY:
                    if (getParameters().getTransferType() == TransferType.Download) {
                        // In download flows, we can cancel the transfer if there was no activity
                        // for a while, as the download is handled by the client.
                        auditLog(this, AuditLogType.DOWNLOAD_IMAGE_CANCELED_TIMEOUT);
                        updateEntityPhase(ImageTransferPhase.CANCELLED_SYSTEM);
                    } else {
                        updateEntityPhaseToStoppedBySystem(AuditLogType.UPLOAD_IMAGE_PAUSED_BY_SYSTEM_TIMEOUT);
                    }
                    break;
                case PAUSE:
                    auditLog(this, AuditLogType.TRANSFER_IMAGE_PAUSED_ON_TIMEOUT);
                    updateEntityPhase(ImageTransferPhase.PAUSED_SYSTEM);
                    break;
                case CANCEL:
                    auditLog(this, AuditLogType.TRANSFER_IMAGE_CANCELED_ON_TIMEOUT);
                    updateEntityPhase(ImageTransferPhase.CANCELLED_SYSTEM);
                    break;
            }
        }
    }

    private boolean shouldAbortOnClientInactivityTimeout(ImageTransfer entity, long ts, Integer idleTimeFromTicket) {
        // For new daemon (1.3.0), we check timeout according to 'idle_time' in ticket;
        // otherwise, fallback to check according to entity's 'lastUpdated'.
        boolean timeoutExceeded = idleTimeFromTicket != null ?
                idleTimeFromTicket > clientInactivityTimeout :
                ts > (entity.getLastUpdated().getTime() / 1000) + clientInactivityTimeout;
        return clientInactivityTimeout > 0
                && timeoutExceeded
                && !entity.getActive();
    }

    private void resetPeriodicPauseLogTime(long ts) {
        if (getParameters().getLastPauseLogTime() != ts) {
            getParameters().setLastPauseLogTime(ts);
            persistCommand(getParameters().getParentCommand(), true);
        }
    }

    private void periodicPauseLog(ImageTransfer entity, long ts) {
        if (ts >= getParameters().getLastPauseLogTime() + getPauseLogInterval()) {
            log.info("Image transfer '{}' was paused by {} for {}",
                    getCommandId(),
                    entity.getPhase() == ImageTransferPhase.PAUSED_SYSTEM ? "system" : "user",
                    getTransferDescription());
            resetPeriodicPauseLogTime(ts);
        }
    }

    /**
     * Start the ovirt-image-daemon session
     */
    protected boolean startImageTransferSession() {
        if (!initializeVds()) {
            log.error("Could not find a suitable host for image transfer '{}'", getCommandId());
            auditLog(this, AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_MISSING_HOST);

            // Go directly to endWithFailure, we only need to remove the disk if we failed at this point
            // for upload, and only unlock for download
            setImageStatus(ImageStatus.OK);
            setCommandStatus(CommandStatus.FAILED);

            return false;
        }

        String imagePath;
        try {
            imagePath = prepareImage(getVdsId());
        } catch (Exception e) {
            log.error("Failed to prepare image for image transfer '{}': {}", getCommandId(), e);
            setImageStatus(ImageStatus.OK);
            setCommandStatus(CommandStatus.FAILED);

            return false;
        }

        // When host does not return a path (e.g. managed block), NBD path will be set below
        if (imagePath == null && !usingNbdServer()) {
            log.error("Image prepare did not return a path for image transfer '{}' (storage may not support file path)", getCommandId());
            setImageStatus(ImageStatus.OK);
            setCommandStatus(CommandStatus.FAILED);
            return false;
        }
        if (imagePath == null) {
            imagePath = "";
        }

        // From this point if an operation fails we have to perform cleanup
        Guid imagedTicketId = Guid.newGuid();
        ImageTransfer updates = new ImageTransfer();
        updates.setImagedTicketId(imagedTicketId);
        updates.setVdsId(getVdsId());
        updates.setProxyUri(getProxyUri() + IMAGES_PATH);
        updates.setDaemonUri(getImageDaemonUri(getVds().getHostName()) + IMAGES_PATH);
        updateEntity(updates);

        if (usingNbdServer()) {
            if (isManagedBlockStorageForTransfer() && !connectAndAttachManagedBlockVolumeForTransfer()) {
                return false;
            }
            try {
                VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.StartNbdServer,
                        getStartNbdServerParameters(getVdsId()));
                imagePath = (String) vdsReturnValue.getReturnValue();
            } catch (Exception e) {
                log.error("Failed to start NBD server for image transfer '{}': {}", getCommandId(), e);
                updateEntityPhaseToStoppedBySystem(
                        AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_FAILED_TO_CREATE_TICKET);
                return false;
            }
        }

        if (!addImageTicketToDaemon(imagedTicketId, imagePath)) {
            log.error("Failed to add host image ticket for image transfer '{}'", getCommandId());
            updateEntityPhaseToStoppedBySystem(
                    AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_FAILED_TO_ADD_TICKET_TO_DAEMON);
            return false;
        }

        if (proxyEnabled()) {
            if (!addImageTicketToProxy(imagedTicketId, getImageDaemonUri(getVds().getHostName()))) {
                log.error("Failed to add proxy image ticket for image transfer '{}'", getCommandId());
                if (getParameters().getTransferClientType().isBrowserTransfer()) {
                    updateEntityPhaseToStoppedBySystem(
                            AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_FAILED_TO_ADD_TICKET_TO_PROXY);
                    return false;
                }
                // No need to stop the transfer - API client can use the daemon url directly.
                auditLog(this, AuditLogType.TRANSFER_FAILED_TO_ADD_TICKET_TO_PROXY);
            }
        }

        log.info("Started image transfer '{}', timeout {} seconds", getCommandId(), getClientTicketLifetime());

        setNewSessionExpiration(getClientTicketLifetime());
        updateEntityPhase(ImageTransferPhase.TRANSFERRING);
        return true;
    }

    private static boolean proxyEnabled() {
        // We always use the proxy in normal deployment, but some users are
        // still using the old unsupported all-in-one configuration, running
        // engine and vdsm on the same host. In this configuration, the proxy
        // must be disabled.
        //
        // When we don't use the proxy:
        // - We don't add, extend or remove proxy image tickets.
        // - ImageTransfer's proxy_url is same as transfer_url, both use the
        //   daemon port.
        // - The administration portal is transferring images directly to the
        //   daemon.
        return Config.<Boolean>getValue(ConfigValues.ImageTransferProxyEnabled);
    }

    @Override
    protected VDS checkForActiveVds() {
        StorageDomain storageDomain = getStorageDomain();
        boolean isManagedBlockStorage = storageDomain != null
                && StorageType.MANAGED_BLOCK_STORAGE.equals(storageDomain.getStorageType());

        Guid hostForExecution = vdsCommandsHelper.getHostForExecution(getStoragePoolId(), host -> {
            if (isManagedBlockStorage) {
                return true;
            }
            var domainsData = resourceManager.getVdsManager(host.getId()).getDomains();
            if (domainsData == null) {
                return false;
            }
            var domainData = domainsData.stream()
                    .filter(vdsDomainsData -> vdsDomainsData.getDomainId().equals(getStorageDomainId()))
                    .findFirst()
                    .orElse(null);
            return domainData != null ? domainData.isValid() : false;
        });

        if (hostForExecution == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
            getReturnValue().setValid(false);
            return null;
        }

        return vdsDao.get(hostForExecution);
    }

    @Override
    protected boolean initializeVds() {
        // In case of downloading a backup disk, the host that runs the VM must handle the transfer
        if (isBackup() && getParameters().getTransferType() == TransferType.Download) {
            VmBackup backup = getBackup();
            if (backup == null) {
                log.error("Cannot download disk '{}', image transfer '{}'. Backup '{}' doesn't exist.",
                        getDiskImage().getId(),
                        getCommandId(),
                        getParameters().getBackupId());
                return false;
            }

            if (getBackupVm() == null) {
                log.error("Cannot download disk '{}' for backup '{}', image transfer '{}'. VM '{}' could not be found.",
                        getDiskImage().getId(),
                        getParameters().getBackupId(),
                        getCommandId(),
                        backup.getVmId());
                return false;
            }

            // Downloading a cold VM backup disk can be done from the host specified by the user,
            // or from any active host otherwise.
            if (getBackupVm().isDown()) {
                return super.initializeVds();
            }

            VDS vds = vdsDao.get(getBackupVm().getRunOnVds());
            if (vds == null) {
                // Can happen when the VM crashed and Not running on any host.
                log.warn("Cannot download disk '{}' for backup '{}', image transfer '{}'. VM is down.",
                        getDiskImage().getId(),
                        getParameters().getBackupId(),
                        getCommandId());
                return false;
            }

            setVds(vds);
            return true;
        }
        return super.initializeVds();
    }

    private boolean addImageTicketToDaemon(Guid imagedTicketId, String imagePath) {
        if (getParameters().getTransferType() == TransferType.Upload &&
                !setVolumeLegalityInStorage(ILLEGAL_IMAGE)) {
            return false;
        }

        ImageTicket ticket = buildImageTicket(imagedTicketId, imagePath);
        AddImageTicketVDSCommandParameters transferCommandParams =
                new AddImageTicketVDSCommandParameters(getVdsId(), ticket);

        // TODO This is called from doPolling(), we should run it async (runFutureVDSCommand?)
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = vdsBroker.runVdsCommand(VDSCommandType.AddImageTicket, transferCommandParams);
        } catch (RuntimeException e) {
            log.error("Failed to start image transfer '{}': {}", getCommandId(), e);
            return false;
        }

        if (!vdsRetVal.getSucceeded()) {
            log.error("Failed to start image transfer '{}': {}", getCommandId(), vdsRetVal.getVdsError());
            return false;
        }

        log.info("Started image transfer '{}' with ticket id {}, timeout {} seconds",
                getCommandId(), imagedTicketId, ticket.getTimeout());
        return true;
    }

    private boolean isSparseImage() {
        // Sparse is not supported yet for block storage in imageio. See BZ#1619006.
        return getDiskImage().getVolumeType() == VolumeType.Sparse &&
                getStorageDomain().getStorageType().isFileDomain();
    }

    private boolean isBackup() {
        return getParameters().getBackupId() != null;
    }

    private boolean isLiveBackup() {
        return isBackup() && getBackup().getBackupType() == VmBackupType.Live;
    }

    private boolean isHybridBackup() {
        return isBackup() && getBackup().getBackupType() == VmBackupType.Hybrid;
    }

    private boolean isSupportsDirtyExtents() {
        if (!isBackup() ||
                getParameters().getTransferType() != TransferType.Download ||
                getDiskImage().getBackupMode() != DiskBackupMode.Incremental) {
            return false;
        }
        VmBackup backup = getBackup();
        return backup != null && backup.isIncremental();
    }

    private boolean usingNbdServer() {
        return !isLiveBackup() && getTransferBackend() == ImageTransferBackend.NBD;
    }

    private boolean isManagedBlockStorageForTransfer() {
        StorageDomain sd = getStorageDomain();
        return sd != null && StorageType.MANAGED_BLOCK_STORAGE.equals(sd.getStorageType());
    }

    private void validateHostConnectorForMbs() {
        if (getVds() == null || getVds().getConnectorInfo() == null) {
            throw new EngineException(EngineError.StorageException,
                    "Host has no connector info; cannot connect managed block volume for image transfer");
        }
    }

    /**
     * Connect and attach the MBS volume on the host, then return the file path for the transfer.
     * @return file URL (e.g. file:///path) or null if connection info was missing
     */
    private String connectAttachAndGetMbsVolumePath(ManagedBlockStorageDisk mbsDisk) {
        Guid storageDomainId = mbsDisk.getStorageIds().isEmpty()
                ? getStorageDomainId()
                : mbsDisk.getStorageIds().get(0);
        Map<String, Object> connectionInfo = connectMbsVolume(storageDomainId, mbsDisk.getImageId());
        if (connectionInfo == null) {
            return null;
        }
        return attachMbsVolumeAndGetPath(storageDomainId, mbsDisk.getImageId(), connectionInfo);
    }

    private Map<String, Object> connectMbsVolume(Guid storageDomainId, Guid volumeId) {
        ConnectManagedBlockStorageDeviceCommandParameters connectParams =
                new ConnectManagedBlockStorageDeviceCommandParameters(storageDomainId,
                        getVds().getConnectorInfo(),
                        volumeId);
        ActionReturnValue connectResult =
                runInternalAction(ActionType.ConnectManagedBlockStorageDevice, connectParams);
        if (!connectResult.getSucceeded()) {
            throw new EngineException(EngineError.StorageException,
                    connectResult.getFault() != null ? connectResult.getFault().getMessage() : "Failed to connect managed block volume");
        }
        return connectResult.getActionReturnValue();
    }

    private String attachMbsVolumeAndGetPath(Guid storageDomainId, Guid volumeId, Map<String, Object> connectionInfo) {
        AttachManagedBlockStorageVolumeVDSCommandParameters attachParams =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(getVds(), connectionInfo, storageDomainId);
        attachParams.setVolumeId(volumeId);
        VDSReturnValue attachResult = runVdsCommand(VDSCommandType.AttachManagedBlockStorageVolume, attachParams);
        if (!attachResult.getSucceeded()) {
            throw new EngineException(EngineError.StorageException,
                    attachResult.getVdsError() != null ? attachResult.getVdsError().getMessage() : "Failed to attach managed block volume");
        }
        String path = extractPathFromAttachResult(attachResult.getReturnValue());
        if (path == null) {
            throw new EngineException(EngineError.StorageException,
                    "Attach managed block volume succeeded but did not return a path");
        }
        return FILE_URL_SCHEME + path;
    }

    @SuppressWarnings("unchecked")
    private static String extractPathFromAttachResult(Object returnValue) {
        if (!(returnValue instanceof Map)) {
            return null;
        }
        Map<String, Object> resultMap = (Map<String, Object>) returnValue;
        String path = (String) resultMap.get("path");
        if (path == null) {
            path = (String) resultMap.get("managed_path");
        }
        return path;
    }

    private boolean addImageTicketToProxy(Guid imagedTicketId, String hostUri) {
        // ToDo: move formatting to an helper for reuse in ImageTransfer
        String url = String.format("%s%s/%s", hostUri, IMAGES_PATH, imagedTicketId);
        ImageTicket imageTicket = buildImageTicket(imagedTicketId, url);

        log.info("Adding proxy image ticket '{}' for image transfer '{}'", imagedTicketId, getCommandId());
        try {
            getProxyClient().putTicket(imageTicket);
        } catch (RuntimeException e) {
            log.error("Failed to add proxy image ticket '{}' for image transfer '{}': {}",
                    imagedTicketId, getCommandId(), e);
            return false;
        }

        return true;
    }

    private ImageTicket buildImageTicket(Guid ticketId, String ticketUrl) {
        ImageTicket ticket = new ImageTicket();

        ticket.setId(ticketId);
        ticket.setTimeout(getClientTicketLifetime());
        ticket.setInactivityTimeout(clientInactivityTimeout);
        ticket.setSize(getParameters().getTransferSize());
        ticket.setUrl(ticketUrl);
        ticket.setTransferId(getParameters().getCommandId().toString());
        ticket.setFilename(getParameters().getDownloadFilename());
        ticket.setSparse(isSparseImage());
        ticket.setDirty(isSupportsDirtyExtents());
        ticket.setOps(new String[] {getParameters().getTransferType().getAllowedOperation()});

        return ticket;
    }

    private boolean setVolumeLegalityInStorage(boolean legal) {
        if (isManagedBlockStorageForTransfer()) {
            return true;
        }
        SetVolumeLegalityVDSCommandParameters parameters =
                new SetVolumeLegalityVDSCommandParameters(getStoragePool().getId(),
                        getStorageDomainId(),
                        getDiskImage().getId(),
                        getDiskImage().getImageId(),
                        legal);
        try {
            runVdsCommand(VDSCommandType.SetVolumeLegality, parameters);
        } catch (EngineException e) {
            log.error("Failed to set image volume legality to {} for image '{}', volume '{}', image transfer '{}': {}",
                    legal, getImage().getImage().getDiskId(), getImage().getImageId(), getCommandId(), e);
            return false;
        }
        return true;
    }

    @Override
    protected boolean validate() {
        if (isImageProvided()) {
            return validateImageTransfer();
        } else if (getParameters().getTransferType() == TransferType.Download) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMAGE_NOT_SPECIFIED_FOR_DOWNLOAD);
        }
        return validateCreateImage();
    }

    private boolean extendImageTransferSession(final ImageTransfer entity) {
        if (entity.getImagedTicketId() == null) {
            log.error("Failed to extend image transfer '{}': no existing session to extend", getCommandId());
            return false;
        }

        long timeout = getHostTicketLifetime();
        Guid resourceId = entity.getImagedTicketId();

        // Send extend ticket request to vdsm
        ExtendImageTicketVDSCommandParameters transferCommandParams =
                new ExtendImageTicketVDSCommandParameters(entity.getVdsId(), entity.getImagedTicketId(), timeout);
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = vdsBroker.runVdsCommand(VDSCommandType.ExtendImageTicket, transferCommandParams);
        } catch (RuntimeException e) {
            log.error("Failed to extend host image ticket '{}' for image transfer '{}': {}",
                    resourceId, getCommandId(), e);
            return false;
        }

        if (!vdsRetVal.getSucceeded()) {
            log.error("Failed to extend image transfer '{}': {}", getCommandId(), vdsRetVal.getVdsError());
            return false;
        }

        log.info("Image transfer '{}' with ticket '{}' extended, timeout {} seconds",
                getCommandId(), resourceId, timeout);

        // Send extend ticket request to proxy
        if (proxyEnabled()) {
            try {
                getProxyClient().extendTicket(resourceId, timeout);
            } catch (RuntimeException e) {
                log.error("Failed to extend proxy ticket '{}' for image transfer '{}': {}",
                        resourceId, getCommandId(), e);
                if (getParameters().getTransferClientType().isBrowserTransfer()) {
                    updateEntityPhaseToStoppedBySystem(
                            AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_FAILED_TO_ADD_TICKET_TO_PROXY);
                    return false;
                }
            }
        }

        setNewSessionExpiration(timeout);
        return true;
    }

    private void setNewSessionExpiration(long timeout) {
        getParameters().setSessionExpiration((System.currentTimeMillis() / 1000) + timeout);
        persistCommand(getParameters().getParentCommand(), true);
    }

    private boolean stopImageTransferSession(ImageTransfer entity) {
        if (Guid.isNullOrEmpty(entity.getImagedTicketId())) {
            log.warn("Image transfer '{}'. Ticket does not exist for image '{}'",
                    getCommandId(), entity.getDiskId());

            // Shouldn't happen, but if the image ticket id is missing in the
            // database it is very likely that it wasn't created
            return true;
        }

        // If we failed to remove the ticket from the daemon, we must fail and
        // retry the operation again.
        if (!removeImageTicketFromDaemon(entity.getImagedTicketId(), entity.getVdsId())) {
            return false;
        }

        // Do not fail if removing proxy ticket fails. Once we remove the host
        // ticket, the proxy ticket cannot be used anyway. In the worst case we
        // are left with proxy ticket that allow access to non-existing URL.
        if (proxyEnabled()) {
            removeImageTicketFromProxy(entity.getImagedTicketId());
        }

        // Stopping NBD server if necessary
        if (usingNbdServer()) {
            stopNbdServer(entity.getVdsId());
        }
        // For managed block: detach volume from host when not doing conversion (conversion keeps it attached).
        if (isManagedBlockStorageForTransfer() && !needsConversionAfterUpload() && entity.getDiskId() != null) {
            detachManagedBlockVolumeFromHost(entity);
            disconnectManagedBlockVolumeForTransfer(entity);
        }

        ImageTransfer updates = new ImageTransfer();
        updateEntity(updates, true);
        return true;
    }

    /**
     * Detaches the managed block volume from the host (VDS DetachManagedBlockStorageVolume).
     * Called when transfer ends, before disconnecting from the storage adapter.
     */
    private void detachManagedBlockVolumeFromHost(ImageTransfer imageTransfer) {
        Disk disk = diskDao.get(imageTransfer.getDiskId());
        if (!(disk instanceof ManagedBlockStorageDisk)) {
            return;
        }
        ManagedBlockStorageDisk mbsDisk = (ManagedBlockStorageDisk) disk;
        if (mbsDisk.getStorageIds() == null || mbsDisk.getStorageIds().isEmpty()) {
            return;
        }
        VDS vds = vdsDao.get(imageTransfer.getVdsId());
        if (vds == null) {
            log.warn("Host '{}' not found for managed block volume detach", imageTransfer.getVdsId());
            return;
        }
        AttachManagedBlockStorageVolumeVDSCommandParameters params =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(vds);
        params.setVolumeId(mbsDisk.getImageId());
        params.setStorageDomainId(mbsDisk.getStorageIds().get(0));
        try {
            VDSReturnValue result = runVdsCommand(VDSCommandType.DetachManagedBlockStorageVolume, params);
            if (!result.getSucceeded()) {
                log.warn("Host detach of managed block volume '{}' failed for transfer '{}': {}",
                        mbsDisk.getImageId(), getCommandId(), result.getVdsError());
            }
        } catch (Exception e) {
            log.error("Failed to detach managed block volume from host '{}' for transfer '{}': {}",
                    imageTransfer.getVdsId(), getCommandId(), e);
        }
    }

    /**
     * Disconnects the managed block volume via the storage adapter (DISCONNECT_VOLUME).
     * Called when transfer ends, after detaching from the host.
     */
    private void disconnectManagedBlockVolumeForTransfer(ImageTransfer imageTransfer) {
        Disk disk = diskDao.get(imageTransfer.getDiskId());
        if (!(disk instanceof ManagedBlockStorageDisk)) {
            return;
        }
        ManagedBlockStorageDisk mbsDisk = (ManagedBlockStorageDisk) disk;
        if (mbsDisk.getStorageIds() == null || mbsDisk.getStorageIds().isEmpty()) {
            log.warn("Managed block disk '{}' has no storage domain for disconnect", imageTransfer.getDiskId());
            return;
        }
        Guid storageDomainId = mbsDisk.getStorageIds().get(0);
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(storageDomainId);
        if (managedBlockStorage == null) {
            log.warn("Managed block storage domain '{}' not found for disconnect", storageDomainId);
            return;
        }
        List<String> extraParams = new ArrayList<>();
        extraParams.add(mbsDisk.getImageId().toString());
        try {
            CinderlibCommandParameters params = new CinderlibCommandParameters(
                    JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(), false),
                    extraParams,
                    getCorrelationId());
            if (!cinderlibExecutor.runCommand(CinderlibCommand.DISCONNECT_VOLUME, params)
                    .getSucceed()) {
                log.warn("Storage adapter DISCONNECT_VOLUME failed for disk '{}' volume '{}'",
                        imageTransfer.getDiskId(), mbsDisk.getImageId());
            }
        } catch (Exception e) {
            log.error("Failed to disconnect managed block volume '{}' for transfer '{}': {}",
                    mbsDisk.getImageId(), getCommandId(), e);
        }
    }

    private boolean removeImageTicketFromDaemon(Guid imagedTicketId, Guid vdsId) {
        RemoveImageTicketVDSCommandParameters parameters = new RemoveImageTicketVDSCommandParameters(
                vdsId, imagedTicketId);
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = vdsBroker.runVdsCommand(VDSCommandType.RemoveImageTicket, parameters);
        } catch (RuntimeException e) {
            log.error("Failed to stop image transfer '{}' for ticket '{}': {}", getCommandId(), imagedTicketId, e);
            return false;
        }

        if (!vdsRetVal.getSucceeded()) {
            log.warn("Failed to stop image transfer '{}' for ticket '{}': {}",
                    getCommandId(), imagedTicketId, vdsRetVal.getVdsError());
            return false;
        }

        log.info("Successfully stopped image transfer '{}' for ticket '{}'", getCommandId(), imagedTicketId);
        return true;
    }

    private boolean removeImageTicketFromProxy(Guid imagedTicketId) {
        // Send DELETE request
        try {
            log.info("Removing proxy image ticket '{}' for image transfer '{}'", imagedTicketId, getCommandId());
            getProxyClient().deleteTicket(imagedTicketId);
        } catch (RuntimeException e) {
            log.error("Failed to remove proxy image ticket '{}' for image transfer '{}': {}",
                    imagedTicketId, getCommandId(), e);
            return false;
        }
        return true;
    }

    private void updateEntityFinalPhase(ImageTransferPhase phase) {
        freeLock();
        updateEntityPhase(phase);
        setAuditLogTypeFromPhase(phase);
    }

    private void updateEntityPhase(ImageTransferPhase phase) {
        ImageTransfer updates = new ImageTransfer(getCommandId());
        updates.setPhase(phase);
        updateEntity(updates);
    }

    private void updateEntityPhaseToStoppedBySystem(AuditLogType stoppedBySystemReason) {
        auditLog(this, stoppedBySystemReason);
        if (getParameters().getTransferType() == TransferType.Upload) {
            updateEntityPhase(ImageTransferPhase.PAUSED_SYSTEM);
        } else {
            updateEntityPhase(ImageTransferPhase.CANCELLED_SYSTEM);
        }
    }

    private void updateEntity(ImageTransfer updates) {
        updateEntity(updates, false);
    }

    private void updateEntity(ImageTransfer updates, boolean clearResourceId) {
        imageTransferUpdater.updateEntity(updates, getCommandId(), clearResourceId);
    }

    private int getHostTicketRefreshAllowance() {
        return Config.<Integer>getValue(ConfigValues.ImageTransferHostTicketRefreshAllowanceInSeconds);
    }

    private int getHostTicketLifetime() {
        return Config.<Integer>getValue(ConfigValues.ImageTransferHostTicketValidityInSeconds);
    }

    private int getClientTicketLifetime() {
        return Config.<Integer>getValue(ConfigValues.ImageTransferClientTicketValidityInSeconds);
    }

    private int getPauseLogInterval() {
        return Config.<Integer>getValue(ConfigValues.ImageTransferPausedLogIntervalInSeconds);
    }

    private int getTransferImageClientInactivityTimeoutInSeconds() {
        return Config.<Integer>getValue(ConfigValues.TransferImageClientInactivityTimeoutInSeconds);
    }

    public static String getProxyUri() {
        String engineHost = EngineLocalConfig.getInstance().getHost();
        if (proxyEnabled()) {
            return HTTPS_SCHEME + engineHost + ":" + PROXY_DATA_PORT;
        } else {
            return getImageDaemonUri(engineHost);
        }
    }

    private static String getImageDaemonUri(String daemonHostname) {
        String port = Config.getValue(ConfigValues.ImageDaemonPort);
        return HTTPS_SCHEME + daemonHostname + ":" + port;
    }

    private void setAuditLogTypeFromPhase(ImageTransferPhase phase) {
        if (getParameters().getAuditLogType() != null) {
            // Some flows, e.g. cancellation, may set the log type more than once.
            // In this case, the first type is the most accurate.
            return;
        }

        if (phase == ImageTransferPhase.FINISHED_SUCCESS) {
            getParameters().setAuditLogType(AuditLogType.TRANSFER_IMAGE_SUCCEEDED);
        } else if (phase == ImageTransferPhase.CANCELLED_SYSTEM || phase == ImageTransferPhase.CANCELLED_USER) {
            getParameters().setAuditLogType(AuditLogType.TRANSFER_IMAGE_CANCELLED);
        } else if (phase == ImageTransferPhase.FINISHED_FAILURE) {
            getParameters().setAuditLogType(AuditLogType.TRANSFER_IMAGE_FAILED);
        } else if (phase == ImageTransferPhase.FINISHED_CLEANUP) {
            getParameters().setAuditLogType(AuditLogType.TRANSFER_IMAGE_CLEANED_UP);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("DiskAlias", getImageAlias());
        addCustomValue("TransferType", getParameters().getTransferType().name());
        return getActionState() == CommandActionState.EXECUTE
                ? AuditLogType.TRANSFER_IMAGE_INITIATED : getParameters().getAuditLogType();
    }

    // Return a string describing the transfer, safe for use before the new image
    // is successfully created; e.g. "disk 'NewDisk' (id '<uuid>')".
    private String getTransferDescription() {
        return String.format("%s %s '%s' (disk id: '%s', image id: '%s')",
                getParameters().getTransferType().name(),
                IMAGE_TYPE,
                getImageAlias(),
                getDiskImage().getId(),
                getDiskImage().getImageId());
    }

    private ImageioClient getProxyClient() {
        if (proxyClient == null) {
            proxyClient = new ImageioClient("localhost", PROXY_CONTROL_PORT);
        }
        return proxyClient;
    }

    private Guid getBackupDiskSnapshotId() {
        if (Guid.isNullOrEmpty(backupDiskSnapshotId)) {
            backupDiskSnapshotId = vmBackupDao.getDiskSnapshotIdForBackup(getParameters().getBackupId(),
                    getParameters().getImageGroupID());
        }

        return backupDiskSnapshotId;
    }

    @Override
    protected void endSuccessfully() {
        log.info("Successfully transferred disk '{}' for image transfer '{}'",
                getParameters().getImageId(), getCommandId());
        if (getParameters().getTransferType() == TransferType.Upload) {
            // Update image data in DB, set Qcow Compat, etc
            // (relevant only for upload)
            super.endSuccessfully();
        }
        updateEntityFinalPhase(ImageTransferPhase.FINISHED_SUCCESS);
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        log.error("Failed to transfer disk '{}' for image transfer '{}'", getParameters().getImageId(), getCommandId());
        if (getParameters().getTransferType() == TransferType.Upload) {
            // Do rollback only for upload - i.e. remove the disk added in 'createImage()'
            runInternalAction(ActionType.RemoveDisk, new RemoveDiskParameters(getParameters().getImageGroupID()));
        }
        updateEntityFinalPhase(ImageTransferPhase.FINISHED_FAILURE);
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    // Container for context needed by state machine handlers
    class StateContext {
        ImageTransfer entity;
        long iterationTimestamp;
        Guid childCmdId;
    }
}
// DE72501108006231412815
