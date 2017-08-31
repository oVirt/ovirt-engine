package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collection;
import java.util.Collections;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RestoreFromCinderSnapshotCommand<T extends RemoveCinderDiskParameters> extends RemoveCinderVolumeParentCommand<T> {

    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

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
        CinderDisk cinderSnapshotToRemove = (CinderDisk) diskImageDao.getSnapshotById(getParameters().getImageId());

        // Update the volume with active=false, so removeDiskFromDb will not set active volume on the first volume.
        cinderSnapshotToRemove.setActive(false);
        imageDao.update(cinderSnapshotToRemove.getImage());
        removeDescendentSnapshots(cinderSnapshotToRemove);
        removeCinderVolume(0);
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
        return callbackProvider.get();
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage,
                getStorageDomainId()));
    }
}
