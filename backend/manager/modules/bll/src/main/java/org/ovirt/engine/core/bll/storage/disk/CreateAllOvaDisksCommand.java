package org.ovirt.engine.core.bll.storage.disk;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateAllOvaDisksParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateAllOvaDisksCommand<T extends CreateAllOvaDisksParameters> extends CommandBase<T> {

    @Inject
    protected ImagesHandler imagesHandler;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public CreateAllOvaDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public CreateAllOvaDisksCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        // Add disks to the database
        getParameters().getDiskInfoDestinationMap().values().forEach(image -> {
            saveImage(image);
            saveBaseDisk(image);
            saveDiskImageDynamic(image);
        });
        // Copy disks
        getParameters().getDiskInfoDestinationMap().forEach(this::copy);
        setSucceeded(true);
    }

    private void copy(DiskImage source, DiskImage destination) {
        ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(
                ActionType.CopyImageGroup,
                buildMoveOrCopyImageGroupParametersForDisk(source, destination));
        if (!vdcRetValue.getSucceeded()) {
            throw new EngineException(vdcRetValue.getFault().getError(), "Failed to copy disk!");
        }
        // TODO: Currently REST-API doesn't support coco for async commands, remove when bug 1199011 fixed
        getTaskIdList().addAll(vdcRetValue.getVdsmTaskIdList());
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForDisk(DiskImage source, DiskImage destination) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(
                getParameters().getEntityId(),
                source.getId(),
                source.getImageId(),
                destination.getId(),
                destination.getImageId(),
                source.getStorageIds().get(0), // TODO: change
                ImageOperation.Copy);
        params.setParentCommand(getActionType());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(true);
        params.setSourceDomainId(source.getStorageIds().get(0));
        params.setStoragePoolId(source.getStoragePoolId());
        params.setImportEntity(true);
        params.setParentParameters(getParameters());
        params.setUseCopyCollapse(true);
        params.setVolumeType(VolumeType.Sparse);
        params.setVolumeFormat(VolumeFormat.COW);
        return params;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    /** Save the entire image, including it's storage mapping */
    protected void saveImage(DiskImage disk) {
        imagesHandler.saveImage(disk);
    }

    /** Saves the base disk object */
    protected void saveBaseDisk(DiskImage disk) {
        baseDiskDao.save(disk);
    }

    /**
     * Generates and saves a {@link DiskImageDynamic} for the given <code>disk</code>
     *
     * @param disk
     *            The imported disk
     **/
    protected void saveDiskImageDynamic(DiskImage disk) {
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(disk.getImageId());
        diskDynamic.setActualSize(disk.getActualSizeInBytes());
        diskImageDynamicDao.save(diskDynamic);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void endSuccessfully() {
        endCopyImageGroupCommands();
        super.endSuccessfully();
    }

    protected void endWithFailure() {
        endCopyImageGroupCommands();
        super.endWithFailure();
    }

    private void endCopyImageGroupCommands() {
        getParameters().getImagesParameters().forEach(parameters -> backend.endAction(
                ActionType.CopyImageGroup,
                parameters,
                getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock()));
    }
}
