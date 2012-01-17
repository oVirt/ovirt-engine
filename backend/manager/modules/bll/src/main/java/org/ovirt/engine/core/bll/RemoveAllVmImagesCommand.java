package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command removes all Vm images and all created snapshots both from Irs
 * and Db.
 */
@InternalCommandAttribute
public class RemoveAllVmImagesCommand<T extends RemoveAllVmImagesParameters> extends VmCommand<T> {
    public RemoveAllVmImagesCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getVmId());
    }

    @Override
    protected void ExecuteVmCommand() {
        java.util.ArrayList<Guid> mImagesToBeRemoved = new java.util.ArrayList<Guid>();
        List<DiskImage> images = getParameters().Images;
        if (images == null) {
            images = DbFacade.getInstance().getDiskImageDAO().getAllForVm(getVmId());
        }
        for (DiskImage image : images) {
            if (image.getactive() != null && image.getactive()) {
                mImagesToBeRemoved.add(image.getId());
            }
        }

        boolean noImagesRemovedYet = true;
        for (DiskImage image : images) {
            if (mImagesToBeRemoved.contains(image.getId())) {
                RemoveImageParameters tempVar = new RemoveImageParameters(image.getId(), mImagesToBeRemoved,
                        getVmId());
                tempVar.setParentCommand(getParameters().getParentCommand());
                tempVar.setParentParemeters(getParameters().getParentParameters());
                tempVar.setDiskImage(image);
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setForceDelete(getParameters().getForceDelete());
                tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                tempVar.setParentParemeters(getParameters());
                VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(VdcActionType.RemoveImage,
                        tempVar);

                if (vdcReturnValue.getSucceeded()) {
                    getReturnValue().getInternalTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                } else {
                    if (noImagesRemovedYet) {
                        setSucceeded(false);
                        getReturnValue().setFault(vdcReturnValue.getFault());
                        return;
                    }

                    log.errorFormat("Can't remove image id: {0} for VM id: {1} due to: {2}.",
                            image.getId(), getParameters().getVmId(),
                            vdcReturnValue.getFault().getMessage());
                }

                noImagesRemovedYet = false;
            }
        }

        setSucceeded(true);
    }

    @Override
    protected void EndVmCommand() {
        setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(RemoveAllVmImagesCommand.class);
}
