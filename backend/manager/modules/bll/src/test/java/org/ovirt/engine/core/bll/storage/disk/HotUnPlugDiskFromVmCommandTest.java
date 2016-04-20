package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Test;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;

public class HotUnPlugDiskFromVmCommandTest extends HotPlugDiskToVmCommandTest {

    @Override
    @Test
    public void validateFailedWrongPlugStatus() throws Exception {
        mockVmStatusUp();
        mockInterfaceList();
        createDiskWrongPlug(false);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.HOT_UNPLUG_DISK_IS_NOT_PLUGGED);
    }

    @Override
    protected HotUnPlugDiskFromVmCommand<HotPlugDiskToVmParameters> createCommand() {
        return new HotUnPlugDiskFromVmCommand<>(createParameters(), null);
    }

    @Override
    protected VdcActionType getCommandActionType() {
        return VdcActionType.HotUnPlugDiskFromVm;
    }

    @Override
    protected void createVirtIODisk() {
        DiskImage disk = getDiskImage();
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setActive(true);
        doReturn(diskDao).when(command).getDiskDao();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        when(osRepository.getDiskHotpluggableInterfaces(any(Integer.class),
                any(Version.class))).thenReturn(new HashSet<>(DISK_HOTPLUGGABLE_INTERFACES));
        mockVmDevice(true);
    }
}
