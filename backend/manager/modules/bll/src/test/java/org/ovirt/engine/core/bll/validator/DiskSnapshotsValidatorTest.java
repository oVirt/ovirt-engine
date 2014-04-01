package org.ovirt.engine.core.bll.validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAO;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

@RunWith(MockitoJUnitRunner.class)
public class DiskSnapshotsValidatorTest {
    private DiskImage disk1;
    private DiskImage disk2;
    private DiskSnapshotsValidator validator;

    @Mock
    private DiskImageDAO diskImageDao;

    @Before
    public void setUp() {
        disk1 = createDisk();
        disk1.setDiskAlias("disk1");
        disk2 = createDisk();
        disk2.setDiskAlias("disk2");
        validator = spy(new DiskSnapshotsValidator(Arrays.asList(disk1, disk2)));
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
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SNAPSHOTS_NOT_EXIST)));
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_SNAPSHOTS_DONT_BELONG_TO_SAME_DISK));
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SNAPSHOTS_ACTIVE));
    }
}
