package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;

public class DisksFilter {
    /**
     * Filters out all Disks that are not images
     */
    public static final Predicate<Disk> ONLY_IMAGES = d -> d.getDiskStorageType() == DiskStorageType.IMAGE;

    /**
     * Filters out all disks that are not snapable (retains only disks that we can take a snapshot of)
     */
    public static final Predicate<Disk> ONLY_SNAPABLE = Disk::isAllowSnapshot;

    /**
     * Filters out all disks that are shareable
     */
    public static final Predicate<Disk> ONLY_NOT_SHAREABLE = d -> !d.isShareable();

    /**
     * Filters out all disks that are not active
     */
    public static final Predicate<Disk> ONLY_ACTIVE = d -> Boolean.TRUE.equals(((DiskImage) d).getActive());

    /**
     * This method is filtering a list of disks retaining only disk images and continues to filter the list by the
     * specified predicates.
     *
     * @param disks The collection of disks to filter
     * @param predicates The predicates to filter by
     * @return A filtered list of disks
     */
    public static List<DiskImage> filterImageDisks(Collection<? extends Disk> disks, Predicate<Disk>... predicates) {
        Predicate<Disk> chain = Stream.concat(Stream.of(ONLY_IMAGES), Arrays.stream(predicates)).reduce(Predicate::and).orElse(p -> true);

        return disks.stream().filter(chain)
                .map(DiskImage.class::cast)
                .collect(Collectors.toList());
    }
}
