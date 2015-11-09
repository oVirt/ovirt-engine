package org.ovirt.engine.core.bll.memory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.utils.GuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MemoryImageRemover {
    private static final Logger log = LoggerFactory.getLogger(MemoryImageRemover.class);
    private static final int NUM_OF_UUIDS_IN_MEMORY_STATE = 6;
    public static final String DELETE_PRIMARY_IMAGE_TASK_KEY = "DELETE_PRIMARY_IMAGE_TASK_KEY";
    public static final String DELETE_SECONDARY_IMAGES_TASK_KEY = "DELETE_SECONDARY_IMAGES_TASK_KEY";

    protected final TaskHandlerCommand<?> enclosingCommand;
    private boolean startPollingTasks;

    public MemoryImageRemover(TaskHandlerCommand<?> enclosingCommand) {
        this.enclosingCommand = enclosingCommand;
    }

    public MemoryImageRemover(TaskHandlerCommand<?> enclosingCommand, boolean startPollingTasks) {
        this(enclosingCommand);
        this.startPollingTasks = startPollingTasks;
    }

    protected abstract DeleteImageGroupVDSCommandParameters buildDeleteMemoryImageParams(List<Guid> guids);

    protected abstract DeleteImageGroupVDSCommandParameters buildDeleteMemoryConfParams(List<Guid> guids);

    protected Guid createTask(Guid taskId, VDSReturnValue vdsRetValue) {
        return enclosingCommand.createTask(
                taskId,
                vdsRetValue.getCreationInfo(),
                enclosingCommand.getActionType());
    }

    /**
     * Default implementation checks whether the memory state representation is not empty
     */
    protected boolean isMemoryStateRemovable(String memoryVolume) {
        return !memoryVolume.isEmpty();
    }

    protected boolean removeMemoryVolume(String memoryVolumes) {
        if (isMemoryStateRemovable(memoryVolumes)) {
            return removeMemoryVolumes(memoryVolumes);
        }

        return true;
    }

    /**
     * Try to remove all the given memory volumes
     *
     * @param memoryVolumes - memory volumes to remove
     * @return true if all the memory volumes were removed successfully, false otherwise
     */
    protected boolean removeMemoryVolumes(Set<String> memoryVolumes) {
        boolean allVolumesRemovedSucessfully = true;
        for (String memoryVols : memoryVolumes) {
            allVolumesRemovedSucessfully &= removeMemoryVolume(memoryVols);
        }

        return allVolumesRemovedSucessfully;
    }

    private boolean removeMemoryVolumes(String memVols) {
        List<Guid> guids = GuidUtils.getGuidListFromString(memVols);

        if (guids.size() != NUM_OF_UUIDS_IN_MEMORY_STATE) {
            log.warn("Cannot remove memory volumes, invalid format '{}'", memVols);
            return true;
        }

        Guid memoryImageRemovalTaskId = removeMemoryImage(guids);
        if (memoryImageRemovalTaskId == null) {
            return false;
        }

        Guid confImageRemovalTaskId = removeConfImage(guids);

        if (startPollingTasks) {
            if (!Guid.Empty.equals(memoryImageRemovalTaskId)) {
                CommandCoordinatorUtil.startPollingTask(memoryImageRemovalTaskId);
            }

            if (confImageRemovalTaskId != null && !Guid.Empty.equals(confImageRemovalTaskId)) {
                CommandCoordinatorUtil.startPollingTask(confImageRemovalTaskId);
            }
        }

        return confImageRemovalTaskId != null;
    }

    protected Guid removeMemoryImage(List<Guid> guids) {
        return removeImage(
                DELETE_PRIMARY_IMAGE_TASK_KEY,
                buildDeleteMemoryImageParams(guids));
    }

    protected Guid removeConfImage(List<Guid> guids) {
        return removeImage(
                DELETE_SECONDARY_IMAGES_TASK_KEY,
                buildDeleteMemoryConfParams(guids));
    }

    protected Guid removeImage(String taskKey, DeleteImageGroupVDSCommandParameters parameters) {
        Guid taskId = enclosingCommand.persistAsyncTaskPlaceHolder(taskKey);

        VDSReturnValue vdsRetValue = removeImage(parameters);
        // if command succeeded, create a task
        if (vdsRetValue.getSucceeded()) {
            Guid guid = createTask(taskId, vdsRetValue);
            enclosingCommand.getTaskIdList().add(guid);
            return guid;
        }
        else {
            boolean imageDoesNotExist = vdsRetValue.getVdsError().getCode() == EngineError.ImageDoesNotExistInDomainError;
            if (!imageDoesNotExist) {
                log.error("Could not remove memory image '{}'", parameters);
            }
            enclosingCommand.deleteAsyncTaskPlaceHolder(taskKey);
            // otherwise, if the command failed because the image does not exist,
            // no need to create and monitor task, so we return empty guid to mark this state
            return imageDoesNotExist ? Guid.Empty : null;
        }
    }

    protected VDSReturnValue removeImage(DeleteImageGroupVDSCommandParameters parameters) {
        try {
            return Backend.getInstance().getResourceManager().runVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    parameters);
        }
        catch(EngineException e) {
            if (e.getErrorCode() == EngineError.ImageDoesNotExistInDomainError) {
                return createImageDoesNotExistInDomainReturnValue();
            }
            throw e;
        }
    }

    protected VDSReturnValue createImageDoesNotExistInDomainReturnValue() {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(false);
        vdsRetValue.setVdsError(new VDSError(EngineError.ImageDoesNotExistInDomainError, ""));
        return vdsRetValue;
    }

    /**
     * @return true IFF one of the given disks is marked with wipe_after_delete
     */
    protected static boolean isDiskWithWipeAfterDeleteExist(Collection<Disk> disks) {
        for (Disk disk : disks) {
            if (disk.isWipeAfterDelete()) {
                return true;
            }
        }
        return false;
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }
}
