package org.ovirt.engine.core.bll.memory;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class MemoryImageRemoverOnDataDomain extends MemoryImageRemover {

    public MemoryImageRemoverOnDataDomain(VM vm, TaskHandlerCommand<?> enclosingCommand) {
        super(vm, enclosingCommand);
    }

    @Override
    protected DeleteImageGroupVDSCommandParameters buildDeleteMemoryImageParams(List<Guid> guids) {
        return new DeleteImageGroupVDSCommandParameters(
                guids.get(1), guids.get(0), guids.get(2), isPostZero(), false);
    }

    @Override
    protected DeleteImageGroupVDSCommandParameters buildDeleteMemoryConfParams(List<Guid> guids) {
        return new DeleteImageGroupVDSCommandParameters(
                guids.get(1), guids.get(0), guids.get(4), isPostZero(), false);
    }

    protected boolean isPostZero() {
        if (cachedPostZero == null) {
            // check if one of the disks is marked with wipe_after_delete
            cachedPostZero =
                    getDbFacade().getDiskDao().getAllForVm(vm.getId()).contains(
                            new Object() {
                                @Override
                                public boolean equals(Object obj) {
                                    return ((Disk) obj).isWipeAfterDelete();
                                }
                            });
        }
        return cachedPostZero;
    }

    /**
     * There is a one to many relation between memory volumes and snapshots, so memory
     * volumes should be removed only if the only snapshot that points to them is removed
     */
    @Override
    protected boolean shouldRemoveMemorySnapshotVolumes(String memoryVolume) {
        return !memoryVolume.isEmpty() &&
                getDbFacade().getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 1;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }
}
