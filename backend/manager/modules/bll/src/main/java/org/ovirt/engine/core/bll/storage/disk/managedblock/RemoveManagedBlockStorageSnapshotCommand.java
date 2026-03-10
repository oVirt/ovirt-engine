package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockCommandParameters;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockExecutor;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveManagedBlockStorageSnapshotCommand<T extends ImagesContainterParametersBase> extends CommandBase<T> {

    @Inject
    private ManagedBlockExecutor managedBlockExecutor;

    @Inject
    private ManagedBlockStorageDao managedBlockStorageDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ImageDao imageDao;

    public RemoveManagedBlockStorageSnapshotCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public RemoveManagedBlockStorageSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = managedBlockStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getParameters().getImageId().toString());
        extraParams.add(getParameters().getImageGroupID().toString());
        ManagedBlockReturnValue returnValue;

        try {
            ManagedBlockCommandParameters params =
                    new ManagedBlockCommandParameters(JsonHelper.mapToJson(
                            managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams,
                            getCorrelationId());
            returnValue =
                    managedBlockExecutor.runCommand(ManagedBlockExecutor.ManagedBlockCommand.REMOVE_SNAPSHOT, params);
        } catch (Exception e) {
            log.error("Failed executing snapshot deletion", e);
            return;
        }

        if (!returnValue.getSucceed()) {
            return;
        }

        removeSnapshotFromDB();
        setSucceeded(true);
    }

    private void removeSnapshotFromDB() {
        TransactionSupport.executeInNewTransaction(() -> {
            DiskImage toBeRemovedImage = diskImageDao.getSnapshotById(getParameters().getImageId());

            // Replace parent id
            List<DiskImage> allSnapshotsForParent =
                    diskImageDao.getAllSnapshotsForParent(toBeRemovedImage.getImageId());

            // Make sure it's not a leaf
            if (!allSnapshotsForParent.isEmpty()) {
                DiskImage childImage = allSnapshotsForParent.get(0);
                childImage.setParentId(toBeRemovedImage.getParentId());
                imageDao.update(childImage.getImage());
            }

            imageDao.remove(toBeRemovedImage.getImageId());

            return null;
        });
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
