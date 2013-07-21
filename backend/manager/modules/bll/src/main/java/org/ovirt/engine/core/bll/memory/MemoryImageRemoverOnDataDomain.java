package org.ovirt.engine.core.bll.memory;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class MemoryImageRemoverOnDataDomain extends MemoryImageRemover {

    protected Boolean cachedPostZero;
    private VM vm;

    public MemoryImageRemoverOnDataDomain(VM vm, TaskHandlerCommand<?> enclosingCommand) {
        super(enclosingCommand);
        this.vm = vm;
    }

    public void remove(Set<String> memoryStates) {
        removeMemoryVolumes(memoryStates);
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

    @Override
    protected boolean isMemoryStateRemovable(String memoryVolume) {
        return !memoryVolume.isEmpty() &&
                getDbFacade().getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 0;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }
}
