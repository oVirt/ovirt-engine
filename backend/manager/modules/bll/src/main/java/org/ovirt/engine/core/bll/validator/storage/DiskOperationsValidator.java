package org.ovirt.engine.core.bll.validator.storage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class DiskOperationsValidator {

    static final Map<ActionType, List<DiskContentType>> allowedCommandsOnTypes = new HashMap<>();
    static {
        allowedCommandsOnTypes.put(ActionType.RemoveDisk,
                Arrays.asList(DiskContentType.DATA,
                        DiskContentType.OVF_STORE,
                        DiskContentType.MEMORY_DUMP_VOLUME,
                        DiskContentType.MEMORY_METADATA_VOLUME,
                        DiskContentType.ISO,
                        DiskContentType.BACKUP_SCRATCH));
        allowedCommandsOnTypes.put(ActionType.UpdateDisk, Arrays.asList(DiskContentType.DATA));
        allowedCommandsOnTypes.put(ActionType.AttachDiskToVm, Arrays.asList(DiskContentType.DATA, DiskContentType.HOSTED_ENGINE));
        allowedCommandsOnTypes.put(ActionType.MoveOrCopyDisk, Arrays.asList(DiskContentType.DATA,
                DiskContentType.MEMORY_DUMP_VOLUME, DiskContentType.MEMORY_METADATA_VOLUME, DiskContentType.ISO));
        allowedCommandsOnTypes.put(ActionType.LiveMigrateDisk, Arrays.asList(DiskContentType.DATA));
    }

    private Disk disk;

    public DiskOperationsValidator(Disk disk) {
        this.disk = disk;
    }

    public ValidationResult isOperationAllowedOnDisk(ActionType actionType) {
        List<DiskContentType> allowedTypes = allowedCommandsOnTypes.get(actionType);
        if (allowedTypes == null) {
            throw new IllegalArgumentException(
                    String.format("Cannot validate operation on disk for action of type %s as it is not an allowed command for the disk content type.", actionType));
        }

        if (!allowedTypes.contains(disk.getContentType())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_CONTENT_TYPE_NOT_SUPPORTED_FOR_OPERATION,
                    String.format("$diskContentType %s", disk.getContentType()));
        }

        return ValidationResult.VALID;
    }

}
