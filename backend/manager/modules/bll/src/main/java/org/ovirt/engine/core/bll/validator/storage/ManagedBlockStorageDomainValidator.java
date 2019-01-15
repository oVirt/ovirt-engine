package org.ovirt.engine.core.bll.validator.storage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class ManagedBlockStorageDomainValidator extends StorageDomainValidator {
    private static final Set<ActionType> allowedStorageOperations;
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
                ActionType.UpdateVmDisk,
                ActionType.HotPlugDiskToVm,
                ActionType.HotUnPlugDiskFromVm,
                ActionType.AddVmTemplate
        ));

    }

    public ManagedBlockStorageDomainValidator(StorageDomain domain) {
        super(domain);
    }

    public ValidationResult isOperationSupportedByManagedBlockStorage(ActionType actionType) {
        return allowedStorageOperations.contains(actionType) ?
                ValidationResult.VALID :
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_FOR_MANAGED_BLOCK_STORAGE_TYPE);
    }
}
