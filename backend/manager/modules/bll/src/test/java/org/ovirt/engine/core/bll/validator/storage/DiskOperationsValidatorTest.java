package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class DiskOperationsValidatorTest {

    @Test
    public void testAllowedOperations() {
        Disk disk = new DiskImage();
        for (Map.Entry<ActionType, List<DiskContentType>> entry : DiskOperationsValidator.allowedCommandsOnTypes.entrySet()) {
            disk.setContentType(entry.getValue().get(0));
            DiskOperationsValidator validator = new DiskOperationsValidator(disk);
            assertThat(validator.isOperationAllowedOnDisk(entry.getKey()), isValid());
        }
    }

    @Test
    public void testDisallowedOperations() {
        Disk disk = new DiskImage();
        for (Map.Entry<ActionType, List<DiskContentType>> entry : DiskOperationsValidator.allowedCommandsOnTypes.entrySet()) {
            EnumSet<DiskContentType> allowedTypes = EnumSet.copyOf(entry.getValue());
            EnumSet<DiskContentType> disallowedTypes = EnumSet.complementOf(allowedTypes);
            if (disallowedTypes.isEmpty()) {
                continue;
            }

            disk.setContentType(disallowedTypes.iterator().next());
            DiskOperationsValidator validator = new DiskOperationsValidator(disk);
            assertThat(validator.isOperationAllowedOnDisk(entry.getKey()),
                    failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_CONTENT_TYPE_NOT_SUPPORTED_FOR_OPERATION,
                            String.format("$diskContentType %s", disk.getContentType())));
        }
    }
}
