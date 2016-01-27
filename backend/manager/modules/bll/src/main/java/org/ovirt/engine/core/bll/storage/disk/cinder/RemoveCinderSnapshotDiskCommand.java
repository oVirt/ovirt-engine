package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.NoOpTransactionCompletionListener;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RemoveCinderSnapshotDiskCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {

    public RemoveCinderSnapshotDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        registerRollbackHandler(new CustomTransactionCompletionListener());

        deleteSnapshot();
        persistCommand(getParameters().getParentCommand(), true);
        getReturnValue().setActionReturnValue(getImageId());
        setSucceeded(true);
    }

    private void deleteSnapshot() {
        getCinderBroker().deleteSnapshot(getImageId());
    }

    @Override
    public Guid getStorageDomainId() {
        return getDiskImage().getStorageIds().get(0);
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
    public CommandCallback getCallback() {
        return new RemoveCinderSnapshotCommandCallback();
    }

    private class CustomTransactionCompletionListener extends NoOpTransactionCompletionListener {
        @Override
        public void onRollback() {
            TransactionSupport.executeInNewTransaction(() -> {
                if (!getParameters().isLeaveLocked()) {
                    DiskImage diskImage = getImage();
                    if (diskImage != null) {
                        getImageDao().updateStatus(diskImage.getImage().getId(), ImageStatus.OK);
                    }
                    unLockImage();
                }
                return null;
            });
        }
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getStorageDomainId()));
    }
}
