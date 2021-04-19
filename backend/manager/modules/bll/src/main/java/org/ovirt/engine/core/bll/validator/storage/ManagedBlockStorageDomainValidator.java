package org.ovirt.engine.core.bll.validator.storage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.EngineLocalConfig;

public class ManagedBlockStorageDomainValidator {

    private static final String CINDERLIB_DB_ENABLE = "CINDERLIB_DB_ENABLE";

    private static final Set<ActionType> allowedStorageOperations;
    private static final boolean isDataBaseInitialized;
    static {
        allowedStorageOperations = new HashSet<>();
        allowedStorageOperations.addAll(Arrays.asList(
                ActionType.AddDisk,
                ActionType.AttachDiskToVm,
                ActionType.DetachDiskFromVm,
                ActionType.RemoveDisk,
                ActionType.AddManagedBlockStorageDomain,
                ActionType.ActivateStorageDomain,
                ActionType.DeactivateStorageDomain,
                ActionType.DeactivateStorageDomainWithOvfUpdate,
                ActionType.AttachStorageDomainToPool,
                ActionType.DetachStorageDomainFromPool,
                ActionType.RemoveStorageDomain,
                ActionType.UpdateStorageDomain,
                ActionType.ForceRemoveStorageDomain,
                ActionType.UpdateDisk,
                ActionType.HotPlugDiskToVm,
                ActionType.HotUnPlugDiskFromVm,
                ActionType.AddVmTemplate,
                ActionType.RemoveVmTemplate,
                ActionType.CloneVm,
                ActionType.CreateSnapshotForVm,
                ActionType.RemoveSnapshot,
                ActionType.TryBackToAllSnapshotsOfVm,
                ActionType.RestoreAllSnapshots,
                ActionType.CopyData,
                ActionType.CopyImageGroupVolumesData,
                ActionType.CopyImageGroup,
                ActionType.CopyImageGroupWithData,
                ActionType.CopyManagedBlockDisk,
                ActionType.MoveOrCopyDisk
        ));
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        isDataBaseInitialized = Boolean.parseBoolean(config.getProperty(CINDERLIB_DB_ENABLE));
    }

    public ManagedBlockStorageDomainValidator() {
    }

    public static ValidationResult isOperationSupportedByManagedBlockStorage(ActionType actionType) {
        if (!isDataBaseInitialized) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CINDERLIB_DATA_BASE_REQUIRED);
        }
        return allowedStorageOperations.contains(actionType) ?
                ValidationResult.VALID :
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_FOR_MANAGED_BLOCK_STORAGE_TYPE);
    }
}
