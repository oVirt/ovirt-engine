package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDeviceDAO;

@NonTransactiveCommandAttribute
public class MoveOrCopyDiskCommand<T extends MoveOrCopyImageGroupParameters> extends MoveOrCopyImageGroupCommand<T> {

    private static final long serialVersionUID = -7219975636530710384L;
    private boolean isVmFound = false;

    public MoveOrCopyDiskCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__COPY);
        } else {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MOVE);
        }
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected boolean canDoAction() {
        ArrayList<String> canDoActionMessages = getReturnValue().getCanDoActionMessages();
        return isImageExist()
                && isImageIsNotLocked()
                && checkOperationIsCorrect()
                && canFindVmOrTemplate()
                && isSourceAndDestTheSame()
                && validateSourceStorageDomain(canDoActionMessages)
                && validateDestStorage(canDoActionMessages)
                && checkTemplateInDestStorageDomain()
                && validateSpaceRequirements()
                && ImagesHandler.CheckImageConfiguration(getStorageDomain().getStorageStaticData(),
                        getImage(),
                        canDoActionMessages)
                && checkCanBeMoveInVm() && checkIfNeedToBeOverride();
    }

    protected boolean isSourceAndDestTheSame() {
        if (getParameters().getOperation() == ImageOperation.Move
                    && getParameters().getSourceDomainId().equals(getParameters().getStorageDomainId())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME);
            return false;
        }
        return true;
    }

    protected boolean isImageExist() {
        if (getImage() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
            return false;
        }
        return true;
    }

    protected boolean isImageIsNotLocked() {
        if (getImage().getimageStatus() == ImageStatus.LOCKED) {
            if (getParameters().getOperation() == ImageOperation.Move) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_LOCKED);
            } else {
                addCanDoActionMessage(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
            }
            return false;
        }
        return true;
    }

    /**
     * The following method will perform a check for correctness of operation
     * It is allow to copy only if it is image that belongs to template and
     * it is allow to move only if it is image of disk
     * @return
     */
    protected boolean checkOperationIsCorrect() {
        boolean retValue = true;
        if (getParameters().getOperation() == ImageOperation.Copy
                && getImage().getVmEntityType() == VmEntityType.VM) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_TEMPLATE_DISK);
            retValue = false;
        }

        if (retValue && getParameters().getOperation() == ImageOperation.Move
                && getImage().getVmEntityType() == VmEntityType.TEMPLATE) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK);
            retValue = false;
        }
        return retValue;
    }

    protected boolean validateDestStorage(ArrayList<String> canDoActionMessages) {
        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        return validator.isDomainExistAndActive(canDoActionMessages)
                            && validator.domainIsValidDestination(canDoActionMessages);
    }

    /**
     * Check if destination storage has enough space
     * @return
     */
    protected boolean validateSpaceRequirements() {
        boolean retValue = true;
        if (!StorageDomainSpaceChecker.isBelowThresholds(getStorageDomain())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
        }

        if (retValue) {
            getImage().getSnapshots().addAll(
                            ImagesHandler.getAllImageSnapshots(getImage().getId(),
                                    getImage().getit_guid()));
            if (!StorageDomainSpaceChecker.hasSpaceForRequest(getStorageDomain(),
                            Math.round(getImage().getActualDiskWithSnapshotsSize()))) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
                retValue = false;
            }
        }
        return retValue;
    }

    protected boolean checkIfNeedToBeOverride() {
        if (getParameters().getOperation() == ImageOperation.Copy && !getParameters().getForceOverride()
                && getImage().getstorage_ids().contains(getStorageDomain().getId())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_ALREADY_EXISTS);
            return false;
        }
        return true;
    }

    /**
     * Validate a source storage domain of image, when a source storage domain is not provided
     * any of the domains image will be used
     * @param canDoActionMessages
     * @return
     */
    protected boolean validateSourceStorageDomain(ArrayList<String> canDoActionMessages) {
        NGuid sourceDomainId = getParameters().getSourceDomainId();
        if (sourceDomainId == null || Guid.Empty.equals(sourceDomainId)) {
            sourceDomainId = getImage().getstorage_ids().get(0);
            getParameters().setSourceDomainId(sourceDomainId);
        }
        StorageDomainValidator validator =
                    new StorageDomainValidator(getStorageDomainDAO().getForStoragePool(sourceDomainId.getValue(),
                            getImage().getstorage_pool_id()));
        return validator.isDomainExistAndActive(canDoActionMessages);
    }

    /**
     * If a disk is attached to VM it can be moved when it is unplugged or at case that disk is plugged
     * vm should be down
     * @return
     */
    protected boolean checkCanBeMoveInVm() {
        boolean retValue = true;
        if (isVmFound && VMStatus.Down != getVm().getstatus()) {
            VmDevice vmDevice =
                    getVmDeviceDAO().get(new VmDeviceId(getImage().getId(), getVm().getId()));
            if (vmDevice.getIsPlugged()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
                retValue = false;
            }
        }
        return retValue;
    }

    /**
     * The following method will check, if we can move disk to destination storage domain, when
     * it is based on template
     * @return
     */
    protected boolean checkTemplateInDestStorageDomain() {
        boolean retValue = true;
        if (getParameters().getOperation() == ImageOperation.Move
                && !ImagesHandler.BlankImageTemplateId.equals(getImage().getParentId())) {
            DiskImage templateImage = getDiskImageDao().get(getImage().getParentId());
            if (!templateImage.getstorage_ids().contains(getParameters().getStorageDomainId())) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }
        return retValue;
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return DbFacade.getInstance().getVmDeviceDAO();
    }

    @Override
    protected void executeCommand() {
        overrideParameters();
        VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                        VdcActionType.MoveOrCopyImageGroup,
                        getParameters(),
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        if (!vdcRetValue.getSucceeded()) {
            setSucceeded(false);
            getReturnValue().setFault(vdcRetValue.getFault());
        } else {
            getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK
                    : (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_FAILED_MOVED_VM_DISK
                            : AuditLogType.USER_FAILED_COPY_TEMPLATE_DISK;

        case END_SUCCESS:
            return getSucceeded() ? (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_SUCCESS
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_SUCCESS
                    : (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE
                            : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_FAILURE;

        default:
            return (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_FAILURE;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // TODO: change to permissions of disk when will be implemented
        DiskImage image = getImage();
        Guid guid = Guid.Empty;
        if (image != null) {
            guid = image.getvm_guid();
        }
        if (getParameters().getOperation() == ImageOperation.Copy) {
            return Collections.singletonList(new PermissionSubject(guid,
                    VdcObjectType.VmTemplate,
                    ActionGroup.COPY_TEMPLATE));
        } else {
            return Collections.singletonList(new PermissionSubject(guid,
                    VdcObjectType.VM,
                    ActionGroup.MOVE_VM));
        }
    }

    /**
     * The following method will override a parameters which are not relevant for the MoveOrCopyDiskCommand to the
     * correct values for these scenario in order to be used at parent class
     */
    private void overrideParameters() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            getParameters().setUseCopyCollapse(true);
            getParameters().setAddImageDomainMapping(true);
        } else {
            getParameters().setUseCopyCollapse(false);
        }
        getParameters().setDestinationImageId(getImageId());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setDestImageGroupId(getImageGroupId());
        getParameters().setVolumeFormat(getDiskImage().getvolume_format());
        getParameters().setVolumeType(getDiskImage().getvolume_type());
        getParameters().setCopyVolumeType(CopyVolumeType.SharedVol);
        getParameters().setParentCommand(getActionType());
        getParameters().setParentParemeters(getParameters());
    }

    /**
     * The following method will determine if a provided vm/template exists
     * @param retValue
     * @return
     */
    private boolean canFindVmOrTemplate() {
        boolean retValue = true;
        if (getParameters().getOperation() == ImageOperation.Copy) {
            VmTemplate template = getVmTemplateDAO().get(getImage().getvm_guid());
            if (template == null) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
                retValue = false;
            } else {
                setVmTemplate(template);
            }
        } else if (!Guid.Empty.equals(getImage().getvm_guid())) {
            VM vm = getVmDAO().get(getImage().getvm_guid());
            isVmFound = true;
            setVm(vm);
        }
        return retValue;
    }
}
