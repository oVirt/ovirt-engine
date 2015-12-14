package org.ovirt.engine.core.bll.memory;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.storage.domain.PostZeroHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;

public class MemoryImageRemoverOnDataDomain extends MemoryImageRemover {

    protected Boolean cachedPostZero;
    private Guid vmId;
    private boolean removeOnlyIfNotUsedAtAll;
    private boolean forceRemove;

    public MemoryImageRemoverOnDataDomain(Guid vmId,
            TaskHandlerCommand<? extends RemoveMemoryVolumesParameters> enclosingCommand, boolean forceRemove) {
        super(enclosingCommand, false);
        this.vmId = vmId;
        removeOnlyIfNotUsedAtAll = enclosingCommand.getParameters().isRemoveOnlyIfNotUsedAtAll();
        this.forceRemove = forceRemove;
    }

    public boolean remove(Set<String> memoryStates) {
        return removeMemoryVolumes(memoryStates);
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

    protected boolean isPostZero() {
        if (cachedPostZero == null) {
            cachedPostZero = isDiskWithWipeAfterDeleteExist(getDiskDao().getAllForVm(vmId));
        }

        return cachedPostZero;
    }

    @Override
    protected boolean isMemoryStateRemovable(String memoryVolume) {
        if (memoryVolume.isEmpty()) {
            return false;
        }
        if (forceRemove) {
            return true;
        }

        int numOfSnapshotsUsingThisMemory = getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume);
        return numOfSnapshotsUsingThisMemory == (removeOnlyIfNotUsedAtAll ? 0 : 1);
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }
}
