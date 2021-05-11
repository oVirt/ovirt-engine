package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.CommandsWeightsUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CopyDataCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupVolumesDataCommandParameters;
import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.utils.CollectionUtils;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CopyImageGroupVolumesDataCommand<T extends CopyImageGroupVolumesDataCommandParameters>
        extends CommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private CommandsWeightsUtils commandsWeightsUtils;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private ImagesHandler imagesHandler;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public CopyImageGroupVolumesDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    private double calculateImageWeight(double totalSize, DiskImage image) {
        return totalSize == 0 ? 1d / getParameters().getImageIds().size() : image.getActualSize() / totalSize;
    }

    @Override
    protected void executeCommand() {
        // If we are copying a template we will get the same disk multiple times
        List<DiskImage> images = diskImageDao.getAllSnapshotsForImageGroup(getParameters().getImageGroupID())
                .stream()
                .filter(CollectionUtils.distinctByKey(DiskImage::getImageId))
                .collect(Collectors.toList());

        ImagesHandler.sortImageList(images);

        // When perform LSM we do not want to copy the LEAF as it is the auto-generated
        // snapshot and is synchronized separately
        if (getParameters().getParentCommand() == ActionType.LiveMigrateDisk) {
            images.remove(images.size() - 1);
        }

        getParameters().setImageIds(ImagesHandler.getDiskImageIds(images));

        prepareWeights(images);

        persistCommand(getParameters().getParentCommand(), getCallback() != null);
        setSucceeded(true);
    }

    private void prepareWeights(List<DiskImage> images) {
        if (getParameters().getJobWeight() != null) {
            double totalSize = images.stream().mapToDouble(DiskImage::getActualSize).sum();
            Map<String, Double> weightDivision = images.stream().collect(
                    Collectors.toMap(x -> x.getImageId().toString(), x -> calculateImageWeight(totalSize, x)));

            getParameters().setOperationsJobWeight(commandsWeightsUtils.adjust(weightDivision,
                    getParameters().getJobWeight()));
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildren) {
        if (completedChildren == getParameters().getImageIds().size()) {
            return false;
        }

        Guid imageId = getParameters().getImageIds().get(completedChildren);
        log.info("Starting child command {} of {}, image '{}'",
                completedChildren + 1, getParameters().getImageIds().size(), imageId);

        copyVolumeData(imageId, completedChildren);
        return true;
    }

    private void copyVolumeData(Guid imageId, int imageIndex) {
        LocationInfo destLocationInfo;

        if (getParameters().getDestImages().isEmpty()) {
            destLocationInfo = buildImageLocationInfo(getParameters().getDestDomain(),
                            getParameters().getImageGroupID(),
                            imageId,
                            getParameters().isLive());
        } else {
            destLocationInfo = buildImageLocationInfo(getParameters().getDestDomain(),
                    getParameters().getDestImageGroupId(),
                    getParameters().getDestImages().get(imageIndex).getImageId(),
                    getParameters().isLive());
        }

        CopyDataCommandParameters parameters = new CopyDataCommandParameters(getParameters().getStoragePoolId(),
                buildImageLocationInfo(getParameters().getSrcDomain(),
                        getParameters().getImageGroupID(),
                        imageId,
                        getParameters().isLive()),
                destLocationInfo,
                false);
        parameters.setVdsId(getParameters().getVdsId());
        parameters.setVdsRunningOn(getParameters().getVdsRunningOn());

        if (imagesHandler.shouldUseDiskBitmaps(getStoragePool().getCompatibilityVersion(), getDiskImage(imageId))) {
            parameters.setCopyBitmaps(true);
        }

        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setJobWeight(getParameters().getOperationsJobWeight().get(imageId.toString()));
        runInternalActionWithTasksContext(ActionType.CopyData, parameters);
    }

    private DiskImage getDiskImage(Guid imageId) {
        DiskImage diskImage = diskImageDao.get(imageId);
        return diskImage != null ? diskImage : diskImageDao.getSnapshotById(imageId);
    }

    @Override
    public void handleFailure() {
    }

    private LocationInfo buildImageLocationInfo(Guid domId, Guid imageGroupId, Guid imageId, boolean prepared) {
        return new VdsmImageLocationInfo(domId, imageGroupId, imageId, null, prepared);
    }
}

