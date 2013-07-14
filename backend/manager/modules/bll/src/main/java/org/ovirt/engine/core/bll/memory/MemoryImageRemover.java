package org.ovirt.engine.core.bll.memory;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.AsyncTaskManager;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.GuidUtils;

public abstract class MemoryImageRemover {

    private TaskHandlerCommand<?> enclosingCommand;
    protected VM vm;
    protected Boolean cachedPostZero;

    public MemoryImageRemover(VM vm, TaskHandlerCommand<?> enclosingCommand) {
        this.enclosingCommand = enclosingCommand;
        this.vm = vm;
    }

    protected abstract boolean shouldRemoveMemorySnapshotVolumes(String memoryVolume);

    protected abstract DeleteImageGroupVDSCommandParameters buildDeleteMemoryImageParams(List<Guid> guids);

    protected abstract DeleteImageGroupVDSCommandParameters buildDeleteMemoryConfParams(List<Guid> guids);

    public void removeMemoryVolume(String memoryVolumes) {
        if (shouldRemoveMemorySnapshotVolumes(memoryVolumes)) {
            removeMemoryVolumes(memoryVolumes, false);
        }
    }

    public void removeMemoryVolumes(Set<String> memoryVolumes) {
        for (String memoryVols : memoryVolumes) {
            removeMemoryVolume(memoryVols);
        }
    }

    protected boolean removeMemoryVolumes(String memVols, boolean startPollingTasks) {
        List<Guid> guids = GuidUtils.getGuidListFromString(memVols);

        if (guids.size() == 6) {

            Guid guid1 = removeMemoryImage(guids);
            if (guid1 == null) {
                return false;
            }

            Guid guid2 = removeConfImage(guids);

            if (startPollingTasks) {
                AsyncTaskManager.getInstance().StartPollingTask(guid1);

                if (guid2 != null) {
                    AsyncTaskManager.getInstance().StartPollingTask(guid2);
                }
            }

            return guid2 != null;
        }

        return true;
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
        Guid taskId2 = enclosingCommand.persistAsyncTaskPlaceHolder(taskKey);

        VDSReturnValue vdsRetValue = Backend.getInstance().getResourceManager().RunVdsCommand(
                VDSCommandType.DeleteImageGroup,
                parameters);

        if (!vdsRetValue.getSucceeded()) {
            return null;
        }

        Guid guid2 = enclosingCommand.createTask(taskId2, vdsRetValue.getCreationInfo(),
                enclosingCommand.getActionType());
        enclosingCommand.getTaskIdList().add(guid2);
        return guid2;
    }
}
