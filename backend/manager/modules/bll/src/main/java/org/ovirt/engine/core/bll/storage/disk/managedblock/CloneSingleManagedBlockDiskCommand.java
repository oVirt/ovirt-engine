package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor.CinderlibCommand;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class CloneSingleManagedBlockDiskCommand<T extends ImagesContainterParametersBase> extends CommandBase<T> {
    @Inject
    private CinderStorageDao cinderStorageDao;
    @Inject
    private CinderlibExecutor cinderlibExecutor;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private ImagesHandler imagesHandler;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public CloneSingleManagedBlockDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getParameters().getImageId().toString());
        Guid clonedVolumeId = Guid.newGuid();
        extraParams.add(clonedVolumeId.toString());
        CinderlibReturnValue returnValue;

        lockImage();
        try {
            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams,
                            getCorrelationId());
            returnValue = cinderlibExecutor.runCommand(CinderlibCommand.CLONE_VOLUME, params);
        } catch (Exception e) {
            log.error("Failed executing clone volume verb", e);
            return;
        }

        if (!returnValue.getSucceed()) {
            return;
        }
        saveClonedDiskToDb(clonedVolumeId);

        unlockImage();
        getReturnValue().setActionReturnValue(clonedVolumeId);
        setSucceeded(true);
    }

    protected void saveClonedDiskToDb(Guid clonedVolId) {
        ManagedBlockStorageDisk clonedManagedBlockDisk = createClonedDisk(clonedVolId);
        TransactionSupport.executeInNewTransaction(() -> {
            baseDiskDao.save(clonedManagedBlockDisk);
            imageDao.save(clonedManagedBlockDisk.getImage());
            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(clonedManagedBlockDisk.getImageId());
            diskImageDynamicDao.save(diskDynamic);
            ImageStorageDomainMap image_storage_domain_map =
                    new ImageStorageDomainMap(clonedManagedBlockDisk.getImageId(),
                            clonedManagedBlockDisk.getStorageIds().get(0),
                            clonedManagedBlockDisk.getQuotaId(),
                            clonedManagedBlockDisk.getDiskProfileId());
            imageStorageDomainMapDao.save(image_storage_domain_map);

            getCompensationContext().snapshotNewEntity(image_storage_domain_map);
            getCompensationContext().snapshotNewEntity(diskDynamic);
            getCompensationContext().snapshotNewEntity(clonedManagedBlockDisk.getImage());
            getCompensationContext().snapshotNewEntity(clonedManagedBlockDisk);
            getCompensationContext().stateChanged();
            return null;
        });
    }

    private ManagedBlockStorageDisk createClonedDisk(Guid clonedVolId) {
        ManagedBlockStorageDisk managedBlockStorageDisk =
                (ManagedBlockStorageDisk) diskImageDao.getSnapshotById(getParameters().getImageId());
        managedBlockStorageDisk.setDiskAlias(getParameters().getDiskAlias());
        managedBlockStorageDisk.setId(clonedVolId);
        managedBlockStorageDisk.setImageId(clonedVolId);
        managedBlockStorageDisk.setImageStatus(ImageStatus.OK);
        managedBlockStorageDisk.setVolumeClassification(VolumeClassification.Volume);
        managedBlockStorageDisk.setVmSnapshotId(getParameters().getVmSnapshotId());
        return managedBlockStorageDisk;
    }

    private void lockImage() {
        imagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.LOCKED);
    }

    private void unlockImage() {
        imagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.OK);
    }

    @Override
    protected void endWithFailure() {
        unlockImage();
        setSucceeded(true);
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
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getParameters().getStorageDomainId()));
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }


}
