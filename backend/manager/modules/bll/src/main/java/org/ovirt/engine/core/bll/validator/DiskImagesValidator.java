package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * A validator for the {@link DiskImage} class. Since most usecases require validations of multiple {@link DiskImage}s
 * (e.g., all the disks belonging to a VM/template), this class works on a {@link Collection} of {@link DiskImage}s.
 *
 */
public class DiskImagesValidator {

    private Collection<DiskImage> diskImages;

    public DiskImagesValidator(Collection<DiskImage> disks) {
        this.diskImages = disks;
    }

    /**
     * Validates that non of the disk are {@link ImageStatus#ILLEGAL}.
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesNotIllegal() {
        return diskImagesNotInStatus(ImageStatus.ILLEGAL, VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL);
    }

    /**
     * Validates that non of the disk are {@link ImageStatus#LOCKED}.
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesNotLocked() {
        return diskImagesNotInStatus(ImageStatus.LOCKED, VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    /**
     * Validates that non of the disk are in the given {@link #status}.
     *
     * @param status
     *            The status to check
     * @param failMessage
     *            The validation message to return in case of failure.
     * @return A {@link ValidationResult} with the validation information. If none of the disks are in the given status,
     *         {@link ValidationResult#VALID} is returned. If one or more disks are in that status, a
     *         {@link ValidationResult} with {@link #failMessage} and the names of the disks in that status is returned.
     */
    private ValidationResult diskImagesNotInStatus(ImageStatus status, VdcBllMessages failMessage) {
        List<String> disksInStatus = new ArrayList<String>();
        for (DiskImage diskImage : diskImages) {
            if (diskImage.getImageStatus() == status) {
                disksInStatus.add(diskImage.getDiskAlias());
            }
        }

        if (!disksInStatus.isEmpty()) {
            return new ValidationResult(failMessage,
                    String.format("$diskAliases %s", StringUtils.join(disksInStatus, ", ")));
        }

        return ValidationResult.VALID;
    }
}
