package org.ovirt.engine.core.bll.memory;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.HibernateVmCommand;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

/**
 * This builder creates the memory images for live snapshots with memory operation
 */
public class DefaultMemoryImageBuilder implements MemoryImageBuilder {
    private Guid storageDomainId;
    private Guid memoryDumpImageGroupId;
    private Guid memoryDumpVolumeId;
    private Guid vmConfImageGroupId;
    private Guid vmConfVolumeId;
    private VM vm;
    private TaskHandlerCommand<?> enclosingCommand;
    private StoragePool storagePool;

    public DefaultMemoryImageBuilder(VM vm, Guid storageDomainId,
            StoragePool storagePool, TaskHandlerCommand<?> enclosingCommand) {
        this.vm = vm;
        this.enclosingCommand = enclosingCommand;
        this.storageDomainId = storageDomainId;
        this.storagePool = storagePool;
        this.memoryDumpImageGroupId = Guid.NewGuid();
        this.memoryDumpVolumeId = Guid.NewGuid();
        this.vmConfImageGroupId = Guid.NewGuid();
        this.vmConfVolumeId = Guid.NewGuid();
    }

    public void build() {
        createImageForVmMetaData();
        createImageForMemoryDump();
    }

    private void createImageForVmMetaData() {
        VDSReturnValue retVal =
                Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CreateImage,
                        new CreateImageVDSCommandParameters(vm.getStoragePoolId(),
                                storageDomainId,
                                vmConfImageGroupId,
                                HibernateVmCommand.META_DATA_SIZE_IN_BYTES,
                                VolumeType.Sparse,
                                VolumeFormat.COW,
                                vmConfVolumeId,
                                "",
                                storagePool.getcompatibility_version()
                                .toString()));

        if (!retVal.getSucceeded()) {
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError,
                    "Failed to create image for vm configuration!");
        }

        Guid guid = enclosingCommand.createTask(
                retVal.getCreationInfo(),
                enclosingCommand.getActionType());
        enclosingCommand.getTaskIdList().add(guid);
    }

    private void createImageForMemoryDump() {
        VDSReturnValue retVal =
                Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CreateImage,
                        new CreateImageVDSCommandParameters(
                                storagePool.getId(),
                                storageDomainId,
                                memoryDumpImageGroupId,
                                vm.getTotalMemorySizeInBytes(),
                                VolumeType.Preallocated,
                                VolumeFormat.RAW,
                                memoryDumpVolumeId,
                                "",
                                storagePool.getcompatibility_version().toString()));

        if (!retVal.getSucceeded()) {
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError,
                    "Failed to create image for memory!");
        }

        Guid guid =
                enclosingCommand.createTask(retVal.getCreationInfo(),
                        enclosingCommand.getActionType(),
                        VdcObjectType.Storage,
                        storageDomainId);
        enclosingCommand.getTaskIdList().add(guid);
    }

    public String getVolumeStringRepresentation() {
        return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s",
                storageDomainId,
                storagePool.getId(),
                memoryDumpImageGroupId,
                memoryDumpVolumeId,
                vmConfImageGroupId,
                vmConfVolumeId);
    }

    public boolean isCreateTasks() {
        return true;
    }
}
