package org.ovirt.engine.core.bll.memory;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.di.Injector;

/**
 * This builder creates the memory images for live snapshots with memory operation
 */
public class LiveSnapshotMemoryImageBuilder implements MemoryImageBuilder {

    private Guid storageDomainId;
    private DiskImage memoryDisk;
    private DiskImage metadataDisk;
    private VM vm;
    private CommandBase<?> enclosingCommand;
    private VmOverheadCalculator vmOverheadCalculator;
    private String snapshotDescription;
    private boolean wipeAfterDelete;

    public LiveSnapshotMemoryImageBuilder(VM vm, Guid storageDomainId,
            CommandBase<?> enclosingCommand, VmOverheadCalculator vmOverheadCalculator,
            String snapshotDescription, boolean wipeAfterDelete) {
        this.vm = vm;
        this.enclosingCommand = enclosingCommand;
        this.storageDomainId = storageDomainId;
        this.vmOverheadCalculator = vmOverheadCalculator;
        this.snapshotDescription = snapshotDescription;
        this.wipeAfterDelete = wipeAfterDelete;
    }

    @Override
    public void build() {
        Guid memoryDiskId = addMemoryDisk();
        Guid metadataDiskId = addMetadataDisk();
        // Have to query for the disks in order to get its imageId
        memoryDisk = getDisk(memoryDiskId);
        metadataDisk = getDisk(metadataDiskId);
    }

    private Guid addMemoryDisk() {
        DiskImage memoryDisk = MemoryUtils.createSnapshotMemoryDisk(vm, getStorageType(), vmOverheadCalculator,
                MemoryUtils.generateMemoryDiskDescription(vm, snapshotDescription));
        memoryDisk.setWipeAfterDelete(wipeAfterDelete);
        return addDisk(memoryDisk);
    }

    private Guid addMetadataDisk() {
        DiskImage metadataDisk = MemoryUtils.createSnapshotMetadataDisk(vm.getName(),
                MemoryUtils.generateMemoryDiskDescription(vm, snapshotDescription));
        metadataDisk.setWipeAfterDelete(wipeAfterDelete);
        return addDisk(metadataDisk);
    }

    private Guid addDisk(DiskImage disk) {
        ActionReturnValue returnValue = Injector.get(BackendInternal.class).runInternalAction(
                ActionType.AddDisk,
                buildAddDiskParameters(disk),
                enclosingCommand.getContext().clone());

        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getFault().getError(),
                    String.format("Failed to create disk! %s", disk.getDiskAlias()));
        }

        return returnValue.getActionReturnValue();
    }

    private DiskImage getDisk(Guid diskId) {
        return (DiskImage) getDiskDao().get(diskId);
    }

    protected DiskDao getDiskDao() {
        return Injector.get(DiskDao.class);
    }

    private AddDiskParameters buildAddDiskParameters(DiskImage disk) {
        AddDiskParameters parameters = new AddDiskParameters(disk);
        parameters.setStorageDomainId(storageDomainId);
        parameters.setParentCommand(enclosingCommand.getActionType());
        parameters.setParentParameters(enclosingCommand.getParameters());
        parameters.setShouldBeLogged(false);
        return parameters;
    }

    private StorageType getStorageType() {
        return getStorageDomainStaticDao().get(storageDomainId).getStorageType();
    }

    protected StorageDomainStaticDao getStorageDomainStaticDao() {
        return Injector.get(StorageDomainStaticDao.class);
    }

    @Override
    public boolean isCreateTasks() {
        return true;
    }

    @Override
    public Guid getMemoryDiskId() {
        return memoryDisk.getId();
    }

    @Override
    public Guid getMetadataDiskId() {
        return metadataDisk.getId();
    }
}
