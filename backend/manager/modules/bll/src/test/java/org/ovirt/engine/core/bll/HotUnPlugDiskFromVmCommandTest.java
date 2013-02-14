package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class HotUnPlugDiskFromVmCommandTest extends HotPlugDiskToVmCommandTest {

    @Override
    @Test
    public void canDoActionFailedWrongPlugStatus() throws Exception {
        mockVmStatusUp();
        cretaeDiskWrongPlug(false);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.HOT_UNPLUG_DISK_IS_NOT_PLUGGED.toString()));
    }

    @Override
    protected HotUnPlugDiskFromVmCommand<HotPlugDiskToVmParameters> createCommand() {
        return new HotUnPlugDiskFromVmCommand<HotPlugDiskToVmParameters>(createParameters());
    }

    @Override
    protected VdcActionType getCommandActionType() {
        return VdcActionType.HotUnPlugDiskFromVm;
    }

    @Override
    protected void cretaeVirtIODisk() {
        DiskImage disk = new DiskImage();
        disk.setImageId(diskImageGuid);
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setActive(true);
        doReturn(diskDao).when(command).getDiskDao();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        mockVmDevice(true);
    }
}
