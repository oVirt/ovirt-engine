package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import java.util.Arrays;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

public class DiskImagesValidatorTest {
    @ClassRule
    public static RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    private DiskImage disk1;
    private DiskImage disk2;
    private DiskImagesValidator validator;

    @Before
    public void setUp() {
        disk1 = createDisk();
        disk1.setDiskAlias("disk1");
        disk2 = createDisk();
        disk2.setDiskAlias("disk2");
        validator = spy(new DiskImagesValidator(Arrays.asList(disk1, disk2)));
    }

    private static DiskImage createDisk() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.NewGuid());
        disk.setDiskAlias(RandomUtils.instance().nextString(10));
        disk.setActive(true);
        disk.setImageStatus(ImageStatus.OK);

        return disk;
    }

    private static String createAliasReplacements(DiskImage... disks) {
        // Take the first alias
        StringBuilder msg = new StringBuilder("$diskAliases ").append(disks[0].getDiskAlias());

        // And and the rest
        for (int i = 1; i < disks.length; ++i) {
            msg.append(", ").append(disks[i].getDiskAlias());
        }

        return msg.toString();
    }

    @Test
    public void diskImagesNotIllegalBothOK() {
        assertThat("Neither disk is illegal", validator.diskImagesNotIllegal(), isValid());
    }

    @Test
    public void diskImagesNotIllegalFirstIllegal() {
        disk1.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements(hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesNotIllegalSecondtIllegal() {
        disk2.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements(hasItem(createAliasReplacements(disk2)))));
    }

    @Test
    public void diskImagesNotIllegalBothIllegal() {
        disk1.setImageStatus(ImageStatus.ILLEGAL);
        disk2.setImageStatus(ImageStatus.ILLEGAL);
        assertThat(validator.diskImagesNotIllegal(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }

    @Test
    public void diskImagesAlreadyExistBothExist() {
        doReturn(true).when(validator).isDiskExists(any(Guid.class));
        assertThat(validator.diskImagesAlreadyExist(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }

    @Test
    public void diskImagesAlreadyExistOneExist() {
        doReturn(true).when(validator).isDiskExists(disk1.getId());
        doReturn(false).when(validator).isDiskExists(disk2.getId());
        assertThat(validator.diskImagesAlreadyExist(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST)).and(replacements
                        (hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesAlreadyExistBothDoesntExist() {
        doReturn(false).when(validator).isDiskExists(any(Guid.class));
        assertEquals(validator.diskImagesAlreadyExist(), ValidationResult.VALID);
    }

    @Test
    public void diskImagesNotLockedBothOK() {
        assertThat("Neither disk is Locked", validator.diskImagesNotLocked(), isValid());
    }

    @Test
    public void diskImagesNotLockedFirstLocked() {
        disk1.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements(hasItem(createAliasReplacements(disk1)))));
    }

    @Test
    public void diskImagesNotLockedSecondtLocked() {
        disk2.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements(hasItem(createAliasReplacements(disk2)))));
    }

    @Test
    public void diskImagesNotLockedBothLocked() {
        disk1.setImageStatus(ImageStatus.LOCKED);
        disk2.setImageStatus(ImageStatus.LOCKED);
        assertThat(validator.diskImagesNotLocked(),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED)).and(replacements
                        (hasItem(createAliasReplacements(disk1, disk2)))));
    }
}
