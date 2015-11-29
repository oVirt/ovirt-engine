package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RemoveCinderSnapshotDiskCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {

    public RemoveCinderSnapshotDiskCommand(T parameters) {
        super(parameters);
    }

    public RemoveCinderSnapshotDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        deleteSnapshot();
        persistCommand(getParameters().getParentCommand(), true);
        getReturnValue().setActionReturnValue(getImageId());
        setSucceeded(true);
    }

    private void deleteSnapshot() {
        getCinderBroker().deleteSnapshot(getImageId());
    }

    public Guid getStorageDomainId() {
        return getDiskImage().getStorageIds().get(0);
    }

    @Override
    protected void endWithFailure() {
        if (getDestinationDiskImage() != null) {
            // If the cinder disk has a snapshot and it is not a part of a template.
            if ((!getDestinationDiskImage().getParentId().equals(Guid.Empty))
                    && (!getDestinationDiskImage().getParentId().equals(getDestinationDiskImage().getImageTemplateId()))) {
                DiskImage previousSnapshot = getDiskImageDao().getSnapshotById(getDestinationDiskImage().getParentId());
                previousSnapshot.setActive(true);
                getImageDao().update(previousSnapshot.getImage());
            }
        }
        super.endWithFailure();
    }

    @Override
    protected void endSuccessfully() {
        if (getDestinationDiskImage() != null) {
            DiskImage curr = getDestinationDiskImage();

            // Set the parent snapshot to be dependent on the current snapshot descendant id.
            List<DiskImage> orderedCinderSnapshots = getDiskImageDao().getAllSnapshotsForParent(curr.getImageId());
            if (!orderedCinderSnapshots.isEmpty()) {
                DiskImage volumeBasedOnsnapshot = orderedCinderSnapshots.get(0);
                volumeBasedOnsnapshot.setParentId(curr.getParentId());
                getImageDao().update(volumeBasedOnsnapshot.getImage());
            }
            getImageDao().remove(curr.getImageId());
        }
        setSucceeded(true);
    }

    @Override
    public void rollback() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                if (!getParameters().isLeaveLocked()) {
                    DiskImage diskImage = getImage();
                    if (diskImage != null) {
                        getImageDao().updateStatus(diskImage.getImage().getId(), ImageStatus.OK);
                    }
                    unLockImage();
                }
                return null;
            }
        });
    }

    @Override
    public CommandCallback getCallback() {
        return new RemoveCinderSnapshotCommandCallback();
    }

}
