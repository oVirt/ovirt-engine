package org.ovirt.engine.core.bll.storage.disk.image;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command removes all Vm images and all created snapshots both from Irs
 * and Db.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveAllVmImagesCommand<T extends RemoveAllVmImagesParameters> extends VmCommand<T> {

    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private ImageDao imageDao;

    public RemoveAllVmImagesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        Set<Guid> imagesToBeRemoved = new HashSet<>();
        List<DiskImage> images = getParameters().getImages();
        if (images == null) {
            images = DisksFilter.filterImageDisks(diskDao.getAllForVm(getVmId()), ONLY_NOT_SHAREABLE, ONLY_ACTIVE);
        }
        for (DiskImage image : images) {
            if (Boolean.TRUE.equals(image.getActive())) {
                imagesToBeRemoved.add(image.getImageId());
            }
        }

        Collection<DiskImage> failedRemoving = new LinkedList<>();
        for (final DiskImage image : images) {
            if (imagesToBeRemoved.contains(image.getImageId())) {
                ActionReturnValue actionReturnValueValue =
                        runInternalActionWithTasksContext(
                                ActionType.RemoveImage,
                                buildRemoveImageParameters(image)
                        );

                if (actionReturnValueValue.getSucceeded()) {
                    (isExecutedAsChildCommand() ? getReturnValue().getInternalVdsmTaskIdList() : getTaskIdList())
                            .addAll(actionReturnValueValue.getInternalVdsmTaskIdList());
                } else {
                    StorageDomain domain = storageDomainDao.get(image.getStorageIds().get(0));
                    failedRemoving.add(image);
                    log.error("Can't remove image id '{}' for VM id '{}' from domain id '{}' due to: {}.",
                            image.getImageId(),
                            getParameters().getVmId(),
                            image.getStorageIds().get(0),
                            actionReturnValueValue.getFault().getMessage());

                    if (domain.getStorageDomainType() == StorageDomainType.Data) {
                        log.info("Image id '{}' will be set at illegal state with no snapshot id.", image.getImageId());
                        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                                () -> {
                                    // If VDSM task didn't succeed to initiate a task we change the disk to at illegal
                                    // state.
                                    updateDiskImagesToIllegal(image);
                                    return true;
                                });
                    } else {
                        log.info("Image id '{}' is not on a data domain and will not be marked as illegal.", image.getImageId());
                    }
                }
            }
        }

        setActionReturnValue(failedRemoving);
        setSucceeded(true);
    }

    private RemoveImageParameters buildRemoveImageParameters(DiskImage image) {
        RemoveImageParameters result = new RemoveImageParameters(image.getImageId());
        boolean parentExists = isExecutedAsChildCommand();
        result.setParentCommand(parentExists ? getParameters().getParentCommand() : getActionType());
        result.setParentParameters(parentExists ? getParameters().getParentParameters() : getParameters());
        result.setDiskImage(image);
        result.setEntityInfo(getParameters().getEntityInfo());
        result.setForceDelete(getParameters().getForceDelete());
        result.setShouldLockImage(false);
        return result;
    }

    /**
     * Update all disks images of specific disk image to illegal state, and set the vm snapshot id to null, since now
     * they are not connected to any VM.
     *
     * @param diskImage - The disk to update.
     */
    private void updateDiskImagesToIllegal(DiskImage diskImage) {
        List<DiskImage> snapshotDisks = diskImageDao.getAllSnapshotsForImageGroup(diskImage.getId());
        for (DiskImage diskSnapshot : snapshotDisks) {
            diskSnapshot.setVmSnapshotId(null);
            diskSnapshot.setImageStatus(ImageStatus.ILLEGAL);
            imageDao.update(diskSnapshot.getImage());
        }
    }

    @Override
    protected void endVmCommand() {
        if (isExecutedAsChildCommand()) {
            // parent command ends the actions on disks
            setSucceeded(true);
        } else {
            super.endVmCommand();
        }
    }
}
