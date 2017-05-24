package org.ovirt.engine.core.bll.validator.storage;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;

/**
 * A validator for checking if disks exist (in the engine database)
 *
 */
public class DiskExistenceValidator {

    @Inject
    private BaseDiskDao baseDiskDao;

    private Collection<Guid> diskGuids;

    public DiskExistenceValidator(Collection<Guid> guids) {
        this.diskGuids = guids;
    }

    /**
     * Validates that the disks exists
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult disksNotExist() {
        String disksNotExistInDbIds =
                diskGuids.stream()
                        .filter(guid -> !isDiskExists(guid))
                        .map(Guid::toString)
                        .collect(Collectors.joining(", "));

        if (!disksNotExistInDbIds.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST,
                    String.format("$diskIds %s", disksNotExistInDbIds));
        }

        return ValidationResult.VALID;
    }

    private boolean isDiskExists(Guid id) {
        return baseDiskDao.exists(id);
    }

}
