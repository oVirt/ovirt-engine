package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

@ExtendWith({MockitoExtension.class, RandomUtilsSeedingExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiskImagesValidatorTest {
    private DiskImage disk1;
    private DiskImage disk2;
    private DiskImagesValidator validator;

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @BeforeEach
    public void setUp() {
        disk1 = createDisk();
        disk1.setDiskAlias("disk1");
        disk1.setStoragePoolId(Guid.newGuid());
        disk2 = createDisk();
        disk2.setDiskAlias("disk2");
        disk2.setStoragePoolId(Guid.newGuid());
        validator = spy(new DiskImagesValidator(Arrays.asList(disk1, disk2)));
        doReturn(vmDao).when(validator).getVmDao();
        doReturn(vmDeviceDao).when(validator).getVmDeviceDao();
        doReturn(snapshotDao).when(validator).getSnapshotDao();
        doReturn(diskImageDao).when(validator).getDiskImageDao();
        doReturn(storagePoolDao).when(validator).getStoragePoolDao();
    }

    private static DiskImage createDisk() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setDiskAlias(RandomUtils.instance().nextString(10));
        disk.setActive(true);
        disk.setImageStatus(ImageStatus.OK);
        ArrayList<Guid> storageDomainIds = new ArrayList<>();
        storageDomainIds.add(Guid.newGuid());
        disk.setStorageIds(storageDomainIds);
        return disk;
    }

    private static String createAliasReplacements(DiskImage... disks) {
        return Arrays.stream(disks).map(DiskImage::getDiskAlias).collect(Collectors.joining(", ", "$diskAliases ", ""));
    }

    @Test
    public void diskImagesNotIllegalBothOK() {
        assertThat("Neither disk is illegal", validator.diskImagesNotIllegal(), isValid());
    }

    @Test
    public void diskImagesNotIllegalFirstIllegal() {
        disk1.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements(hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesNotIllegalSecondtIllegal() {
        disk2.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements(hasItem(createAliasReplacements(disk2)))));
    }

    @Test
    public void diskImagesNotIllegalBothIllegal() {
        disk1.setImageStatus(ImageStatus.ILLEGAL);
        disk2.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }

    @Test
    public void diskImagesAlreadyExistBothExist() {
        doReturn(new DiskImage()).when(validator).getExistingDisk(any());
        assertThat(validator.diskImagesAlreadyExist(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }

    /**
     * Test a case when the two validated disks exists and have a null disk alias, in that case the disk aliases in
     * the CDA message should be taken from the disks existing on the setup
     */
    @Test
    public void diskImagesAlreadyDiskInImportWithNullAlias() {
        disk1.setDiskAlias(null);
        disk2.setDiskAlias(null);
        DiskImage existingImage1 = new DiskImage();
        existingImage1.setDiskAlias("existingDiskAlias1");
        DiskImage existingImage2 = new DiskImage();
        existingImage2.setDiskAlias("existingDiskAlias2");

        doReturn(existingImage1).when(validator).getExistingDisk(disk1.getId());
        doReturn(existingImage2).when(validator).getExistingDisk(disk2.getId());
        assertThat(validator.diskImagesAlreadyExist(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST)).and(replacements
                        (hasItem(createAliasReplacements(existingImage1, existingImage2)))));
    }


    @Test
    public void diskImagesAlreadyExistOneExist() {
        doReturn(new DiskImage()).when(validator).getExistingDisk(disk1.getId());
        doReturn(null).when(validator).getExistingDisk(disk2.getId());
        assertThat(validator.diskImagesAlreadyExist(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST)).and(replacements
                        (hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesAlreadyExistBothDoesntExist() {
        doReturn(null).when(validator).getExistingDisk(any());
        assertThat(validator.diskImagesAlreadyExist(), isValid());
    }

    @Test
    public void diskImagesNotLockedBothOK() {
        assertThat("Neither disk is Locked", validator.diskImagesNotLocked(), isValid());
    }

    @Test
    public void diskImagesNotLockedFirstLocked() {
        disk1.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements(hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesNotLockedSecondtLocked() {
        disk2.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements(hasItem(createAliasReplacements(disk2)))));
    }

    @Test
    public void diskImagesNotLockedBothLocked() {
        disk1.setImageStatus(ImageStatus.LOCKED);
        disk2.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }

    private List<VmDevice> prepareForCheckingIfDisksSnapshotsAttachedToOtherVms() {
        VmDevice device1 = createVmDeviceForDisk(disk1);
        VmDevice device2 = createVmDeviceForDisk(disk2);
        when(vmDeviceDao.getVmDevicesByDeviceId(disk1.getId(), null)).thenReturn(Collections.singletonList(device1));
        when(vmDeviceDao.getVmDevicesByDeviceId(disk2.getId(), null)).thenReturn(Collections.singletonList(device2));
        when(vmDao.get(any())).thenReturn(new VM());
        when(snapshotDao.get(any())).thenReturn(new Snapshot());
        return Arrays.asList(device1, device2);
    }

    @Test
    public void diskImagesHasDerivedDisksNoStorageDomainSpecifiedSuccess() {
        disk1.setVmEntityType(VmEntityType.TEMPLATE);
        assertThat(validator.diskImagesHaveNoDerivedDisks(null),
                isValid());
    }

    @Test
    public void diskImagesHasDerivedDisksNoStorageDomainSpecifiedFailure() {
        disk1.setVmEntityType(VmEntityType.TEMPLATE);
        when(diskImageDao.getAllSnapshotsForParent(disk1.getImageId())).thenReturn(Collections.singletonList(disk2));
        assertThat(validator.diskImagesHaveNoDerivedDisks(null),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DETECTED_DERIVED_DISKS));
    }

    @Test
    public void diskImagesHasDerivedDisksOnStorageDomain() {
        Guid storageDomainId = Guid.Empty;
        disk1.setVmEntityType(VmEntityType.TEMPLATE);
        ArrayList<Guid> storageDomainIds = new ArrayList<>();
        storageDomainIds.add(storageDomainId);
        disk2.setStorageIds(storageDomainIds);
        when(diskImageDao.getAllSnapshotsForParent(disk1.getImageId())).thenReturn(Collections.singletonList(disk2));
        assertThat(validator.diskImagesHaveNoDerivedDisks(storageDomainId),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DETECTED_DERIVED_DISKS));
    }

    @Test
    public void diskImagesNoDerivedDisksOnStorageDomain() {
        disk1.setVmEntityType(VmEntityType.TEMPLATE);
        ArrayList<Guid> storageDomainIds = new ArrayList<>();
        storageDomainIds.add(Guid.newGuid());
        disk2.setStorageIds(storageDomainIds);
        when(diskImageDao.getAllSnapshotsForParent(disk1.getImageId())).thenReturn(Collections.singletonList(disk2));
        assertThat(validator.diskImagesHaveNoDerivedDisks(Guid.Empty), isValid());
    }

    @Test
    public void diskImagesSnapshotsNotAttachedToOtherVmsOneDiskSnapshotAttached() {
        List<VmDevice> createdDevices = prepareForCheckingIfDisksSnapshotsAttachedToOtherVms();
        createdDevices.get(1).setSnapshotId(Guid.newGuid());
        assertThat(validator.diskImagesSnapshotsNotAttachedToOtherVms(false),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_IS_ATTACHED_TO_ANOTHER_VM));
        verify(snapshotDao, times(1)).get(createdDevices.get(1).getSnapshotId());
        verify(snapshotDao, never()).get(createdDevices.get(0).getSnapshotId());
    }

    @Test
    public void diskImagesSnapshotsNotAttachedToOtherVmsNoDiskSnapshotsAttached() {
        List<VmDevice> createdDevices = prepareForCheckingIfDisksSnapshotsAttachedToOtherVms();
        assertThat(validator.diskImagesSnapshotsNotAttachedToOtherVms(false), isValid());
        verify(snapshotDao, never()).get(createdDevices.get(1).getSnapshotId());
        verify(snapshotDao, never()).get(createdDevices.get(0).getSnapshotId());
    }

    @Test
    public void testIsQcowV3SupportedForDcVersionV4() {
        disk1.setVolumeFormat(VolumeFormat.COW);
        disk1.setQcowCompat(QcowCompat.QCOW2_V3);
        StoragePool sp = new StoragePool();
        sp.setStoragePoolFormatType(StorageFormatType.V4);
        when(storagePoolDao.get(any())).thenReturn(sp);
        assertThat(validator.isQcowVersionSupportedForDcVersion(), isValid());
    }

    @Test
    public void testIsQcowV2SupportedForDcVersionV4() {
        disk1.setVolumeFormat(VolumeFormat.COW);
        disk1.setQcowCompat(QcowCompat.QCOW2_V2);
        StoragePool sp = new StoragePool();
        sp.setStoragePoolFormatType(StorageFormatType.V4);
        when(storagePoolDao.get(any())).thenReturn(sp);
        assertThat(validator.isQcowVersionSupportedForDcVersion(), isValid());
    }

    @Test
    public void testIsQcowV3SupportedForDcVersionV3() {
        disk1.setVolumeFormat(VolumeFormat.COW);
        disk1.setQcowCompat(QcowCompat.QCOW2_V3);
        StoragePool sp = new StoragePool();
        sp.setStoragePoolFormatType(StorageFormatType.V3);
        when(storagePoolDao.get(any())).thenReturn(sp);
        assertThat(validator.isQcowVersionSupportedForDcVersion(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_QCOW_COMPAT_DOES_NOT_MATCH_DC_VERSION));
    }

    @Test
    public void testIsQcowV2SupportedForDcVersionV3() {
        disk1.setVolumeFormat(VolumeFormat.COW);
        disk1.setQcowCompat(QcowCompat.QCOW2_V2);
        StoragePool sp = new StoragePool();
        sp.setStoragePoolFormatType(StorageFormatType.V3);
        when(storagePoolDao.get(any())).thenReturn(sp);
        assertThat(validator.isQcowVersionSupportedForDcVersion(), isValid());
    }

    @Test
    public void testSnapshotAlreadyExists() {
        when(diskImageDao.getAllSnapshotsForImageGroup(disk1.getId())).thenReturn(Collections.singletonList(disk2));
        Map<Guid, DiskImage> diskImagesMap = Collections.singletonMap(disk2.getId(), disk2);
        assertThat(validator.snapshotAlreadyExists(diskImagesMap),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_IMAGE_ALREADY_EXISTS));
    }

    @Test
    public void testIncrementalBackupNotEnabledForAllDisks() {
        disk1.setBackup(DiskBackup.None);
        assertThat(validator.incrementalBackupEnabled(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_DISABLED_FOR_DISKS));
    }

    private VmDevice createVmDeviceForDisk(DiskImage disk) {
        VmDevice device = new VmDevice();
        device.setId(new VmDeviceId(null, disk.getId()));
        return device;
    }
}
