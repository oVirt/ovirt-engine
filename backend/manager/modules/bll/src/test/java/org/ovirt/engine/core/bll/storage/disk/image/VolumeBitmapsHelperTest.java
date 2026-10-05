package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Qcow2BitmapInfo;
import org.ovirt.engine.core.common.businessentities.storage.Qcow2BitmapInfoFlags;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * A test case for {@link VolumeBitmapsHelper}.
 * All the VDSM access (host resolution, qemu image info) is mocked away, so only the
 * reconcile/check logic over the reported bitmaps is tested.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VolumeBitmapsHelperTest {

    private static final Guid STORAGE_POOL_ID = Guid.newGuid();
    private static final Guid STORAGE_DOMAIN_ID = Guid.newGuid();
    private static final Guid HOST_ID = Guid.newGuid();

    @Mock
    private ImagesHandler imagesHandler;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private VdsCommandsHelper vdsCommandsHelper;

    @InjectMocks
    private VolumeBitmapsHelper helper = new VolumeBitmapsHelper();

    private DiskImage leafImage;

    @BeforeEach
    public void setUp() {
        leafImage = createQcowDisk(Guid.newGuid());
        doReturn(HOST_ID).when(vdsCommandsHelper)
                .getHostForExecution(eq(STORAGE_POOL_ID), ArgumentMatchers.<Predicate<VDS>>any());
        doReturn(List.of(leafImage)).when(diskImageDao).getAllSnapshotsForLeaf(leafImage.getImageId());
    }

    private DiskImage createQcowDisk(Guid imageId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setImageId(imageId);
        diskImage.setStorageIds(List.of(STORAGE_DOMAIN_ID));
        diskImage.setVolumeFormat(VolumeFormat.COW);
        return diskImage;
    }

    private VmCheckpoint createCheckpoint(Guid id) {
        VmCheckpoint checkpoint = new VmCheckpoint();
        checkpoint.setId(id);
        return checkpoint;
    }

    private Qcow2BitmapInfo createBitmap(String name, Qcow2BitmapInfoFlags... flags) {
        Qcow2BitmapInfo bitmap = new Qcow2BitmapInfo();
        bitmap.setName(name);
        bitmap.setGranularity(65536);
        bitmap.setFlags(Arrays.asList(flags));
        return bitmap;
    }

    private QemuImageInfo createImageInfo(Qcow2BitmapInfo... bitmaps) {
        QemuImageInfo imageInfo = new QemuImageInfo();
        imageInfo.setBitmaps(new ArrayList<>(Arrays.asList(bitmaps)));
        return imageInfo;
    }

    private void mockVolumeBitmaps(DiskImage volume, QemuImageInfo imageInfo) {
        doReturn(imageInfo).when(imagesHandler).getQemuImageInfoFromVdsm(
                eq(STORAGE_POOL_ID),
                eq(STORAGE_DOMAIN_ID),
                eq(volume.getId()),
                eq(volume.getImageId()),
                eq(HOST_ID),
                eq(true));
    }

    /**
     * A raw disk has no bitmaps at all - nothing should be queried and nothing removed.
     */
    @Test
    public void reconcileDiskRawDisk() {
        leafImage.setVolumeFormat(VolumeFormat.RAW);
        Guid checkpointId = Guid.newGuid();

        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(createCheckpoint(checkpointId))),
                (volume, bitmapName) -> { });

        assertTrue(removed.isEmpty(), "no checkpoint should be removed for a raw disk");
        verifyNoInteractions(imagesHandler, diskImageDao, vdsDao, vdsCommandsHelper);
    }

    /**
     * No host in the pool can report bitmaps - the result is unknown, nothing may be touched.
     */
    @Test
    public void reconcileDiskNoCapableHost() {
        doReturn(null).when(vdsCommandsHelper)
                .getHostForExecution(eq(STORAGE_POOL_ID), ArgumentMatchers.<Predicate<VDS>>any());

        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(createCheckpoint(Guid.newGuid()))),
                (volume, bitmapName) -> { });

        assertNull(removed, "unknown host capability should return null");
        verifyNoInteractions(imagesHandler);
    }

    /**
     * The bitmaps of a volume could not be queried - nothing may be touched.
     */
    @Test
    public void reconcileDiskQueryFailed() {
        mockVolumeBitmaps(leafImage, null);

        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(createCheckpoint(Guid.newGuid()))),
                (volume, bitmapName) -> { });

        assertNull(removed, "failed query should return null");
    }

    @Test
    public void reconcileDiskValidCheckpointBitmap() {
        Guid checkpointId = Guid.newGuid();
        mockVolumeBitmaps(leafImage,
                createImageInfo(createBitmap(checkpointId.toString(),
                        Qcow2BitmapInfoFlags.AUTO)));

        List<String> removedBitmaps = new ArrayList<>();
        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(createCheckpoint(checkpointId))),
                (volume, bitmapName) -> removedBitmaps.add(bitmapName));

        assertTrue(removed.isEmpty(), "a valid checkpoint should not be removed");
        assertTrue(removedBitmaps.isEmpty(), "a valid bitmap should not be removed");
    }

    /**
     * A bitmap that belongs to no known checkpoint (e.g. left over by a failed backup)
     * is an orphan and must be removed, but it takes no checkpoint with it.
     */
    @Test
    public void reconcileDiskRemovesOrphanBitmap() {
        Guid checkpointId = Guid.newGuid();
        String orphanName = Guid.newGuid().toString();
        mockVolumeBitmaps(leafImage,
                createImageInfo(
                        createBitmap(checkpointId.toString(), Qcow2BitmapInfoFlags.AUTO),
                        createBitmap(orphanName, Qcow2BitmapInfoFlags.AUTO)));

        List<String> removedBitmaps = new ArrayList<>();
        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(createCheckpoint(checkpointId))),
                (volume, bitmapName) -> removedBitmaps.add(bitmapName));

        assertTrue(removed.isEmpty(), "an orphan bitmap removes no checkpoint");
        assertEquals(List.of(orphanName), removedBitmaps, "the orphan bitmap should be removed");
    }

    /**
     * A checkpoint bitmap that was never flushed by the crashed qemu ('in-use' on an offline
     * image, no 'auto') is inconsistent: the bitmap is removed and the checkpoint with it.
     */
    @Test
    public void reconcileDiskRemovesInconsistentCheckpointBitmap() {
        Guid checkpointId = Guid.newGuid();
        mockVolumeBitmaps(leafImage,
                createImageInfo(createBitmap(checkpointId.toString(), Qcow2BitmapInfoFlags.IN_USE)));

        List<String> removedBitmaps = new ArrayList<>();
        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(createCheckpoint(checkpointId))),
                (volume, bitmapName) -> removedBitmaps.add(bitmapName));

        assertEquals(Set.of(checkpointId), removed, "the checkpoint of the inconsistent bitmap should be removed");
        assertEquals(List.of(checkpointId.toString()), removedBitmaps,
                "the inconsistent bitmap should be removed");
    }

    /**
     * A checkpoint whose bitmap is missing entirely (e.g. a bitmap creation that failed
     * halfway) has no usable state left and must be removed from the database.
     */
    @Test
    public void reconcileDiskRemovesCheckpointWithoutBitmap() {
        Guid existingCheckpointId = Guid.newGuid();
        Guid missingCheckpointId = Guid.newGuid();
        mockVolumeBitmaps(leafImage,
                createImageInfo(createBitmap(existingCheckpointId.toString(), Qcow2BitmapInfoFlags.AUTO)));

        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(
                        createCheckpoint(existingCheckpointId),
                        createCheckpoint(missingCheckpointId))),
                (volume, bitmapName) -> { });

        assertEquals(Set.of(missingCheckpointId), removed,
                "only the checkpoint without a bitmap should be removed");
    }

    /**
     * Every volume of the chain is scanned - the same bitmap name appears on each volume
     * (each snapshot clones the previous bitmaps to the new leaf), so the callback fires
     * once per volume it is removed from.
     */
    @Test
    public void reconcileDiskScansWholeVolumeChain() {
        DiskImage baseVolume = createQcowDisk(Guid.newGuid());
        doReturn(List.of(baseVolume, leafImage)).when(diskImageDao)
                .getAllSnapshotsForLeaf(leafImage.getImageId());

        Guid checkpointId = Guid.newGuid();
        String orphanName = Guid.newGuid().toString();
        // The orphan is inconsistent on the base volume and valid on the leaf: as soon as
        // it is unusable on one volume of the chain it must go from all of them.
        mockVolumeBitmaps(baseVolume, createImageInfo(
                createBitmap(checkpointId.toString(), Qcow2BitmapInfoFlags.AUTO),
                createBitmap(orphanName, Qcow2BitmapInfoFlags.IN_USE)));
        mockVolumeBitmaps(leafImage, createImageInfo(
                createBitmap(checkpointId.toString(), Qcow2BitmapInfoFlags.AUTO),
                createBitmap(orphanName, Qcow2BitmapInfoFlags.AUTO)));

        List<DiskImage> removedFrom = new ArrayList<>();
        List<String> removedNames = new ArrayList<>();
        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(createCheckpoint(checkpointId))),
                (volume, bitmapName) -> {
                    removedFrom.add(volume);
                    removedNames.add(bitmapName);
                });

        assertTrue(removed.isEmpty(), "an orphan bitmap removes no checkpoint");
        assertEquals(2, removedFrom.size(), "the bitmap should be removed from both volumes of the chain");
        assertEquals(baseVolume.getImageId(), removedFrom.get(0).getImageId(),
                "the chain should be scanned base first");
        assertEquals(List.of(orphanName, orphanName), removedNames,
                "the orphan bitmap should be removed from every volume it appears on");
    }

    /**
     * A disk with no snapshots has a single-volume chain - the leaf itself is queried.
     */
    @Test
    public void reconcileDiskSingleVolumeChain() {
        doReturn(Collections.emptyList()).when(diskImageDao).getAllSnapshotsForLeaf(leafImage.getImageId());
        Guid checkpointId = Guid.newGuid();
        mockVolumeBitmaps(leafImage,
                createImageInfo(createBitmap(checkpointId.toString(), Qcow2BitmapInfoFlags.AUTO)));

        Set<Guid> removed = helper.reconcileDisk(
                STORAGE_POOL_ID,
                leafImage,
                new HashSet<>(List.of(createCheckpoint(checkpointId))),
                (volume, bitmapName) -> { });

        assertTrue(removed.isEmpty(), "a valid checkpoint should not be removed");
        verify(imagesHandler).getQemuImageInfoFromVdsm(
                eq(STORAGE_POOL_ID),
                eq(STORAGE_DOMAIN_ID),
                eq(leafImage.getId()),
                eq(leafImage.getImageId()),
                eq(HOST_ID),
                eq(true));
    }

    @Test
    public void getVolumeBitmapsReturnsBitmaps() {
        Qcow2BitmapInfo bitmap = createBitmap(Guid.newGuid().toString(), Qcow2BitmapInfoFlags.AUTO);
        mockVolumeBitmaps(leafImage, createImageInfo(bitmap));

        List<Qcow2BitmapInfo> bitmaps = helper.getVolumeBitmaps(
                STORAGE_POOL_ID, leafImage, HOST_ID, true);

        assertEquals(List.of(bitmap), bitmaps);
    }

    @Test
    public void getVolumeBitmapsQueryFailed() {
        mockVolumeBitmaps(leafImage, null);

        List<Qcow2BitmapInfo> bitmaps = helper.getVolumeBitmaps(
                STORAGE_POOL_ID, leafImage, HOST_ID, true);

        assertNull(bitmaps, "a failed query should return null and not an empty list");
    }

    /**
     * A volume with no bitmaps at all is a valid query result - an empty list, not null.
     */
    @Test
    public void getVolumeBitmapsNoBitmaps() {
        mockVolumeBitmaps(leafImage, createImageInfo());

        List<Qcow2BitmapInfo> bitmaps = helper.getVolumeBitmaps(
                STORAGE_POOL_ID, leafImage, HOST_ID, true);

        assertTrue(bitmaps.isEmpty(), "a volume without bitmaps is a valid empty result");
    }

    /**
     * The specific host passed to getVolumeBitmaps is used as-is; the pool-wide host
     * resolution is only part of reconcileDisk.
     */
    @Test
    public void getVolumeBitmapsUsesGivenHost() {
        Guid otherHost = Guid.newGuid();
        doReturn(createImageInfo()).when(imagesHandler).getQemuImageInfoFromVdsm(
                eq(STORAGE_POOL_ID),
                eq(STORAGE_DOMAIN_ID),
                eq(leafImage.getId()),
                eq(leafImage.getImageId()),
                eq(otherHost),
                eq(true));

        helper.getVolumeBitmaps(STORAGE_POOL_ID, leafImage, otherHost, true);

        verify(imagesHandler).getQemuImageInfoFromVdsm(
                eq(STORAGE_POOL_ID),
                eq(STORAGE_DOMAIN_ID),
                eq(leafImage.getId()),
                eq(leafImage.getImageId()),
                eq(otherHost),
                eq(true));
        verifyNoInteractions(vdsCommandsHelper, vdsDao);
    }
}
