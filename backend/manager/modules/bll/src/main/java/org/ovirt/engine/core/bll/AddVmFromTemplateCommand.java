package org.ovirt.engine.core.bll;

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


public class AddVmFromTemplateCommand<T extends AddVmFromTemplateParameters> extends AddVmCommand<T> {
    public AddVmFromTemplateCommand(T parameters) {
        super(parameters);
        parameters.setDontCheckTemplateImages(true);
    }

    protected AddVmFromTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void ExecuteVmCommand() {
        super.ExecuteVmCommand();
        // override template id to blank
        getParameters().OriginalTemplate = getVm().getvmt_guid();
        VmTemplateHandler.lockVmTemplateInTransaction(getParameters().OriginalTemplate, getCompensationContext());
        getVm().setvmt_guid(VmTemplateHandler.BlankVmTemplateId);
        getVm().getStaticData().setQuotaId(getParameters().getQuotaId());
        DbFacade.getInstance().getVmStaticDAO().update(getVm().getStaticData());
    }

    @Override
    protected boolean AddVmImages() {
        if (getVmTemplate().getDiskMap().size() > 0) {
            if (getVm().getstatus() != VMStatus.Down) {
                log.error("Cannot add images. VM is not Down");
                throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
            }
            VmHandler.LockVm(getVm().getDynamicData(), getCompensationContext());
            for (DiskImage dit : getVmTemplate().getDiskMap().values()) {
                DiskImageBase diskInfo = null;
                diskInfo = getParameters().getDiskInfoList().get(dit.getinternal_drive_mapping());
                CreateCloneOfTemplateParameters tempVar = new CreateCloneOfTemplateParameters(dit.getId(),
                        getParameters().getVmStaticData().getId(), diskInfo);
                DiskImage img = DbFacade.getInstance().getDiskImageDAO().get(dit.getId());
                tempVar.setStorageDomainId(img.getstorage_ids().get(0));
                tempVar.setVmSnapshotId(getVmSnapshotId());
                tempVar.setParentCommand(VdcActionType.AddVmFromTemplate);
                tempVar.setEntityId(getParameters().getEntityId());
                CreateCloneOfTemplateParameters p = tempVar;
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
                }
            }
        }
        return true;
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction();
        returnValue = returnValue
                && ImagesHandler.CheckImagesConfiguration(getStorageDomainId().getValue(),
                        new java.util.ArrayList<DiskImageBase>(getParameters().getDiskInfoList().values()),
                        getReturnValue().getCanDoActionMessages());
        return returnValue;
    }

    @Override
    protected int getNeededDiskSize(int count) {
        return (int)getVmTemplate().getActualDiskSize();
    }

    @Override
    protected void EndSuccessfully() {
        super.EndSuccessfully();
        VmTemplateHandler.UnLockVmTemplate(getParameters().OriginalTemplate);
    }

    @Override
    protected void EndWithFailure() {
        super.EndWithFailure();
        VmTemplateHandler.UnLockVmTemplate(getParameters().OriginalTemplate);
    }
}
