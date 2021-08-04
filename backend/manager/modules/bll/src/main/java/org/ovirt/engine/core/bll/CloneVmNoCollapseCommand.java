package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class CloneVmNoCollapseCommand<T extends CloneVmParameters> extends CloneVmCommand<T> {

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

    protected CloneVmNoCollapseCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    public CloneVmNoCollapseCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void init() {
        super.init();
        setCompensationContext(createDefaultCompensationContext());
    }

    @Override
    protected boolean validate() {
        if (!(getSourceVmFromDb().getStatus() == VMStatus.Suspended || getSourceVmFromDb().isDown())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        }
        return super.validate();
    }

    @Override
    protected void executeVmCommand() {
        getParameters().setStage(CloneVmParameters.CloneVmStage.CREATE_VM_SNAPSHOT);
        setSucceeded(true);
    }

    @Override
    protected void addVmImages() {
        getParameters()
                .getSrcToDstChainMap()
                .values()
                .stream()
                .map(Map::values)
                .flatMap(diskImages -> diskImages.stream())
                .forEach(imagesHandler::addDiskImageWithNoVmDevice);
        super.attachDisks();
    }

    private CommandContext createStepsContext() {
        Step addedStep = executionHandler.addSubStep(getExecutionContext(),
                getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                StepEnum.COPY_IMAGE,
                ExecutionMessageDirector.resolveStepMessage(StepEnum.COPY_IMAGE, Collections.emptyMap()));
        ExecutionContext ctx = new ExecutionContext();
        ctx.setStep(addedStep);
        ctx.setMonitored(true);
        return ExecutionHandler.createInternalJobContext(getContext(), null)
                .withExecutionContext(ctx);
    }

    @Override
    protected void copyDisks() {
        Collection<DiskImage> vmDisks = super.getSourceDisks();

        for (DiskImage diskImage : vmDisks) {
            MoveOrCopyImageGroupParameters copyParams = createCopyParams(diskImage);

            ActionReturnValue returnValue =
                    runInternalAction(ActionType.CopyImageGroup, copyParams, createStepsContext());

            if (!returnValue.getSucceeded()) {
                log.error("Failed to copy image group");
                throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
            }

            getParameters().getSrcDiskIdToTargetDiskIdMapping()
                    .put(diskImage.getId(), copyParams.getDestImageGroupId());
        }

        persistCommandIfNeeded();
        setSucceeded(true);
    }

    @Override
    protected void createDestSnapshots() {
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
        oldVm.setClusterArch(getVm().getClusterArch());

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
                getParameters().getDestStorageDomainId();
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
    protected void removeVmSnapshot() {
        log.info("Skipping Auto-Generated snapshot removal, since it was not generated for " +
                    "exporting VM '{}' without collapsing snapshots", getSourceVmId());
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

    @Override
    protected void removeVmImages() {
        TransactionSupport.executeInNewTransaction(() -> {
            getParameters()
                    .getSrcToDstChainMap()
                    .values()
                    .stream()
                    .flatMap(m -> m.values().stream())
                    .forEach(diskImage -> imagesHandler.removeDiskImage(diskImage,
                            getParameters().getNewVmGuid()));
            return null;
        });
    }
}
