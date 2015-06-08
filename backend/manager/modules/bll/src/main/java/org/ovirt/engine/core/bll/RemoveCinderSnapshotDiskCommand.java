package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.RemoveCinderSnapshotCommandCallback;
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
        super.endWithFailure();
    }

    @Override
    protected void endSuccessfully() {
        if (getDestinationDiskImage() != null) {
            DiskImage curr = getDestinationDiskImage();
            DiskImage volumeBasedOnsnapshot = getDiskImageDao().getAllSnapshotsForLeaf(curr.getImageId()).get(0);
            getImageDao().remove(curr.getImageId());
            volumeBasedOnsnapshot.setParentId(curr.getParentId());
            getBaseDiskDao().update(volumeBasedOnsnapshot);
            getImageDao().update(volumeBasedOnsnapshot.getImage());
        }
        if (!getParameters().isParentHasTasks()) {
            getBackend().endAction(getParameters().getParentCommand(),
                    getParameters().getParentParameters(),
                    null);
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
