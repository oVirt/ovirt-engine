package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@LockIdNameAttribute
public class AddVmFromTemplateCommand<T extends AddVmFromTemplateParameters> extends AddVmCommand<T> {

    public AddVmFromTemplateCommand(T parameters) {
        super(parameters);
        parameters.setDontCheckTemplateImages(true);
    }

    protected AddVmFromTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validateIsImagesOnDomains() {
        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<String, Pair<String, String>>();
        Map<String, Pair<String, String>> parentLocks = super.getExclusiveLocks();
        if (parentLocks != null) {
            locks.putAll(parentLocks);
        }
        locks.put(getVmTemplateId().toString(), LockMessagesMatchUtil.TEMPLATE);
        return locks;
    }

    @Override
    protected void executeVmCommand() {
        VmTemplateHandler.lockVmTemplateInTransaction(getVmTemplateId(), getCompensationContext());
        super.executeVmCommand();
        getParameters().OriginalTemplate = getVm().getVmtGuid();
        getVm().setVmtGuid(VmTemplateHandler.BlankVmTemplateId);
        getVm().getStaticData().setQuotaId(getParameters().getVmStaticData().getQuotaId());
        DbFacade.getInstance().getVmStaticDao().update(getVm().getStaticData());
        // if there are no tasks, we can end the command right away.
        if (getTaskIdList().isEmpty()) {
            endSuccessfully();
        }
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
                DiskImageBase diskInfo = getParameters().getDiskInfoDestinationMap().get(disk.getId());
                CreateCloneOfTemplateParameters p = new CreateCloneOfTemplateParameters(disk.getImageId(),
                        getParameters().getVmStaticData().getId(), diskInfo);
                p.setStorageDomainId(disk.getStorageIds().get(0));
                p.setDestStorageDomainId(diskInfoDestinationMap.get(disk.getId()).getStorageIds().get(0));
                p.setDiskAlias(diskInfoDestinationMap.get(disk.getId()).getDiskAlias());
                p.setVmSnapshotId(getVmSnapshotId());
                p.setParentCommand(VdcActionType.AddVmFromTemplate);
                p.setParentParameters(getParameters());
                p.setEntityId(getParameters().getEntityId());
                p.setQuotaId(diskInfoDestinationMap.get(disk.getId()).getQuotaId() != null ? diskInfoDestinationMap.get(disk.getId())
                        .getQuotaId()
                        : null);
                VdcReturnValueBase result = Backend.getInstance().runInternalAction(
                                VdcActionType.CreateCloneOfTemplate,
                                p,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
                getParameters().getImagesParameters().add(p);

                /**
                 * if couldnt create snapshot then stop the transaction and the command
                 */
                if (!result.getSucceeded()) {
                    throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
                } else {
                    getTaskIdList().addAll(result.getInternalTaskIdList());
                    newDiskImages.add((DiskImage) result.getActionReturnValue());
                }
            }
        }
        return true;
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

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        VmTemplateHandler.UnLockVmTemplate(getParameters().OriginalTemplate);
    }

    @Override
    protected void endWithFailure() {
        super.endWithFailure();
        VmTemplateHandler.UnLockVmTemplate(getParameters().OriginalTemplate);
    }
}
