package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.CreateCloneOfTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@LockIdNameAttribute(isReleaseAtEndOfExecute = false)
public class AddVmFromTemplateCommand<T extends AddVmFromTemplateParameters> extends AddVmCommand<T> {

    public AddVmFromTemplateCommand(T parameters) {
        super(parameters);
    }

    protected AddVmFromTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validateIsImagesOnDomains() {
        return true;
    }

    @Override
    protected void executeVmCommand() {
        super.executeVmCommand();
        getParameters().OriginalTemplate = getVm().getVmtGuid();
        getVm().setVmtGuid(VmTemplateHandler.BlankVmTemplateId);
        getVm().getStaticData().setQuotaId(getParameters().getVmStaticData().getQuotaId());
        DbFacade.getInstance().getVmStaticDao().update(getVm().getStaticData());
        // if there are no tasks, we can end the command right away.
        if (getTaskIdList().isEmpty()) {
            endSuccessfully();
        }
        checkTrustedService();
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmName", getVmName());
        logable.addCustomValue("VmTemplateName", getVmTemplateName());
        if (getVmTemplate().isTrustedService() && !getVm().isTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVmTemplate().isTrustedService() && getVm().isTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    /**
     * TODO: need to see why those checks are not executed
     * for this command
     */
    @Override
    protected boolean checkTemplateImages(List<String> reasons) {
        return true;
    }

    @Override
    protected boolean addVmImages() {
        if (getVmTemplate().getDiskMap().size() > 0) {
            if (getVm().getStatus() != VMStatus.Down) {
                log.error("Cannot add images. VM is not Down");
                throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
            }
            VmHandler.LockVm(getVm().getDynamicData(), getCompensationContext());
            for (DiskImage disk : getVmTemplate().getDiskMap().values()) {
                VdcReturnValueBase result = Backend.getInstance().runInternalAction(
                                VdcActionType.CreateCloneOfTemplate,
                                buildCreateCloneOfTemplateParameters(disk),
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

                // if couldnt create snapshot then stop the transaction and the command
                if (!result.getSucceeded()) {
                    throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
                } else {
                    getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
                    DiskImage newImage = (DiskImage) result.getActionReturnValue();
                    getSrcDiskIdToTargetDiskIdMapping().put(disk.getId(), newImage.getId());
                }
            }
        }
        return true;
    }

    private CreateCloneOfTemplateParameters buildCreateCloneOfTemplateParameters(DiskImage disk) {
        DiskImageBase diskInfo = getParameters().getDiskInfoDestinationMap().get(disk.getId());
        CreateCloneOfTemplateParameters params = new CreateCloneOfTemplateParameters(disk.getImageId(),
                getParameters().getVmStaticData().getId(), diskInfo);
        params.setStorageDomainId(disk.getStorageIds().get(0));
        params.setDestStorageDomainId(diskInfoDestinationMap.get(disk.getId()).getStorageIds().get(0));
        params.setDiskAlias(diskInfoDestinationMap.get(disk.getId()).getDiskAlias());
        params.setVmSnapshotId(getVmSnapshotId());
        params.setParentCommand(VdcActionType.AddVmFromTemplate);
        params.setParentParameters(getParameters());
        params.setEntityInfo(getParameters().getEntityInfo());
        params.setQuotaId(diskInfoDestinationMap.get(disk.getId()).getQuotaId() != null ?
                diskInfoDestinationMap.get(disk.getId()).getQuotaId() : null);
        return params;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = super.canDoAction();
        if (retValue) {
            for (DiskImage dit : getVmTemplate().getDiskMap().values()) {
                retValue =
                        ImagesHandler.CheckImageConfiguration(destStorages.get(diskInfoDestinationMap.get(dit.getId()).getStorageIds().get(0))
                                .getStorageStaticData(),
                                diskInfoDestinationMap.get(dit.getId()),
                                getReturnValue().getCanDoActionMessages());
                if (!retValue) {
                    break;
                }
            }
        }
        return retValue;
    }

    @Override
    protected int getNeededDiskSize(Guid storageId) {
        double actualSize = 0;
        List<DiskImage> disks = storageToDisksMap.get(storageId);
        for (DiskImage disk : disks) {
            actualSize += disk.getActualSize();
        }
        return (int) actualSize;
    }
}
