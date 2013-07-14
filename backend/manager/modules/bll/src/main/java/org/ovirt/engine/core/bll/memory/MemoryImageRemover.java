package org.ovirt.engine.core.bll.memory;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.AsyncTaskManager;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class MemoryImageRemover {
    private static final Log log = LogFactory.getLog(MemoryImageRemover.class);
    private static final int NUM_OF_UUIDS_IN_MEMORY_STATE = 6;

    private TaskHandlerCommand<?> enclosingCommand;
    private boolean startPollingTasks;

    public MemoryImageRemover(TaskHandlerCommand<?> enclosingCommand) {
        this.enclosingCommand = enclosingCommand;
    }

    public MemoryImageRemover(TaskHandlerCommand<?> enclosingCommand, boolean startPollingTasks) {
        this(enclosingCommand);
        this.startPollingTasks = startPollingTasks;
    }

    protected abstract boolean isMemoryStateRemovable(String memoryVolume);

    protected abstract DeleteImageGroupVDSCommandParameters buildDeleteMemoryImageParams(List<Guid> guids);

    protected abstract DeleteImageGroupVDSCommandParameters buildDeleteMemoryConfParams(List<Guid> guids);

    public void removeMemoryVolume(String memoryVolumes) {
        if (isMemoryStateRemovable(memoryVolumes)) {
            removeMemoryVolumes(memoryVolumes);
        }
    }

    public void removeMemoryVolumes(Set<String> memoryVolumes) {
        for (String memoryVols : memoryVolumes) {
            removeMemoryVolume(memoryVols);
        }
    }

    private boolean removeMemoryVolumes(String memVols) {
        List<Guid> guids = GuidUtils.getGuidListFromString(memVols);

        if (guids.size() != NUM_OF_UUIDS_IN_MEMORY_STATE) {
            log.warnFormat("Cannot remove memory volumes, invalid format: {0}", memVols);
            return true;
        }

        Guid memoryImageRemovalTaskId = removeMemoryImage(guids);
        if (memoryImageRemovalTaskId == null) {
            return false;
        }

        Guid confImageRemovalTaskId = removeConfImage(guids);

        if (startPollingTasks) {
            if (!Guid.Empty.equals(memoryImageRemovalTaskId)) {
                AsyncTaskManager.getInstance().StartPollingTask(memoryImageRemovalTaskId);
            }

            if (confImageRemovalTaskId != null && !Guid.Empty.equals(confImageRemovalTaskId)) {
                AsyncTaskManager.getInstance().StartPollingTask(confImageRemovalTaskId);
            }
        }

        return confImageRemovalTaskId != null;
    }

    protected Guid removeMemoryImage(List<Guid> guids) {
        return removeImage(
                VmCommand.DELETE_PRIMARY_IMAGE_TASK_KEY,
                buildDeleteMemoryImageParams(guids));
    }

    protected Guid removeConfImage(List<Guid> guids) {
        return removeImage(
                VmCommand.DELETE_SECONDARY_IMAGES_TASK_KEY,
                buildDeleteMemoryConfParams(guids));
    }

    protected Guid removeImage(String taskKey, DeleteImageGroupVDSCommandParameters parameters) {
        Guid taskId = enclosingCommand.persistAsyncTaskPlaceHolder(taskKey);

        VDSReturnValue vdsRetValue = removeImage(parameters);
        // if command succeeded, create a task
        if (vdsRetValue.getSucceeded()) {
            Guid guid = enclosingCommand.createTask(taskId, vdsRetValue.getCreationInfo(),
                    enclosingCommand.getActionType());
            enclosingCommand.getTaskIdList().add(guid);
            return guid;
        }
        // otherwise, if the command failed because the image already does not exist,
        // no need to create and monitor task, so we return empty guid to mark this state
        return vdsRetValue.getVdsError().getCode() == VdcBllErrors.ImageDoesNotExistInDomainError ?
                Guid.Empty : null;
    }

    protected VDSReturnValue removeImage(DeleteImageGroupVDSCommandParameters parameters) {
        try {
            return Backend.getInstance().getResourceManager().RunVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    parameters);
        }
        catch(VdcBLLException e) {
            if (e.getErrorCode() == VdcBllErrors.ImageDoesNotExistInDomainError) {
                return createImageDoesNotExistInDomainReturnValue();
            }
            throw e;
        }
    }

    protected VDSReturnValue createImageDoesNotExistInDomainReturnValue() {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(false);
        vdsRetValue.setVdsError(new VDSError(VdcBllErrors.ImageDoesNotExistInDomainError, ""));
        return vdsRetValue;
    }
}
