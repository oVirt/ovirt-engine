package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class CloneVmNoCollapseCommand<T extends CloneVmParameters> extends CloneVmCommand<T> implements SerialChildExecutingCommand {

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    private SnapshotsManager snapshotsManager;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    @Inject
    private ImageDao imageDao;

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    protected CloneVmNoCollapseCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    public CloneVmNoCollapseCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean addVmImages() {
        getParameters()
                .getSrcToDstChainMap()
                .values()
                .stream()
                .map(Map::values)
                .flatMap(diskImages -> diskImages.stream())
                .forEach(imagesHandler::addDiskImageWithNoVmDevice);
        super.attachDisks();

        return true;
    }

    @Override
    protected void executeVmCommand() {
        Collection<DiskImage> vmDisks = super.getAdjustedDiskImagesFromConfiguration();

        for (DiskImage diskImage : vmDisks) {
            MoveOrCopyImageGroupParameters copyParams = createCopyParams(diskImage);

            ActionReturnValue returnValue = runInternalAction(ActionType.CopyImageGroup, copyParams);
            if (!returnValue.getSucceeded()) {
                setSucceeded(false);
                return;
            }

            getParameters().getSrcDiskIdToTargetDiskIdMapping()
                    .put(diskImage.getId(), copyParams.getDestImageGroupId());
        }

        persistCommandIfNeeded();
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getStage() == CloneVmParameters.CloneVmStage.COPY_DISKS) {
            super.executeVmCommand();
            createDestSnapshots();
            getParameters().setStage(CloneVmParameters.CloneVmStage.CREATE_SNAPSHOTS);
            persistCommandIfNeeded();
            return true;
        }

        return false;
    }

    private void createDestSnapshots() {
        TransactionSupport.executeInNewTransaction(() -> {
            Map<Guid, DiskImage> oldToNewImageMap = getParameters()
                    .getSrcToDstChainMap()
                    .values()
                    .stream()
                    .flatMap(m -> m.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            snapshotDao.getAllWithConfiguration(getParameters().getVmId())
                    .stream()
                    .forEach(snapshot -> addSnapshotToDB(oldToNewImageMap, snapshot));

            return null;
        });
    }

    private void addSnapshotToDB(Map<Guid, DiskImage> oldToNewImageMap, Snapshot snapshot) {
        VM oldVm = snapshotVmConfigurationHelper.getVmFromConfiguration(snapshot);

        List<DiskImage> newDiskImages = oldVm.getDiskList()
                .stream()
                .map(d -> oldToNewImageMap.get(d.getImageId()))
                .collect(Collectors.toList());

        Guid newSnapshotId = Guid.newGuid();
        newDiskImages.forEach(diskImage -> {
            diskImage.setVmSnapshotId(newSnapshotId);
            imageDao.update(diskImage.getImage());
        });

        snapshotsManager.addSnapshot(newSnapshotId,
                snapshot.getDescription(),
                snapshot.getStatus(),
                snapshot.getType(),
                initClonedVm(oldVm),
                true,
                null,
                null,
                snapshot.getCreationDate(),
                newDiskImages,
                oldVm.getManagedVmDeviceMap(),
                getCompensationContext());
    }

    private VM initClonedVm(VM oldVm) {
        oldVm.setId(getVmId());
        oldVm.setDiskMap(getVm().getDiskMap());
        oldVm.setName(getVm().getName());
        oldVm.setOriginalTemplateGuid(getVm().getOriginalTemplateGuid());
        oldVm.setOriginalTemplateName(getVm().getOriginalTemplateName());
        oldVm.setVmtGuid(getVm().getVmtGuid());
        oldVm.getInterfaces().forEach(iface -> {
            iface.setId(Guid.newGuid());
            iface.setVmId(getVmId());
        });

        return oldVm;
    }

    private MoveOrCopyImageGroupParameters createCopyParams(DiskImage diskImage) {
        Guid srcStorageDomainID = diskImage.getStorageIds().get(0);
        Guid destImageGroupID = Guid.newGuid();
        Guid destStorageDomainID = getParameters().getDestStorageDomainId() == null ? srcStorageDomainID :
                diskImage.getStorageIds().get(0);
        List<DiskImage> newChain = prepareImageChainMap(diskImage, destImageGroupID, destStorageDomainID);

        MoveOrCopyImageGroupParameters p =
                new MoveOrCopyImageGroupParameters(diskImage.getImageId(),
                        srcStorageDomainID,
                        destStorageDomainID,
                        ImageOperation.Copy);
        p.setAddImageDomainMapping(false);
        p.setVolumeFormat(diskImage.getVolumeFormat());
        p.setVolumeType(diskImage.getVolumeType());
        p.setUseCopyCollapse(false);
        p.setWipeAfterDelete(diskImage.isWipeAfterDelete());
        p.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
        p.setParentParameters(getParameters());
        p.setParentCommand(getActionType());
        p.setJobWeight(Job.MAX_WEIGHT);
        p.setDestImages(newChain);
        p.setImageGroupID(diskImage.getId());
        p.setDestImageGroupId(destImageGroupID);

        return p;
    }

    private List<DiskImage> prepareImageChainMap(DiskImage diskImage, Guid destImageGroupID, Guid destStorageDomainID) {
        Map<DiskImage, DiskImage> oldToNewDiskImageMap =
                imagesHandler.mapChainToNewIDs(diskImage.getId(),
                        destImageGroupID,
                        destStorageDomainID,
                        getCurrentUser());
        getParameters().getSrcToDstChainMap()
                .put(destImageGroupID,
                        oldToNewDiskImageMap
                                .keySet()
                                .stream()
                                .collect(Collectors.toMap(d -> d.getImageId(),
                                        d -> oldToNewDiskImageMap.get(d))));
        List<DiskImage> newChain = new ArrayList(oldToNewDiskImageMap.values());
        ImagesHandler.sortImageList(newChain);

        return newChain;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void unlockEntities() {
        TransactionSupport.executeInNewTransaction(() -> {
            getParameters().getSrcToDstChainMap()
                    .values()
                    .stream()
                    .flatMap(e -> e.values().stream())
                    .forEach(d -> {
                        d.setImageStatus(ImageStatus.OK);
                        imageDao.update(d.getImage());
                    });

            return null;
        });
    }

    @Override
    protected void endSuccessfully() {
        unlockEntities();
        super.endSuccessfully();
    }

    @Override
    protected void endWithFailure() {
        getReturnValue().setEndActionTryAgain(false);
        super.endWithFailure();
    }
}
