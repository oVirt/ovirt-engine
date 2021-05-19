package org.ovirt.engine.core.bll.storage.disk;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateAllTemplateDisksParameters;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateAllTemplateDisksCommand<T extends CreateAllTemplateDisksParameters> extends CommandBase<T> {

    @Inject
    protected VmHandler vmHandler;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    private final List<DiskImage> images = new ArrayList<>();
    private int targetDiskIdIndex;
    private List<CinderDisk> cinderDisks;
    private List<ManagedBlockStorageDisk> managedBlockDisks;
    private final Guid vmSnapshotId = Guid.newGuid();

    public CreateAllTemplateDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVmId(parameters.getVmId());
    }

    public CreateAllTemplateDisksCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void init() {
        super.init();

        if (getVm() != null) {
            images.addAll(getVmDisksFromDb());
        }
        getParameters().setUseCinderCommandCallback(!getCinderDisks().isEmpty());
    }

    @Override
    protected void executeCommand() {
        final Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping = new HashMap<>();

        if (getVm() != null && (!addVmTemplateCinderDisks(srcDeviceIdToTargetDeviceIdMapping)
                || !addVmTemplateManagedBlockDisks(srcDeviceIdToTargetDeviceIdMapping))) {
            // Error cloning Cinder disks for template
            return;
        }

        TransactionSupport.executeInNewTransaction(() -> {
            addVmTemplateImages(srcDeviceIdToTargetDeviceIdMapping);
            return null;
        });

        setActionReturnValue(srcDeviceIdToTargetDeviceIdMapping);
        setSucceeded(true);
    }

    private boolean addVmTemplateManagedBlockDisks(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        List<ManagedBlockStorageDisk> managedBlockDisks = getManagedBlockDisks();
        if (managedBlockDisks.isEmpty()) {
            return true;
        }
        Map<Guid, Guid> diskImageMap = new HashMap<>();
        for (ManagedBlockStorageDisk managedBlockDisk : managedBlockDisks) {
            ImagesContainterParametersBase params = buildImagesContainterParameters(managedBlockDisk);
            ActionReturnValue returnValue =
                    runInternalAction(ActionType.CloneSingleManagedBlockDisk,
                            params,
                            cloneContext().withoutExecutionContext().withoutLock());
            if (!returnValue.getSucceeded()) {
                log.error("Error cloning Managed block disk '{}'", managedBlockDisk.getDiskAlias());
                getReturnValue().setFault(returnValue.getFault());
                return false;
            }
            Guid imageId = returnValue.getActionReturnValue();
            diskImageMap.put(managedBlockDisk.getId(), imageId);
        }
        srcDeviceIdToTargetDeviceIdMapping.putAll(diskImageMap);
        return true;
    }

    private boolean addVmTemplateCinderDisks(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        List<CinderDisk> cinderDisks = getCinderDisks();
        if (cinderDisks.isEmpty()) {
            return true;
        }
        // Create Cinder disk templates
        Map<Guid, Guid> diskImageMap = new HashMap<>();
        for (CinderDisk cinderDisk : cinderDisks) {
            ImagesContainterParametersBase params = buildImagesContainterParameters(cinderDisk);
            ActionReturnValue returnValue =
                    runInternalAction(ActionType.CloneSingleCinderDisk,
                            params,
                            cloneContext().withoutExecutionContext().withoutLock());
            if (!returnValue.getSucceeded()) {
                log.error("Error cloning Cinder disk '{}'", cinderDisk.getDiskAlias());
                getReturnValue().setFault(returnValue.getFault());
                return false;
            }
            Guid imageId = returnValue.getActionReturnValue();
            diskImageMap.put(cinderDisk.getId(), imageId);
        }
        srcDeviceIdToTargetDeviceIdMapping.putAll(diskImageMap);
        return true;
    }

    private ImagesContainterParametersBase buildImagesContainterParameters(DiskImage srcDisk) {
        ImagesContainterParametersBase createParams = new ImagesContainterParametersBase(srcDisk.getImageId());
        DiskImage templateDisk = getParameters().getDiskInfoDestinationMap().get(srcDisk.getId());
        createParams.setDiskAlias(templateDisk.getDiskAlias());
        createParams.setStorageDomainId(templateDisk.getStorageIds().get(0));
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        createParams.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        createParams.setVmSnapshotId(vmSnapshotId);
        return createParams;
    }

    private void addVmTemplateImages(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        DisksFilter.filterImageDisks(images, ONLY_NOT_SHAREABLE, ONLY_ACTIVE)
                .forEach(diskImage -> addVmTemplateImage(diskImage, srcDeviceIdToTargetDeviceIdMapping));
    }

    private void addVmTemplateImage(DiskImage diskImage, Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        // The return value of this action is the 'copyImage' task GUID:
        Guid targetDiskId = getParameters().getTargetDiskIds()[targetDiskIdIndex++];
        ActionReturnValue returnValue = backend.runInternalAction(
                ActionType.CreateImageTemplate,
                buildCreateImageTemplateCommandParameters(diskImage, targetDiskId),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }

        getReturnValue().getVdsmTaskIdList().addAll(returnValue.getInternalVdsmTaskIdList());
        DiskImage newImage = returnValue.getActionReturnValue();
        srcDeviceIdToTargetDeviceIdMapping.put(diskImage.getId(), newImage.getId());
    }

    private CreateImageTemplateParameters buildCreateImageTemplateCommandParameters(DiskImage diskImage, Guid vmSnapshotId) {
        DiskImage imageFromParams = getParameters().getDiskInfoDestinationMap().get(diskImage.getId());
        CreateImageTemplateParameters createParams = new CreateImageTemplateParameters(diskImage.getImageId(),
                getParameters().getVmTemplateId(), getParameters().getVmTemplateName(), getVmId());
        createParams.setStorageDomainId(diskImage.getStorageIds().get(0));
        createParams.setVmSnapshotId(vmSnapshotId);
        createParams.setEntityInfo(getParameters().getEntityInfo());
        createParams.setDestinationStorageDomainId(imageFromParams.getStorageIds().get(0));
        createParams.setDiskAlias(imageFromParams.getDiskAlias());
        createParams.setDescription(imageFromParams.getDiskDescription());
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        createParams.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        createParams.setQuotaId(imageFromParams.getQuotaId());
        createParams.setDiskProfileId(imageFromParams.getDiskProfileId());
        createParams.setVolumeFormat(imageFromParams.getVolumeFormat());
        createParams.setVolumeType(imageFromParams.getVolumeType());
        createParams.setCopyVolumeType(getParameters().getCopyVolumeType());
        return createParams;
    }

    @Override
    protected void endSuccessfully() {
        for (ActionParametersBase params : getParameters().getImagesParameters()) {
            backend.endAction(params.getCommandType(), params, cloneContextAndDetachFromParent());
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        for (ActionParametersBase params : getParameters().getImagesParameters()) {
            params.setTaskGroupSuccess(false);
            backend.endAction(params.getCommandType(), params, cloneContextAndDetachFromParent());
        }
        setSucceeded(false);
    }

    protected List<DiskImage> getVmDisksFromDb() {
        vmHandler.updateDisksFromDb(getVm());
        vmHandler.filterImageDisksForVM(getVm());
        return getVm().getDiskList();
    }

    private List<CinderDisk> getCinderDisks() {
        if (cinderDisks == null) {
            cinderDisks = DisksFilter.filterCinderDisks(images);
        }
        return cinderDisks;
    }

    private List<ManagedBlockStorageDisk> getManagedBlockDisks() {
        if (managedBlockDisks == null) {
            managedBlockDisks = DisksFilter.filterManagedBlockStorageDisks(images);
        }
        return managedBlockDisks;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

}
