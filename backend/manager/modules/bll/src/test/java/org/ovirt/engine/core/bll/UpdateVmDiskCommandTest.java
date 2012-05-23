package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ VmHandler.class, Config.class })
public class UpdateVmDiskCommandTest {

    private final Guid diskImageGuid = Guid.NewGuid();
    private final Guid vmId = Guid.NewGuid();

    @Mock
    private VmDAO vmDAO;
    @Mock
    private VdsDAO vdsDao;
    @Mock
    private DiskDao diskDao;

    /**
     * The command under test.
     */
    protected UpdateVmDiskCommand<UpdateVmDiskParameters> command;

    @Before
    public void setUp() {
        mockStatic(Config.class);
        when(Config.GetValue(ConfigValues.HotPlugSupportedOsList)).
                thenReturn("Windows2008,Windows2008x64,Windows2008R2x64,RHEL5,RHEL5x64,RHEL6,RHEL6x64");
        mockStatic(VmHandler.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void canDoActionFailedVMNotFound() throws Exception {
        initializeCommand();
        mockNullVm();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND.toString()));
    }

    @Test
    public void canDoActionFailedVMHasNotDisk() throws Exception {
        initializeCommand();
        mockVmStatusUp();
        createNullDisk();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    protected void initializeCommand() {
        command = spy(new UpdateVmDiskCommand<UpdateVmDiskParameters>(createParameters()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected DiskDao getDiskDao() {
                return diskDao;
            }
        });
        mockVds();
    }

    private void mockNullVm() {
        AuditLogableBaseMockUtils.mockVmDao(command, vmDAO);
        mockGetForDisk(null);
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(null);
    }

    /**
     * Mock a VM in status Up
     */
    protected VM mockVmStatusUp() {
        VM vm = new VM();
        vm.setstatus(VMStatus.Down);
        vm.setguest_os("rhel6");
        AuditLogableBaseMockUtils.mockVmDao(command, vmDAO);
        mockGetForDisk(new VM());
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(vm);
        return vm;
    }

    private void mockGetForDisk(VM vm) {
        List<VM> vms = new ArrayList<VM>();
        vms.add(vm);
        Map<Boolean, List<VM>> vmsMap = new HashMap<Boolean, List<VM>>();
        vmsMap.put(Boolean.TRUE, vms);
        when(vmDAO.getForDisk(diskImageGuid)).thenReturn(vmsMap);
    }

    /**
     * Mock VDS
     */
    protected void mockVds() {
        VDS vds = new VDS();
        vds.setvds_group_compatibility_version(new Version("3.1"));
        command.setVdsId(Guid.Empty);
        doReturn(vdsDao).when(command).getVdsDAO();
        when(vdsDao.get(Guid.Empty)).thenReturn(vds);
    }

    /**
     * @return Valid parameters for the command.
     */
    protected UpdateVmDiskParameters createParameters() {
        DiskImage diskInfo = new DiskImage();
        return new UpdateVmDiskParameters(vmId, diskImageGuid, diskInfo);
    }

    /**
     * The following method will simulate a situation when disk was not found in DB
     */
    private void createNullDisk() {
        doReturn(diskDao).when(command).getDiskDao();
    }

    /**
     * The following method will create a VirtIO disk , which is marked as unplugged
     * @return
     */
    protected DiskImage cretaeVirtIODisk() {
        DiskImage disk = new DiskImage();
        disk.setImageId(diskImageGuid);
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setPlugged(false);
        disk.setactive(true);
        disk.setvm_guid(vmId);
        disk.setId(diskImageGuid);
        doReturn(diskDao).when(command).getDiskDao();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        return disk;
    }
}
