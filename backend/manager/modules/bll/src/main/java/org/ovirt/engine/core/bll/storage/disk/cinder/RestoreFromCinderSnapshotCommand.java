package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.RemoveCinderDiskVolumeParameters;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RestoreFromCinderSnapshotCommand<T extends RemoveCinderDiskParameters> extends RemoveCinderVolumeParentCommand<T> {

    public RestoreFromCinderSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        setStorageDomainId(getParameters().getStorageDomainId());
        removeCinderVolume();
        setSucceeded(true);
    }

    private void removeCinderVolume() {
        CinderDisk cinderSnapshotToRemove = (CinderDisk) getDiskImageDao().getSnapshotById(getParameters().getImageId());

        // Update the volume with active=false, so removeDiskFromDb will not set active volume on the first volume.
        cinderSnapshotToRemove.setActive(false);
        getImageDao().update(cinderSnapshotToRemove.getImage());
        removeDescendentSnapshots(cinderSnapshotToRemove);
        removeCinderVolume(cinderSnapshotToRemove.getStorageIds().get(0), 0);
    }

    protected boolean removeCinderVolume(Guid storageId, int removedVolumeIndex) {
        RemoveCinderDiskVolumeParameters param = getParameters().getChildCommandsParameters().get(removedVolumeIndex);
        try {
            VdcReturnValueBase vdcReturnValueBase =
                    getFutureRemoveCinderDiskVolume(storageId, removedVolumeIndex).get();
            if (vdcReturnValueBase == null || !vdcReturnValueBase.getSucceeded()) {
                handleExecutionFailure(param.getRemovedVolume(), vdcReturnValueBase);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Remove the snapshot's descendants. For example if we plan to commit snapshot A we should delete all the snapshots
     * which were created after it (like snapshot B).
     */
    private void removeDescendentSnapshots(CinderDisk cinderDiskSnapshot) {
        initCinderDiskVolumesParametersList(cinderDiskSnapshot);
    }

    @Override
    public void removeDiskFromDbCallBack(final CinderDisk cinderVolume) {
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                () -> {
                    removeDiskFromDb(cinderVolume, null);
                    return null;
                });
    }

    @Override
    public CommandCallback getCallback() {
        return new SerialChildCommandsExecutionCallback();
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage,
                getStorageDomainId()));
    }
}
