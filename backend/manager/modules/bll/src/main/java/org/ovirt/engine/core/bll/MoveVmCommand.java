package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.IsVmDuringInitiatingVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@Deprecated
@NonTransactiveCommandAttribute(forceCompensation = true)
public class MoveVmCommand<T extends MoveVmParameters> extends MoveOrCopyTemplateCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected MoveVmCommand(Guid commandId) {
        super(commandId);
    }

    public MoveVmCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getContainerId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
        setStoragePoolId(getVm().getStoragePoolId());
        setDescription(getVmName());
    }

    @Override
    protected ImageOperation getMoveOrCopyImageOperation() {
        return ImageOperation.Move;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected boolean canDoAction() {
        VmValidator vmValidator = new VmValidator(getVm());
        SnapshotsValidator snapshotValidator = new SnapshotsValidator();
        boolean retValue =
                validate(snapshotValidator.vmNotDuringSnapshot(getVmId()))
                && validate(snapshotValidator.vmNotInPreview(getVmId()))
                && validate(vmValidator.vmDown())
                && validate(vmValidator.vmNotHavingDeviceSnapshotsAttachedToOtherVms(false));

        // check that images are ok
        // not checking storage domain, there is a check in
        // checkTemplateInStorageDomain later
        VmHandler.updateDisksFromDb(getVm());
        List<DiskImage> diskImages = ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), false, false, true);
        List<DiskImage> diskImagesToValidate = ImagesHandler.filterImageDisks(diskImages, true, false, true);
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskImagesToValidate);
        retValue = retValue &&
                validate(new StoragePoolValidator(getStoragePool()).isUp()) &&
                validate(diskImagesValidator.diskImagesNotLocked()) &&
                validate(diskImagesValidator.diskImagesNotIllegal());

        ensureDomainMap(diskImages, getParameters().getStorageDomainId());
        for(DiskImage disk : diskImages) {
            imageFromSourceDomainMap.put(disk.getId(), disk);
        }

        retValue = retValue && checkTemplateInStorageDomain(diskImages);

        if (retValue
                && DbFacade.getInstance()
                        .getStoragePoolIsoMapDao()
                        .get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                                getVm().getStoragePoolId())) == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }

        if (retValue && getVm().getDiskMap().size() == 0) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS);
            retValue = false;
        }

        // update vm snapshots for storage free space check
        ImagesHandler.fillImagesBySnapshots(getVm());
        return retValue && validateSpaceRequirements(diskImagesToValidate);
    }

    protected boolean checkTemplateInStorageDomain(List<DiskImage> diskImages) {
        boolean retValue = checkStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active)
                && checkIfDisksExist(diskImages);
        if (retValue && !VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVm().getVmtGuid())) {
            List<DiskImage> imageList =
                    ImagesHandler.filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(getVm().getVmtGuid()),
                            false,
                            false, true);
            Map<Guid, DiskImage> templateImagesMap = new HashMap<Guid, DiskImage>();
            for (DiskImage image : imageList) {
                templateImagesMap.put(image.getImageId(), image);
            }
            for (DiskImage image : diskImages) {
                if (templateImagesMap.containsKey(image.getImageTemplateId())) {
                    if (!templateImagesMap.get(image.getImageTemplateId())
                            .getStorageIds()
                            .contains(getParameters().getStorageDomainId())) {
                        retValue = false;
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
                        break;
                    }
                }
            }
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        VM vm = getVm();
        if (vm.getStatus() != VMStatus.Down) {
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }
        // Check if vm is initializing to run or already running - if it is in
        // such state,
        // we cannot move the vm
        boolean isVmDuringInit = ((Boolean) runVdsCommand(VDSCommandType.IsVmDuringInitiating,
                        new IsVmDuringInitiatingVDSCommandParameters(vm.getId())).getReturnValue()).booleanValue();

        if (isVmDuringInit) {
            log.error("VM '{}' must be down for Move VM to be successfully executed", vm.getName());
            setActionReturnValue(vm.getStatus());
            setSucceeded(false);
            return;
        }

        VmHandler.lockVm(vm.getDynamicData(), getCompensationContext());
        moveOrCopyAllImageGroups();

        setSucceeded(true);

    }

    @Override
    protected void incrementDbGeneration() {
        getVmStaticDAO().incrementDbGeneration(getVm().getId());
    }

    @Override
    protected void moveOrCopyAllImageGroups() {
        moveOrCopyAllImageGroups(getVmId(), ImagesHandler.filterImageDisks(getVm().getDiskList(), false, false, true));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_MOVED_VM : AuditLogType.USER_FAILED_MOVE_VM;
        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_MOVED_VM_FINISHED_SUCCESS
                    : AuditLogType.USER_MOVED_VM_FINISHED_FAILURE;
        default:
            return AuditLogType.USER_MOVED_VM_FINISHED_FAILURE;
        }
    }

    protected void endMoveVmCommand() {
        boolean vmExists = (getVm() != null);
        if (vmExists) {
            incrementDbGeneration();
        }

        endActionOnAllImageGroups();

        if (vmExists) {
            VmHandler.unLockVm(getVm());

            VmHandler.updateDisksFromDb(getVm());
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("MoveVmCommand::EndMoveVmCommand: Vm is null - not performing full endAction");
        }

        setSucceeded(true);
    }

    @Override
    protected void endSuccessfully() {
        endMoveVmCommand();
    }

    @Override
    protected void endWithFailure() {
        endMoveVmCommand();
    }

    @Override
    protected VdcActionType getImagesActionType() {
        return VdcActionType.MoveImageGroup;
    }
}
