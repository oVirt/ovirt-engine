package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class HotUnPlugDiskFromVmCommandTest extends HotPlugDiskToVmCommandTest {

    @Test
    public void canDoActionFailedWrongPlugStatus() throws Exception {
        initializeCommand();
        mockVmStatusUp();
        cretaeDiskWrongPlug(false);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.HOT_UNPLUG_DISK_IS_NOT_PLUGGED.toString()));
    }

    @Override
    protected void initializeCommand() {
        command = spy(new HotUnPlugDiskFromVmCommand<HotPlugDiskToVmParameters>(createParameters()));
        mockVds();
        when(command.getActionType()).thenReturn(VdcActionType.HotUnPlugDiskFromVm);
    }

    protected void cretaeVirtIODisk() {
        DiskImage disk = new DiskImage();
        disk.setImageId(diskImageGuid);
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setactive(true);
        disk.setvm_guid(vmId);
        doReturn(diskImageDao).when(command).getDiskDao();
        when(diskImageDao.get(diskImageGuid)).thenReturn(disk);
        mockVmDevice(true);
    }
}
