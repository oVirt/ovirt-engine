package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ReduceImageCommandParameters;
import org.ovirt.engine.core.common.action.RefreshVolumeParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.ReduceImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class ReduceImageCommand<T extends ReduceImageCommandParameters> extends CommandBase<T> {

    private static final Logger log = LoggerFactory.getLogger(ReduceImageCommand.class);

    @Inject
    private ImagesHandler imagesHandler;

    public ReduceImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected void executeCommand() {
        if (getStorageDomain().getStorageType().isFileDomain()) {
            log.info("Reduce image isn't required for file based domains");
            setSucceeded(true);
            return;
        }

        if (!isReduceVolumeSupported()) {
            log.info("Reduce image isn't supported in {}", getStoragePool().getCompatibilityVersion());
            setSucceeded(true);
            return;
        }

        if (!isInternalMerge()) {
            log.info("Reduce image isn't supported for active image merge");
            setSucceeded(true);
            return;
        }

        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getImageGroupId()));

        boolean prepareImageSucceeded = false;
        boolean reduceImageSucceeded = false;

        if (!getParameters().isVmRunningOnSpm()) {
            // The VM isn't running on the SPM but the reduce command is performed on the SPM, hence
            // we have to prepare the image on the SPM
            log.debug("Preparing image {}/{} on the SPM", getParameters().getImageGroupId(), getParameters().getImageId());
            try {
                prepareImage();
                prepareImageSucceeded = true;
            } catch (EngineException e) {
                log.error("Failed to prepare image {}/{} on the SPM", getParameters().getImageGroupId(), getParameters().getImageId());
            }
        }

        if (!getParameters().isVmRunningOnSpm() && !prepareImageSucceeded) {
            // As we don't want to fail the live merge because of a failure to reduce the image, we still mark the
            // command as succeeded.
            setSucceeded(true);
            return;
        }

        try {
            VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.ReduceImage, creaeteReduceImageVDSCommandParameters());
            if (vdsReturnValue.getSucceeded()) {
                Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
                getTaskIdList().add(createTask(taskId,
                        vdsReturnValue.getCreationInfo(),
                        getParameters().getParentCommand(),
                        VdcObjectType.Storage,
                        getParameters().getStorageDomainId()));
                reduceImageSucceeded = true;
            }
        } catch (EngineException e) {
            log.error("Reducing image {}/{} failed", getParameters().getImageGroupId(), getParameters().getImageId());
        }

        if (prepareImageSucceeded && !reduceImageSucceeded) {
            try {
                teardownImage();
            } catch (EngineException e) {
                log.error("Failed to teardown image {}/{} on the SPM", getParameters().getImageGroupId(), getParameters().getImageId());
            }
        }

        setSucceeded(true);
    }

    @Override
    public ActionReturnValue endAction() {
        if (!getParameters().isVmRunningOnSpm()) {
            // Teardown the image on the SPM
            log.debug("Tearing down image {}/{} on the SPM", getParameters().getImageGroupId(), getParameters().getImageId());
            try {
                teardownImage();
            } catch (EngineException e) {
                log.error("Failed to teardown image {}/{} on the SPM", getParameters().getImageGroupId(), getParameters().getImageId());
            }

            // Refresh image on the host running the VM
            log.debug("Refreshing image {}/{} on its running host {}", getParameters().getImageGroupId(),
                    getParameters().getImageId(), getParameters().getRunningVdsId());
            try {
                runInternalAction(ActionType.RefreshVolume, createRefreshVolumeParameters());
            } catch (EngineException e) {
                log.error("Failed to refresh image {}/{} on its running host {}", getParameters().getImageGroupId(),
                        getParameters().getImageId(), getParameters().getRunningVdsId());
            }
        }

        // We mark the action as succeeded, even if it failed, in order not to fail the live merge operation.
        setSucceeded(true);
        setCommandStatus(CommandStatus.SUCCEEDED);
        return getReturnValue();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.reduceImage;
    }

    private boolean isReduceVolumeSupported() {
        return FeatureSupported.isReduceVolumeSupported(getStoragePool().getCompatibilityVersion());
    }

    private boolean isInternalMerge() {
        return !getParameters().getActiveDiskImage().getParentId().equals(getParameters().getImageId());
    }

    private ReduceImageVDSCommandParameters creaeteReduceImageVDSCommandParameters() {
        return new ReduceImageVDSCommandParameters(getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getImageId(),
                getParameters().isAllowActive());
    }

    private RefreshVolumeParameters createRefreshVolumeParameters() {
        RefreshVolumeParameters parameters = new RefreshVolumeParameters(getParameters().getRunningVdsId(),
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getImageId());
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    private void prepareImage() {
        imagesHandler.prepareImage(getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getImageId(),
                getParameters().getSpmId());
    }

    private void teardownImage() {
        imagesHandler.teardownImage(getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getImageId(),
                getParameters().getSpmId());
    }
}
