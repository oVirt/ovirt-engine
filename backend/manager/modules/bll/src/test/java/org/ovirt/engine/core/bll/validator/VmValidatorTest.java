package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class VmValidatorTest {

    private VmValidator validator;

    private VM vm;

    @Before
    public void setUp() {
        vm = new VM();
        validator = new VmValidator(vm);
    }

    @Test
    public void canDisableVirtioScsiSuccess() {
        Disk disk = new DiskImage();
        disk.setDiskInterface(DiskInterface.VirtIO);

        assertThat(validator.canDisableVirtioScsi(Collections.singletonList(disk)), isValid());
    }

    @Test
    public void canDisableVirtioScsiFail() {
        Disk disk = new DiskImage();
        disk.setDiskInterface(DiskInterface.VirtIO_SCSI);

        assertThat(validator.canDisableVirtioScsi(Collections.singletonList(disk)),
                failsWith(VdcBllMessages.CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS));
    }
}
