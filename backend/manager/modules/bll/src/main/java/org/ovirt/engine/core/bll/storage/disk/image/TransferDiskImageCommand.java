package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.tasks.CommandHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.PrepareImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

@NonTransactiveCommandAttribute
public class TransferDiskImageCommand<T extends TransferDiskImageParameters> extends TransferImageCommand<T> implements QuotaStorageDependent {

    private static final String FILE_URL_SCHEME = "file://";

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private DiskDao diskDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VmDao vmDao;

    public TransferDiskImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
        addValidationMessage(EngineMessage.VAR__ACTION__TRANSFER);
    }

    @Override
    protected boolean validateCreateImage() {
        ActionReturnValue returnValue = CommandHelper.validate(ActionType.AddDisk, getAddDiskParameters(),
                getContext().clone());
        getReturnValue().setValidationMessages(returnValue.getValidationMessages());
        return returnValue.isValid();
    }

    @Override
    protected void createImage() {
        runInternalAction(ActionType.AddDisk, getAddDiskParameters(), cloneContextAndDetachFromParent());
    }

    @Override
    protected String prepareImage(Guid vdsId) {
        VDSReturnValue vdsRetVal = runVdsCommand(VDSCommandType.PrepareImage,
                    getPrepareParameters(vdsId));
        return FILE_URL_SCHEME + ((PrepareImageReturn) vdsRetVal.getReturnValue()).getImagePath();
    }

    @Override
    protected boolean validateImageTransfer() {
        DiskImage diskImage = getDiskImage();
        DiskValidator diskValidator = getDiskValidator(diskImage);
        DiskImagesValidator diskImagesValidator = getDiskImagesValidator(diskImage);
        StorageDomainValidator storageDomainValidator = getStorageDomainValidator(
                storageDomainDao.getForStoragePool(diskImage.getStorageIds().get(0), diskImage.getStoragePoolId()));
        return validate(diskValidator.isDiskExists())
                && validateActiveDiskPluggedToAnyNonDownVm(diskImage, diskValidator)
                && validate(diskImagesValidator.diskImagesNotIllegal())
                && validate(diskImagesValidator.diskImagesNotLocked())
                && validate(storageDomainValidator.isDomainExistAndActive());
    }

    private boolean validateActiveDiskPluggedToAnyNonDownVm(DiskImage diskImage, DiskValidator diskValidator) {
        return diskImage.isDiskSnapshot() || validate(diskValidator.isDiskPluggedToAnyNonDownVm(false));
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

    @Override
    protected void tearDownImage(Guid vdsId) {
        try {
            VDSReturnValue vdsRetVal = runVdsCommand(VDSCommandType.TeardownImage,
                    getImageActionsParameters(vdsId));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
        } catch (EngineException e) {
            DiskImage image = getDiskImage();
            log.warn("Failed to tear down image '{}' for image transfer session: {}",
                    image, e.getVdsError());

            // Invoke log method directly rather than relying on infra, because teardown
            // failure may occur during command execution, e.g. if the upload is paused.
            addCustomValue("DiskAlias", image != null ? image.getDiskAlias() : "(unknown)");
            auditLogDirector.log(this, AuditLogType.TRANSFER_IMAGE_TEARDOWN_FAILED);
        }
    }

    private AddDiskParameters getAddDiskParameters() {
        AddDiskParameters diskParameters = getParameters().getAddDiskParameters();
        diskParameters.setParentCommand(getActionType());
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setSkipDomainCheck(true);
        return diskParameters;
    }

    protected ImageActionsVDSCommandParameters getImageActionsParameters(Guid vdsId) {
        return new ImageActionsVDSCommandParameters(vdsId,
                getStoragePool().getId(),
                getStorageDomainId(),
                getDiskImage().getId(),
                getDiskImage().getImageId());
    }

    @Override
    protected String getImageAlias() {
        return  getParameters().getAddDiskParameters() != null ?
                getParameters().getAddDiskParameters().getDiskInfo().getDiskAlias() :
                getDiskImage().getDiskAlias();
    }

    @Override
    protected String getImageType() {
        return "disk";
    }

    protected DiskImage getDiskImage() {
        if (!Guid.isNullOrEmpty(getParameters().getImageId())) {
            setImageId(getParameters().getImageId());
            return super.getDiskImage();
        }
        return (DiskImage) diskDao.get(getParameters().getImageGroupID());
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        if (getParameters().getAddDiskParameters() != null) {
            AddDiskParameters parameters = getAddDiskParameters();
            list.add(new QuotaStorageConsumptionParameter(
                    ((DiskImage) parameters.getDiskInfo()).getQuotaId(),
                    null,
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
        if (getDiskImage() != null) {
            locks.put(getDiskImage().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_DISK_IS_LOCKED));
        }
        if (!Guid.isNullOrEmpty(getParameters().getImageId())) {
            List<VM> vms = vmDao.getVmsListForDisk(getDiskImage().getId(), true);
            vms.forEach(vm -> locks.put(vm.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED)));
        }
        return locks;
    }
}
