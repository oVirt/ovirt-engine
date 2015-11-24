package org.ovirt.engine.core.bll.memory;

import java.util.List;

import org.ovirt.engine.core.bll.storage.domain.PostZeroHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class MemoryImageRemoverFromExportDomain extends MemoryImageRemover {

    private Guid storagePoolId;
    private Guid storageDomainId;
    protected Boolean cachedPostZero;
    private VM vm;

    public MemoryImageRemoverFromExportDomain(VM vm, TaskHandlerCommand<?> enclosingCommand,
            Guid storagePoolId, Guid storageDomainId) {
        super(enclosingCommand);
        this.vm = vm;
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
    }

    public void remove() {
        removeMemoryVolumes(MemoryUtils.getMemoryVolumesFromSnapshots(vm.getSnapshots()));
    }

    @Override
    protected boolean removeMemoryVolume(String memoryVolumes) {
        return super.removeMemoryVolume(
                MemoryUtils.changeStorageDomainAndPoolInMemoryState(
                        memoryVolumes, storageDomainId, storagePoolId));
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

    /**
     * We set the post zero field on memory image deletion from export domain as we do
     * when it is deleted from data domain even though the export domain is NFS and NFS
     * storage do the wipe on its own, in order to be compliance with the rest of the
     * code that do the same, and to be prepared for supporting export domains which
     * are not NFS.
     */
    protected boolean isPostZero() {
        if (cachedPostZero == null) {
            cachedPostZero = isDiskWithWipeAfterDeleteExist(vm.getDiskMap().values());
        }
        return cachedPostZero;
    }
}
