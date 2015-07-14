package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;

@RunWith(MockitoJUnitRunner.class)
public class DiskSnapshotsValidatorTest {
    private DiskImage disk1;
    private DiskImage disk2;
    private DiskSnapshotsValidator validator;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private SnapshotDao snapshotDao;

    @Before
    public void setUp() {
        disk1 = createDisk();
        disk1.setDiskAlias("disk1");
        disk2 = createDisk();
        disk2.setDiskAlias("disk2");
        validator = spy(new DiskSnapshotsValidator(Arrays.asList(disk1, disk2)));

        doReturn(diskImageDao).when(validator).getDiskImageDao();
        doReturn(snapshotDao).when(validator).getSnapshotDao();
    }

    private static DiskImage createDisk() {
        DiskImage disk = new DiskImage();
        disk.setImageId(Guid.newGuid());
        disk.setActive(true);
        disk.setImageStatus(ImageStatus.OK);
        ArrayList<Guid> storageDomainIds = new ArrayList<>();
        storageDomainIds.add(Guid.newGuid());
        disk.setStorageIds(storageDomainIds);
        return disk;
    }

    @Test
    public void diskSnapshotsExist() {
        assertEquals(validator.diskSnapshotsNotExist(Arrays.asList(disk1.getImageId(), disk2.getImageId())),
                ValidationResult.VALID);
    }

    @Test
    public void diskSnapshotsDontExist() {
        assertThat(validator.diskSnapshotsNotExist(Arrays.asList(Guid.newGuid())),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SNAPSHOTS_NOT_EXIST));
    }

    @Test
    public void diskImagesBelongToSameImageGroup() {
        Guid imageGroupId = Guid.newGuid();
        disk1.setId(imageGroupId);
        disk2.setId(imageGroupId);

        assertThat(validator.diskImagesBelongToSameImageGroup(), isValid());
    }

    @Test
    public void diskImagesDontBelongToSameImageGroup() {
        disk1.setId(Guid.newGuid());
        disk2.setId(Guid.newGuid());

        assertThat(validator.diskImagesBelongToSameImageGroup(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_SNAPSHOTS_DONT_BELONG_TO_SAME_DISK));
    }

    @Test
    public void allImagesAreSnapshots() {
        disk1.setActive(false);
        disk2.setActive(false);

        assertThat(validator.imagesAreSnapshots(), isValid());
    }

    @Test
    public void notAllImagesAreSnapshots() {
        disk1.setActive(true);
        disk2.setActive(false);

        assertThat(validator.imagesAreSnapshots(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SNAPSHOTS_ACTIVE));
    }

    @Test
    public void diskSnapshotsCanBePreviewed() {
        Snapshot activeSnapshot = getActiveSnapshot();
        when(snapshotDao.get(any(Guid.class))).thenReturn(activeSnapshot);

        when(diskImageDao.get(disk1.getImageId())).thenReturn(null);
        when(diskImageDao.get(disk2.getImageId())).thenReturn(null);

        assertThat(validator.canDiskSnapshotsBePreviewed(activeSnapshot.getId()), isValid());
    }

    @Test
    public void diskSnapshotsCannotBePreviewed() {
        Snapshot activeSnapshot = getActiveSnapshot();
        when(snapshotDao.get(any(Guid.class))).thenReturn(activeSnapshot);

        when(diskImageDao.get(disk1.getImageId())).thenReturn(disk1);
        when(diskImageDao.get(disk2.getImageId())).thenReturn(disk2);

        assertThat(validator.canDiskSnapshotsBePreviewed(activeSnapshot.getId()),
                failsWith(EngineMessage.CANNOT_PREVIEW_ACTIVE_SNAPSHOT));
    }

    private Snapshot getActiveSnapshot() {
        Snapshot snapshot = new Snapshot();
        snapshot.setId(Guid.newGuid());
        snapshot.setType(Snapshot.SnapshotType.ACTIVE);

        return snapshot;
    }
}
