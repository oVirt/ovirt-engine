package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.RemoveImageCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class RemoveCinderDiskCommand<T extends RemoveCinderDiskParameters> extends RemoveImageCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(RemoveCinderDiskCommand.class);
    private CinderDisk cinderDisk;
    private Guid storageDomainId;

    public RemoveCinderDiskCommand(T parameters) {
        super(parameters, null);
    }

    public RemoveCinderDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void executeCommand() {
        CinderDisk disk = getDisk();
        lockDiskIfNecessary();
        CinderDisk lastCinderVolume = (CinderDisk) ImagesHandler.getSnapshotLeaf(disk.getId());
        if (getParameters().isLockVm()) {
            VM vm = getVmForNonShareableDiskImage(disk);

            // if the disk is not part of a vm (floating), there are no snapshots to update
            // so no lock is required.
            if (getParameters().isRemoveFromSnapshots() && vm != null) {
                lockVmSnapshotsWithWait(vm);
            }
        }
        getCinderBroker().deleteVolumeUnknownType(lastCinderVolume);
        getParameters().setRemovedVolume(lastCinderVolume);
        persistCommand(getParameters().getParentCommand(), true);
        getReturnValue().setActionReturnValue(disk.getId());
        setSucceeded(true);
    }

    private void lockDiskIfNecessary() {
        if (getDisk().getImageStatus() != ImageStatus.LOCKED) {
            ImagesHandler.updateImageStatus(getDisk().getId(), ImageStatus.LOCKED);
        }
    }

    protected CinderDisk getDisk() {
        if (cinderDisk == null) {
            cinderDisk = (CinderDisk) getDiskDao().get(getParameters().getDiskId());
        }
        return cinderDisk;
    }

    protected void removeDiskFromDb(final CinderDisk lastCinderVolume) {
        final Snapshot updated = getSnapshot(lastCinderVolume);
        TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        // If the image being removed has the same id as the disk id, we should remove the disk.
                        if (lastCinderVolume.getImageId().equals(getDisk().getImageId())) {
                            getDbFacade().getVmDeviceDao().remove(new VmDeviceId(lastCinderVolume.getId(), null));
                            getBaseDiskDao().remove(lastCinderVolume.getId());
                        }
                        getImageStorageDomainMapDao().remove(lastCinderVolume.getImageId());
                        getImageDao().remove(lastCinderVolume.getImageId());
                        getDiskImageDynamicDAO().remove(lastCinderVolume.getImageId());
                        if (updated != null) {
                            getSnapshotDao().update(updated);
                        }
                        return null;
                    }
                });
    }

    private Snapshot getSnapshot(CinderDisk lastCinderVolume) {
        Guid vmSnapshotId = lastCinderVolume.getVmSnapshotId();
        Snapshot updated = null;
        if (vmSnapshotId != null && !Guid.Empty.equals(vmSnapshotId)) {
            Snapshot snapshot = getSnapshotDao().get(vmSnapshotId);
            if (snapshot != null) {
                updated = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snapshot,
                        lastCinderVolume.getImageId());
            }
        }
        return updated;
    }

    protected void endSuccessfully() {
        endRemoveCinderDisk(getParameters().getRemovedVolume());
    }

    @Override
    protected void endWithFailure() {
        CinderDisk cinderVolume = getParameters().getRemovedVolume();
        log.error("Could not volume id {} from Cinder which is related to disk {}",
                cinderVolume.getDiskAlias(),
                cinderVolume.getImageId());
        if (getParameters().isFaultTolerant()) {
            endRemoveCinderDisk(cinderVolume);
        } else {
            auditLogFailureWithImageDeletion(cinderVolume.getImageId());
            ImagesHandler.updateImageStatus(getDisk().getId(), ImageStatus.ILLEGAL);
        }
    }

    private void endRemoveCinderDisk(CinderDisk leafCinderVolume) {
        removeDiskFromDb(leafCinderVolume);
        if (!leafCinderVolume.getImageId().equals(getDisk().getImageId())) {
            RemoveCinderDiskParameters removeParams = new RemoveCinderDiskParameters(getParameters().getDiskId());
            removeParams.setFaultTolerant(getParameters().isFaultTolerant());
            removeParams.setRemovedVolume(getParameters().getRemovedVolume());
            removeParams.setShouldBeLogged(getParameters().getShouldBeLogged());
            removeParams.setLockVm(false);
            Future<VdcReturnValueBase> future =
                    CommandCoordinatorUtil.executeAsyncCommand(VdcActionType.RemoveCinderDisk,
                            removeParams,
                            cloneContext(),
                            new SubjectEntity[0]);
            try {
                setReturnValue(future.get());
                setSucceeded(getReturnValue().getSucceeded());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error removing Cinder disk '{}': {}",
                        getDiskImage().getDiskAlias(),
                        e.getMessage());
                log.debug("Exception", e);
                ImagesHandler.updateImageStatus(getDisk().getId(), ImageStatus.ILLEGAL);
                auditLogFailureWithImageDeletion(leafCinderVolume.getParentId());
            }
        } else {
            if (getParameters().getShouldBeLogged()) {
                new AuditLogDirector().log(this, AuditLogType.USER_FINISHED_REMOVE_DISK);
            }
        }
    }

    private void auditLogFailureWithImageDeletion(Guid failedVolumeId) {
        addCustomValue("imageId", failedVolumeId.toString());
        new AuditLogDirector().log(this, AuditLogType.USER_FINISHED_FAILED_REMOVE_CINDER_DISK);
    }

    protected ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    @Override
    public CommandCallback getCallback() {
        return new RemoveCinderDiskCommandCallback();
    }

    @Override
    public boolean canDoAction() {
        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.emptyMap();
    }

    @Override
    public Guid getStorageDomainId() {
        if (storageDomainId == null) {
            storageDomainId = ((CinderDisk) getDiskDao().get(getDisk().getId())).getStorageIds().get(0);
            storageDomainId = getDisk().getStorageIds().get(0);
        }
        return storageDomainId;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskAlias());
        }
        return jobProperties;
    }

    protected ImageStorageDomainMapDao getImageStorageDomainMapDao() {
        return getDbFacade().getImageStorageDomainMapDao();
    }

    public String getDiskAlias() {
        if (getDisk() != null) {
            return getDisk().getDiskAlias();
        }
        return StringUtils.EMPTY;
    }
}
