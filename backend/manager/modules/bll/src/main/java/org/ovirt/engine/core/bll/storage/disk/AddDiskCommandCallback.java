package org.ovirt.engine.core.bll.storage.disk;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@Typed(AddDiskCommandCallback.class)
public class AddDiskCommandCallback extends ConcurrentChildCommandsExecutionCallback {

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    private ImageDao imageDao;

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {
        AddDiskCommand<AddDiskParameters> addDiskCommand = (AddDiskCommand<AddDiskParameters>) command;

        if (addDiskCommand.getChildActionType() != ActionType.AddImageFromScratch || anyFailed) {
            super.childCommandsExecutionEnded(command, anyFailed, childCmdIds, status, completedChildren);
            return;
        }

        DiskImage diskImage = (DiskImage) addDiskCommand.getParameters().getDiskInfo();
        log.info("Getting volume info for image '{}/{}'", diskImage.getId(), diskImage.getImageId());
        try {
            DiskImage fromVdsm = imagesHandler.getVolumeInfoFromVdsm(diskImage.getStoragePoolId(),
                    diskImage.getStorageIds().get(0),
                    diskImage.getId(),
                    diskImage.getImageId());
            if (fromVdsm.getSize() != diskImage.getSize()) {
                log.info("Updating size from '{}' to '{}'", diskImage.getSize(), fromVdsm.getSize());
                TransactionSupport.executeInNewTransaction(() -> {
                    imageDao.updateImageSize(fromVdsm.getImageId(), fromVdsm.getSize());

                    return null;
                });
            }
        } catch (Exception e) {
            log.error("Failed to get volume info", e);
            setCommandEndStatus(command, true, status, childCmdIds);
            throw e;
        }

        setCommandEndStatus(command, false, status, childCmdIds);
    }
}
