package org.ovirt.engine.core.bll.memory;

import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.storage.domain.PostDeleteActionHandler;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.di.Injector;

public class MemoryImageRemoverFromExportDomain extends MemoryImageRemover {

    private Guid storagePoolId;
    private Guid storageDomainId;
    protected Boolean cachedPostZero;
    private VM vm;
    private PostDeleteActionHandler postDeleteActionHandler;
    private StorageDomainDao storageDomainDao;

    public MemoryImageRemoverFromExportDomain(VM vm, CommandBase<?> enclosingCommand,
            Guid storagePoolId, Guid storageDomainId) {
        super(enclosingCommand);
        this.vm = vm;
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        postDeleteActionHandler = Injector.get(PostDeleteActionHandler.class);
        storageDomainDao = Injector.get(StorageDomainDao.class);
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
        boolean discardAfterDelete = storageDomainDao.get(guids.get(0)).isDiscardAfterDelete();
        return postDeleteActionHandler.fixParameters(
                new DeleteImageGroupVDSCommandParameters(guids.get(1), guids.get(0), guids.get(2),
                        isPostZero(), discardAfterDelete, false));
    }

    @Override
    protected DeleteImageGroupVDSCommandParameters buildDeleteMemoryConfParams(List<Guid> guids) {
        boolean discardAfterDelete = storageDomainDao.get(guids.get(0)).isDiscardAfterDelete();
        return postDeleteActionHandler.fixParameters(
                new DeleteImageGroupVDSCommandParameters(guids.get(1), guids.get(0), guids.get(4),
                        isPostZero(), discardAfterDelete, false));
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
