package org.ovirt.engine.core.bll.memory;

import java.util.List;

import org.ovirt.engine.core.bll.storage.PostZeroHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public class HibernationVolumesRemover extends MemoryImageRemover {

    private Boolean cachedPostZero;
    private Guid vmId;
    private String memoryState;

    public HibernationVolumesRemover(String memoryState, Guid vmId,
            TaskHandlerCommand<?> enclosingCommand) {
        this(memoryState, vmId, enclosingCommand, false);
    }

    public HibernationVolumesRemover(String memoryState, Guid vmId,
            TaskHandlerCommand<?> enclosingCommand, boolean startPollingTasks) {
        super(enclosingCommand, startPollingTasks);
        this.memoryState = memoryState;
        this.vmId = vmId;
    }

    public boolean remove() {
        return removeMemoryVolume(memoryState);
    }

    @Override
    protected DeleteImageGroupVDSCommandParameters buildDeleteMemoryImageParams(List<Guid> guids) {
        return PostZeroHandler.fixParametersWithPostZero(
                new DeleteImageGroupVDSCommandParameters(guids.get(1), guids.get(0), guids.get(2),
                        isPostZero(), false));
    }

    @Override
    protected DeleteImageGroupVDSCommandParameters buildDeleteMemoryConfParams(List<Guid> guids) {
        return PostZeroHandler.fixParametersWithPostZero(
                new DeleteImageGroupVDSCommandParameters(guids.get(1), guids.get(0), guids.get(4),
                        isPostZero(), false));
    }

    @Override
    protected Guid createTask(Guid taskId, VDSReturnValue vdsRetValue) {
        return enclosingCommand.createTask(
                taskId,
                vdsRetValue.getCreationInfo(),
                enclosingCommand.getParameters().getParentCommand());
    }

    protected boolean isPostZero() {
        if (cachedPostZero == null) {
            cachedPostZero = isDiskWithWipeAfterDeleteExist(getDiskDao().getAllForVm(vmId));
        }
        return cachedPostZero;
    }
}
