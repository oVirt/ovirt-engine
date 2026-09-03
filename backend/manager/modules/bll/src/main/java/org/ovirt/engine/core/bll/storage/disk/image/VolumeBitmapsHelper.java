package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Qcow2BitmapInfo;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tells whether the qcow2 bitmap backing a checkpoint can still be used.
 * <p>
 * The engine tracks checkpoints in its database, but the bitmaps backing them live inside the qcow2 volumes, and the
 * two can drift apart without the engine noticing - a bitmap can be missing entirely, or be left marked
 * {@code in-use} because the qemu process that had it open died before flushing it. Every operation that has to open
 * the image then fails: incremental backup, NBD export, and disk resize both online (libvirt {@code block_resize})
 * and offline ({@code qemu-img resize}).
 * <p>
 * There are two sources of truth and which one applies depends on whether a qemu process has the image open:
 * <ul>
 * <li><b>VM down</b> - the qcow2 header, read through {@code getQemuImageInfo}. {@code in-use} on an offline image
 * means the bitmap was never flushed.</li>
 * <li><b>VM up</b> - libvirt, through {@code checkpointCreateXML(REDEFINE | REDEFINE_VALIDATE)}. The header cannot be
 * used here because {@code in-use} is also set for a perfectly healthy, actively recording bitmap. Only qemu's
 * in-memory view knows the bitmap was loaded as inconsistent, and this is the supported way to ask for it.</li>
 * </ul>
 * Presence of a bitmap, and the flags of any volume below the active one, are meaningful in both cases.
 */
@Singleton
public class VolumeBitmapsHelper {

    private static final Logger log = LoggerFactory.getLogger(VolumeBitmapsHelper.class);

    public enum BitmapState {
        /** The bitmap exists and is consistent in the whole volume chain. */
        VALID,
        /** The bitmap is missing or inconsistent, it cannot be used and should be cleaned up. */
        INVALID,
        /** The bitmaps could not be queried, so nothing can be concluded. Never treat this as a failure. */
        UNKNOWN
    }

    @Inject
    private ImagesHandler imagesHandler;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    /**
     * @return the host to query, or {@code null} when no host in the pool can report qcow2 bitmaps, in which case
     *         every check in this class returns {@link BitmapState#UNKNOWN}.
     */
    private Guid resolveHost(Guid storagePoolId, Guid vdsId) {
        if (vdsId == null) {
            vdsId = vdsCommandsHelper.getHostForExecution(storagePoolId, VDS::isQemuImageInfoBitmaps);
            return vdsId;
        }
        VDS vds = vdsDao.get(vdsId);
        return vds != null && vds.isQemuImageInfoBitmaps() ? vdsId : null;
    }

    /**
     * Makes one disk of a down VM consistent with the checkpoints the engine knows: for every volume of the chain
     * the on-disk bitmaps are listed and compared against the engine's checkpoints.
     * <ul>
     * <li>a bitmap that belongs to no checkpoint (an orphan, e.g. from a failed backup) is removed</li>
     * <li>a checkpoint bitmap that is inconsistent (never flushed by the crashed qemu) is removed, and the
     * checkpoint is removed from the database</li>
     * <li>a checkpoint whose bitmap is missing entirely is removed from the database</li>
     * </ul>
     * The removed checkpoints are the ones the chain keeps no usable state for, so the chain that is left behind
     * is fully usable for an incremental backup.
     *
     * @param removedBitmap callback invoked for every bitmap removed from a volume, for auditing
     * @return the checkpoint ids removed from the database, or {@code null} when the bitmaps could not be queried
     *         (nothing was touched in that case)
     */
    public Set<Guid> reconcileDisk(Guid storagePoolId,
            DiskImage leafImage,
            Set<VmCheckpoint> knownCheckpoints,
            BiConsumer<DiskImage, String> removedBitmap) {
        if (!leafImage.isQcowFormat()) {
            return Collections.emptySet();
        }
        Guid host = resolveHost(storagePoolId, null);
        if (host == null) {
            return null;
        }

        // Map the checkpoints to the bitmap names they own, and scan every volume of the chain once.
        Set<String> checkpointBitmapNames = knownCheckpoints.stream()
                .map(checkpoint -> checkpoint.getId().toString())
                .collect(Collectors.toSet());
        Set<String> seenBitmapNames = new HashSet<>();
        Set<String> removedBitmapNames = new HashSet<>();

        for (DiskImage volume : getVolumeChain(leafImage)) {
            List<Qcow2BitmapInfo> bitmaps = getVolumeBitmaps(storagePoolId, volume, host, true);
            if (bitmaps == null) {
                return null;
            }
            for (Qcow2BitmapInfo bitmap : bitmaps) {
                seenBitmapNames.add(bitmap.getName());
                boolean ownedByCheckpoint = checkpointBitmapNames.contains(bitmap.getName());
                if (!ownedByCheckpoint || !bitmap.isValid()) {
                    // An orphan bitmap, or a checkpoint bitmap that was never flushed - both
                    // are unusable, and neither can ever be repaired.
                    if (!removedBitmapNames.contains(bitmap.getName())) {
                        log.warn("Removing {} bitmap '{}' from disk '{}' volume '{}'",
                                ownedByCheckpoint ? "inconsistent" : "orphan",
                                bitmap.getName(),
                                volume.getId(),
                                volume.getImageId());
                    }
                    removedBitmapNames.add(bitmap.getName());
                    if (removedBitmap != null) {
                        removedBitmap.accept(volume, bitmap.getName());
                    }
                }
            }
        }

        // Every checkpoint whose bitmap is gone (removed above, or never created - e.g. a bitmap
        // creation that failed halfway) cannot be used anymore: remove it from the database.
        Set<Guid> removedCheckpoints = knownCheckpoints.stream()
                .filter(checkpoint -> !seenBitmapNames.contains(checkpoint.getId().toString()))
                .map(VmCheckpoint::getId)
                .collect(Collectors.toSet());
        return removedCheckpoints;
    }

    /**
     * @return the bitmaps of a single volume, or {@code null} if they could not be queried
     */
    public List<Qcow2BitmapInfo> getVolumeBitmaps(Guid storagePoolId,
            DiskImage volume,
            Guid vdsId,
            boolean prepareAndTeardown) {
        QemuImageInfo imageInfo = imagesHandler.getQemuImageInfoFromVdsm(storagePoolId,
                volume.getStorageIds().get(0),
                volume.getId(),
                volume.getImageId(),
                vdsId,
                prepareAndTeardown);

        if (imageInfo == null) {
            // getQemuImageInfoFromVdsm swallows the error and returns null, so we cannot tell a volume without
            // bitmaps from a failed query. Reporting it as unknown avoids destroying checkpoints on a transient error.
            log.warn("Failed to query the bitmaps of disk '{}' volume '{}'", volume.getId(), volume.getImageId());
            return null;
        }
        return imageInfo.getBitmaps();
    }

    private List<DiskImage> getVolumeChain(DiskImage leafImage) {
        List<DiskImage> chain = diskImageDao.getAllSnapshotsForLeaf(leafImage.getImageId())
                .stream()
                .filter(DiskImage::isQcowFormat)
                .collect(Collectors.toList());
        return chain.isEmpty() ? List.of(leafImage) : chain;
    }
}
