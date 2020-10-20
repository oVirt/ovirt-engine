package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;

public class DisksFilter {

    /**
     * A generic {@link Predicate} for filtering {@link Disk}s by their storage type and then casting them
     * to the appropriate concrete implementation.
     */
    private static class DiskStorageTypePredicate<T extends Disk> implements Predicate<Disk> {
        private Class<T> implementingDiskType;

        private DiskStorageTypePredicate(Class<T> implementingDiskType) {
            this.implementingDiskType = implementingDiskType;
        }

        @Override
        public boolean test(Disk disk) {
            return DiskStorageType.forClass(implementingDiskType) == disk.getDiskStorageType();
        }

        public Class<T> getImplementingDiskType() {
            return implementingDiskType;
        }
    }

    /**
     * Filters out all disks that are not images.
     */
    public static final DiskStorageTypePredicate<DiskImage> ONLY_IMAGES =
            new DiskStorageTypePredicate<>(DiskImage.class);

    /**
     * Filters out all disks that are not Luns.
     */
    public static final DiskStorageTypePredicate<LunDisk> ONLY_LUNS = new DiskStorageTypePredicate<>(LunDisk.class);

    /**
     * Filters out all disks that are not Cinder disks.
     */
    public static final DiskStorageTypePredicate<CinderDisk> ONLY_CINDER =
            new DiskStorageTypePredicate<>(CinderDisk.class);

    /**
     * Filters out all disks that are not managed block storage disks.
     */
    public static final DiskStorageTypePredicate<ManagedBlockStorageDisk> ONLY_MANAGED_BLOCK_STORAGE =
            new DiskStorageTypePredicate<>(ManagedBlockStorageDisk.class);

    /**
     * Filters out all disks that are not snapable (retains only disks that we can take a snapshot of).
     */
    public static final Predicate<Disk> ONLY_SNAPABLE = Disk::isAllowSnapshot;

    /**
     * Filters out all disks that are shareable.
     */
    public static final Predicate<Disk> ONLY_NOT_SHAREABLE = d -> !d.isShareable();

    /**
     * Filters out all disks that are not active.
     */
    public static final Predicate<Disk> ONLY_ACTIVE = d -> Boolean.TRUE.equals(((DiskImage) d).getActive());

    /**
     * Filters out all disks that are not plugged.
     */
    public static final Predicate<Disk> ONLY_PLUGGED = Disk::getPlugged;

    /**
     * Filters out all disk snapshots
     */
    public static final Predicate<Disk> ONLY_DISK_SNAPSHOT = Disk::isDiskSnapshot;

    /**
     * This method filters a list of disks retaining only disk images and continues to filter the list by the
     * specified predicates.
     *
     * @param disks The collection of disks to filter
     * @param predicates The predicates to filter by
     * @return A filtered list of disks
     */
    @SafeVarargs
    public static List<DiskImage> filterImageDisks(Collection<? extends Disk> disks, Predicate<Disk>... predicates) {
        return filterDisksByStorageType(disks, ONLY_IMAGES, predicates);
    }

    /**
     * This method filters a list of disks retaining only lun disks and continues to filter the list by the
     * specified predicates.
     *
     * @param disks The collection of disks to filter
     * @param predicates The predicates to filter by
     * @return A filtered list of disks
     */
    @SafeVarargs
    public static List<LunDisk> filterLunDisks(Collection<? extends Disk> disks, Predicate<Disk>... predicates) {
        return filterDisksByStorageType(disks, ONLY_LUNS, predicates);
    }

    /**
     * This method filters a list of disks retaining only cinder disks and continues to filter the list by the
     * specified predicates.
     *
     * @param disks The collection of disks to filter
     * @param predicates The predicates to filter by
     * @return A filtered list of disks
     */
    @SafeVarargs
    public static List<CinderDisk> filterCinderDisks(Collection<? extends Disk> disks, Predicate<Disk>... predicates) {
        return filterDisksByStorageType(disks, ONLY_CINDER, predicates);
    }

    /**
     * This method filters a list of disks retaining only managed block storage disks and continues to filter the list by the
     * specified predicates.
     *
     * @param disks The collection of disks to filter
     * @param predicates The predicates to filter by
     * @return A filtered list of disks
     */
    @SafeVarargs
    public static List<ManagedBlockStorageDisk> filterManagedBlockStorageDisks(Collection<? extends Disk> disks, Predicate<Disk>... predicates) {
        return filterDisksByStorageType(disks, ONLY_MANAGED_BLOCK_STORAGE, predicates);
    }

    /**
     * This method filters a list of disks retaining only disks of a certain storage type and continues to filter the list by
     * the specified predicates.
     *
     * @param disks The collection of disks to filter
     * @param storageTypePredicate The predicate that defines the storage type to filter by
     * @param predicates The predicates to filter by
     * @return A filtered list of disks
     */
    @SafeVarargs
    private static <T extends Disk> List<T> filterDisksByStorageType
        (Collection<? extends Disk> disks, DiskStorageTypePredicate<T> storageTypePredicate, Predicate<Disk>... predicates) {
        Predicate<Disk> chain = Stream.concat(Stream.of(storageTypePredicate), Arrays.stream(predicates)).reduce(Predicate::and).orElse(p -> true);

        return disks.stream().filter(chain)
                .map(storageTypePredicate.getImplementingDiskType()::cast)
                .collect(Collectors.toList());
    }
}
