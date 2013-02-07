package org.ovirt.engine.core.bll;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command removes all Vm images and all created snapshots both from Irs
 * and Db.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveAllVmImagesCommand<T extends RemoveAllVmImagesParameters> extends VmCommand<T> {

    private static final long serialVersionUID = 3577196516027044528L;

    public RemoveAllVmImagesCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        Set<Guid> mImagesToBeRemoved = new HashSet<Guid>();
        List<DiskImage> images = getParameters().Images;
        if (images == null) {
            images =
                    ImagesHandler.filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(getVmId()),
                            true,
                            false);
        }
        for (DiskImage image : images) {
            if (Boolean.TRUE.equals(image.getactive())) {
                mImagesToBeRemoved.add(image.getImageId());
            }
        }

        boolean noImagesRemovedYet = true;
        for (final DiskImage image : images) {
            if (mImagesToBeRemoved.contains(image.getImageId())) {
                RemoveImageParameters tempVar = new RemoveImageParameters(image.getImageId());
                tempVar.setParentCommand(getParameters().getParentCommand());
                tempVar.setParentParameters(getParameters().getParentParameters());
                tempVar.setDiskImage(image);
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setForceDelete(getParameters().getForceDelete());
                VdcReturnValueBase vdcReturnValue =
                        Backend.getInstance().runInternalAction(VdcActionType.RemoveImage,
                                tempVar,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

                if (vdcReturnValue.getSucceeded()) {
                    getReturnValue().getInternalTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                } else {
                    if (noImagesRemovedYet) {
                        setSucceeded(false);
                        getReturnValue().setFault(vdcReturnValue.getFault());
                        return;
                    }

                    log.errorFormat("Can't remove image id: {0} for VM id: {1} due to: {2}. Image will be set at illegal state with no snapshot id.",
                            image.getImageId(),
                            getParameters().getVmId(),
                            vdcReturnValue.getFault().getMessage());

                    TransactionSupport.executeInScope(TransactionScopeOption.Required,
                            new TransactionMethod<Object>() {
                                @Override
                                public Object runInTransaction() {
                                    // If VDSM task didn't succeed to initiate a task we change the disk to at illegal
                                    // state.
                                    updateDiskImagesToIllegal(image);
                                    return true;
                                }
                            });
                }
                noImagesRemovedYet = false;
            }
        }

        setSucceeded(true);
    }

    /**
     * Update all disks images of specific disk image to illegal state, and set the vm snapshot id to null, since now
     * they are not connected to any VM.
     *
     * @param diskImage - The disk to update.
     */
    private void updateDiskImagesToIllegal(DiskImage diskImage) {
        List<DiskImage> snapshotDisks =
                getDbFacade().getDiskImageDao().getAllSnapshotsForImageGroup(diskImage.getId());
        for (DiskImage diskSnapshot : snapshotDisks) {
            diskSnapshot.setvm_snapshot_id(null);
            diskSnapshot.setImageStatus(ImageStatus.ILLEGAL);
            getDbFacade().getImageDao().update(diskSnapshot.getImage());
        }
    }

    @Override
    protected void endVmCommand() {
        setSucceeded(true);
    }
}
