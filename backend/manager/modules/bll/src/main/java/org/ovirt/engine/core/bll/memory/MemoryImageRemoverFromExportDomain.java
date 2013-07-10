package org.ovirt.engine.core.bll.memory;

import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class MemoryImageRemoverFromExportDomain extends MemoryImageRemover {

    private Guid storagePoolId;
    private Guid storageDomainId;

    public MemoryImageRemoverFromExportDomain(VM vm, TaskHandlerCommand<?> enclosingCommand,
            Guid storagePoolId, Guid storageDomainId) {
        super(vm, enclosingCommand);
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
    }

    /**
     * We set the post zero field on memory image deletion from export domain as we do
     * when it is deleted from data domain even though the export domain is NFS and NFS
     * storage do the wipe on its own, in order to be compliance with the rest of the
     * code that do the same, and to be prepared for supporting export domains which
     * are not NFS.
     */
    @Override
    protected boolean isPostZero() {
        if (cachedPostZero == null) {
            // check if one of the disks is marked with wipe_after_delete
            cachedPostZero =
                    vm.getDiskMap().values().contains(new Object() {
                        @Override
                        public boolean equals(Object obj) {
                            return ((Disk) obj).isWipeAfterDelete();
                        }
                    });
        }
        return cachedPostZero;
    }

    @Override
    protected boolean shouldRemoveMemorySnapshotVolumes(String memoryVolume) {
        return !memoryVolume.isEmpty();
    }

    @Override
    public void removeMemoryVolume(String memoryVolumes) {
        super.removeMemoryVolume(
                MemoryUtils.changeStorageDomainAndPoolInMemoryVolume(
                        memoryVolumes, storageDomainId, storagePoolId));
    }
}
