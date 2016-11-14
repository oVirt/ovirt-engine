package org.ovirt.engine.core.bll.validator.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public class MultipleDiskVmElementValidator {

    protected Map<Disk, Collection<DiskVmElement>> diskToDiskVmElements;

    /**
     * For a single disk with one or more disk vm elements.
     */
    public MultipleDiskVmElementValidator(Disk disk, Collection<DiskVmElement> diskVmElements) {
        diskToDiskVmElements = Collections.singletonMap(disk, diskVmElements);
    }

    /**
     * For a few disks, each with its corresponding vm element.
     */
    public MultipleDiskVmElementValidator(Map<Disk, DiskVmElement> diskToDiskVmElement) {
        diskToDiskVmElements = diskToDiskVmElement.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, diskImageDiskVmElementEntry ->
                        Collections.singleton(diskImageDiskVmElementEntry.getValue())));
    }

    /**
     * For testing purposes.
     */
    protected MultipleDiskVmElementValidator() {
    }

    public ValidationResult isPassDiscardSupportedForDestSd(Guid destSdId) {
        return isPassDiscardSupportedForDestSds(diskToDiskVmElements.keySet().stream()
                .collect(Collectors.toMap(Disk::getId, disk -> destSdId)));
    }

    public ValidationResult isPassDiscardSupportedForDestSds(Map<Guid, Guid> diskIdToDestSdId) {
        return getDisksValidators().stream()
                .map(diskVmElementValidator -> diskVmElementValidator.isPassDiscardSupported(
                        diskIdToDestSdId.get(diskVmElementValidator.getDiskId())))
                .filter(validationResult -> !validationResult.isValid())
                .findAny().orElse(ValidationResult.VALID);
    }

    public Collection<Guid> getDisksWithoutSupportForPassDiscard(Map<Guid, Guid> diskIdToDestSdId) {
        return getDisksValidators().stream()
                .filter(diskVmElementValidator -> !diskVmElementValidator.isPassDiscardSupported(
                        diskIdToDestSdId.get(diskVmElementValidator.getDiskId())).isValid())
                .map(DiskVmElementValidator::getDiskId)
                .collect(Collectors.toList());
    }

    protected DiskVmElementValidator createDiskVmElementValidator(Disk disk, DiskVmElement diskVmElement) {
        return new DiskVmElementValidator(disk, diskVmElement);
    }

    private Collection<DiskVmElementValidator> getDisksValidators() {
        return diskToDiskVmElements.entrySet().stream()
                .map(diskCollectionEntry -> diskCollectionEntry.getValue().stream()
                        .map(diskVmElement -> createDiskVmElementValidator(
                                diskCollectionEntry.getKey(), diskVmElement)))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }
}
