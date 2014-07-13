package org.ovirt.engine.core.bll.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.HibernateVmCommand;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
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
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This builder creates the memory images for live snapshots with memory operation
 */
public class LiveSnapshotMemoryImageBuilder implements MemoryImageBuilder {
    private static final String CREATE_IMAGE_FOR_VM_TASK_KEY = "CREATE_IMAGE_FOR_VM_TASK_KEY";
    private static final String CREATE_IMAGE_FOR_MEMORY_DUMP_TASK_KEY = "CREATE_IMAGE_FOR_MEMORY_DUMP_TASK_KEY";

    private Guid storageDomainId;
    private Guid memoryDumpImageGroupId;
    private Guid memoryDumpVolumeId;
    private Guid vmConfImageGroupId;
    private Guid vmConfVolumeId;
    private VM vm;
    private TaskHandlerCommand<?> enclosingCommand;
    private StoragePool storagePool;
    private VolumeType volumeTypeForDomain;

    public LiveSnapshotMemoryImageBuilder(VM vm, Guid storageDomainId,
            StoragePool storagePool, TaskHandlerCommand<?> enclosingCommand) {
        this.vm = vm;
        this.enclosingCommand = enclosingCommand;
        this.storageDomainId = storageDomainId;
        this.storagePool = storagePool;
        this.memoryDumpImageGroupId = Guid.newGuid();
        this.memoryDumpVolumeId = Guid.newGuid();
        this.vmConfImageGroupId = Guid.newGuid();
        this.vmConfVolumeId = Guid.newGuid();
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
                        new CreateImageVDSCommandParameters(
                                storagePool.getId(),
                                storageDomainId,
                                vmConfImageGroupId,
                                HibernateVmCommand.META_DATA_SIZE_IN_BYTES,
                                VolumeType.Sparse,
                                VolumeFormat.COW,
                                vmConfVolumeId,
                                ""));

        if (!retVal.getSucceeded()) {
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError,
                    "Failed to create image for vm configuration!");
        }

        Guid taskId = enclosingCommand.persistAsyncTaskPlaceHolder(CREATE_IMAGE_FOR_VM_TASK_KEY);
        Guid guid = enclosingCommand.createTask(
                taskId,
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
                                getVolumeTypeForDomain(),
                                VolumeFormat.RAW,
                                memoryDumpVolumeId,
                                ""));

        if (!retVal.getSucceeded()) {
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError,
                    "Failed to create image for memory!");
        }

        Guid taskId = enclosingCommand.persistAsyncTaskPlaceHolder(CREATE_IMAGE_FOR_MEMORY_DUMP_TASK_KEY);
        Guid guid =
                enclosingCommand.createTask(taskId,
                        retVal.getCreationInfo(),
                        enclosingCommand.getActionType(),
                        VdcObjectType.Storage,
                        storageDomainId);
        enclosingCommand.getTaskIdList().add(guid);
    }

    private VolumeType getVolumeTypeForDomain() {
        if (volumeTypeForDomain == null) {
            StorageDomainStatic sdStatic = DbFacade.getInstance().getStorageDomainStaticDao().get(storageDomainId);
            volumeTypeForDomain = HibernateVmCommand.getMemoryVolumeTypeForStorageDomain(sdStatic.getStorageType());
        }
        return volumeTypeForDomain;
    }


    public String getVolumeStringRepresentation() {
        return MemoryUtils.createMemoryStateString(
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

    public List<DiskImage> getDisksToBeCreated() {
        DiskImage imageForMemory = new DiskImage();
        imageForMemory.setStorageIds(new ArrayList<Guid>(Collections.singletonList(storageDomainId)));
        imageForMemory.setStoragePoolId(storagePool.getId());
        imageForMemory.setSize(vm.getTotalMemorySizeInBytes());
        imageForMemory.setVolumeType(getVolumeTypeForDomain());
        imageForMemory.setvolumeFormat(VolumeFormat.RAW);

        DiskImage imageForMetadata = DiskImage.copyOf(imageForMemory);
        imageForMetadata.setSize(HibernateVmCommand.META_DATA_SIZE_IN_BYTES);
        imageForMetadata.setVolumeType(VolumeType.Sparse);
        imageForMetadata.setvolumeFormat(VolumeFormat.COW);
        return Arrays.asList(imageForMemory, imageForMetadata);
    }
}
