package org.ovirt.engine.core.bll.memory;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

/**
 * This builder creates the memory images for live snapshots with memory operation
 */
public class LiveSnapshotMemoryImageBuilder implements MemoryImageBuilder {

    private Guid storageDomainId;
    private DiskImage memoryDisk;
    private DiskImage metadataDisk;
    private VM vm;
    private CommandBase<?> enclosingCommand;
    private StoragePool storagePool;

    public LiveSnapshotMemoryImageBuilder(VM vm, Guid storageDomainId,
            StoragePool storagePool, CommandBase<?> enclosingCommand) {
        this.vm = vm;
        this.enclosingCommand = enclosingCommand;
        this.storageDomainId = storageDomainId;
        this.storagePool = storagePool;
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
        DiskImage memoryDisk = MemoryUtils.createSnapshotMemoryDisk(vm, getStorageType());
        return addDisk(memoryDisk);
    }

    private Guid addMetadataDisk() {
        DiskImage metadataDisk = MemoryUtils.createSnapshotMetadataDisk();
        return addDisk(metadataDisk);
    }

    private Guid addDisk(DiskImage disk) {
        VdcReturnValueBase returnValue = getBackend().runInternalAction(
                VdcActionType.AddDisk,
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
        return DbFacade.getInstance().getDiskDao();
    }

    private AddDiskParameters buildAddDiskParameters(DiskImage disk) {
        AddDiskParameters parameters = new AddDiskParameters(disk);
        parameters.setStorageDomainId(storageDomainId);
        parameters.setParentCommand(enclosingCommand.getActionType());
        parameters.setParentParameters(enclosingCommand.getParameters());
        parameters.setShouldBeLogged(false);
        return parameters;
    }

    private BackendInternal getBackend() {
        return Backend.getInstance();
    }

    private StorageType getStorageType() {
        return getStorageDomainStaticDao().get(storageDomainId).getStorageType();
    }

    protected StorageDomainStaticDao getStorageDomainStaticDao() {
        return DbFacade.getInstance().getStorageDomainStaticDao();
    }

    @Override
    public String getVolumeStringRepresentation() {
        return MemoryUtils.createMemoryStateString(
                storageDomainId,
                storagePool.getId(),
                memoryDisk.getId(),
                memoryDisk.getImageId(),
                metadataDisk.getId(),
                metadataDisk.getImageId());
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
