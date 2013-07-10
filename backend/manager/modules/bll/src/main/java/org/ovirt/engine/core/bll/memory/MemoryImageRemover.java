package org.ovirt.engine.core.bll.memory;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.bll.AsyncTaskManager;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class MemoryImageRemover {

    private TaskHandlerCommand<?> enclosingCommand;
    private VM vm;

    public MemoryImageRemover(VM vm, TaskHandlerCommand<?> enclosingCommand) {
        this.enclosingCommand = enclosingCommand;
        this.vm = vm;
    }

    public void removeMemoryVolume(String memoryVolumes) {
        if (shouldRemoveMemorySnapshotVolumes(memoryVolumes)) {
            removeMemoryVolumes(memoryVolumes, false);
        }
    }

    public void removeMemoryVolumes(Collection<String> memoryVolumes) {
        for (String memoryVols : memoryVolumes) {
            removeMemoryVolume(memoryVols);
        }
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    /**
     * There is a one to many relation between memory volumes and snapshots, so memory
     * volumes should be removed only if the only snapshot that points to them is removed
     */
    protected boolean shouldRemoveMemorySnapshotVolumes(String memoryVolume) {
        return !memoryVolume.isEmpty() &&
                getDbFacade().getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 1;
    }

    protected boolean removeMemoryVolumes(String memVols, boolean startPollingTasks) {
        List<Guid> guids = GuidUtils.getGuidListFromString(memVols);

        if (guids.size() == 6) {
            // get all vm disks in order to check post zero - if one of the
            // disks is marked with wipe_after_delete
            boolean postZero =
                    LinqUtils.filter(
                            getDbFacade().getDiskDao().getAllForVm(vm.getId()),
                            new Predicate<Disk>() {
                                @Override
                                public boolean eval(Disk disk) {
                                    return disk.isWipeAfterDelete();
                                }
                            }).size() > 0;

            Guid taskId1 = enclosingCommand.persistAsyncTaskPlaceHolder(VmCommand.DELETE_PRIMARY_IMAGE_TASK_KEY);
            // delete first image
            // the next 'DeleteImageGroup' command should also take care of the image removal:
            VDSReturnValue vdsRetValue =
                    Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DeleteImageGroup,
                            new DeleteImageGroupVDSCommandParameters(guids.get(1), guids.get(0),
                                    guids.get(2), postZero, false));

            if (!vdsRetValue.getSucceeded()) {
                return false;
            }

            Guid guid1 =
                    enclosingCommand.createTask(taskId1, vdsRetValue.getCreationInfo(),
                            enclosingCommand.getActionType(), VdcObjectType.Storage, guids.get(0));
            enclosingCommand.getTaskIdList().add(guid1);

            Guid taskId2 = enclosingCommand.persistAsyncTaskPlaceHolder(VmCommand.DELETE_SECONDARY_IMAGES_TASK_KEY);
            // delete second image
            // the next 'DeleteImageGroup' command should also take care of the image removal:
            vdsRetValue =
                    Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DeleteImageGroup,
                            new DeleteImageGroupVDSCommandParameters(guids.get(1), guids.get(0),
                                    guids.get(4), postZero, false));

            if (!vdsRetValue.getSucceeded()) {
                if (startPollingTasks) {
                    AsyncTaskManager.getInstance().StartPollingTask(guid1);
                }
                return false;
            }

            Guid guid2 = enclosingCommand.createTask(taskId2, vdsRetValue.getCreationInfo(),
                    enclosingCommand.getActionType());
            enclosingCommand.getTaskIdList().add(guid2);

            if (startPollingTasks) {
                AsyncTaskManager.getInstance().StartPollingTask(guid1);
                AsyncTaskManager.getInstance().StartPollingTask(guid2);
            }
        }

        return true;
    }

}
