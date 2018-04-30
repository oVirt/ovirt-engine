package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;

@ExtendWith(MockitoExtension.class)
public class DiskExistenceValidatorTest {

    private Guid disk1;
    private Guid disk2;

    @Mock
    private BaseDiskDao baseDiskDao;

    @InjectMocks
    private DiskExistenceValidator diskExistenceValidator =
            createDiskExistenceValidator();

    private DiskExistenceValidator createDiskExistenceValidator() {
        disk1 = Guid.newGuid();
        disk2 = Guid.newGuid();
        return new DiskExistenceValidator(Arrays.asList(disk1, disk2));
    }

    @Test
    public void diskImagesExist() {
        doReturn(true).when(baseDiskDao).exists(disk1);
        doReturn(true).when(baseDiskDao).exists(disk2);
        assertThat(diskExistenceValidator.disksNotExist(), isValid());
    }

    @Test
    public void bothDiskImagesDontExist() {
        doReturn(false).when(baseDiskDao).exists(disk1);
        doReturn(false).when(baseDiskDao).exists(disk2);
        assertThat(diskExistenceValidator.disksNotExist(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST,
                        "$diskIds " + disk1.toString() + ", " + disk2.toString()));
    }

    @Test
    public void oneDiskImagesDoesntExist() {
        doReturn(false).when(baseDiskDao).exists(disk1);
        doReturn(true).when(baseDiskDao).exists(disk2);
        assertThat(diskExistenceValidator.disksNotExist(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST, "$diskIds " + disk1.toString()));
    }
}
