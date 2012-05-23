package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class UpdateVmDiskCommandTest {
    @Rule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.HotPlugSupportedOsList,
                    "Windows2008,Windows2008x64,Windows2008R2x64,RHEL5,RHEL5x64,RHEL6,RHEL6x64")
            );

    private Guid diskImageGuid = Guid.NewGuid();
    private Guid vmId = Guid.NewGuid();

    @Mock
    private VmDAO vmDAO;
    @Mock
    private VdsDAO vdsDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private VmNetworkInterfaceDAO vmNetworkInterfaceDAO;

    /**
     * The command under test.
     */
    protected UpdateVmDiskCommand<UpdateVmDiskParameters> command;

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
            // Overridden here and not during spying, since it's called in the constructor
            @SuppressWarnings("synthetic-access")
            @Override
            protected DiskDao getDiskDao() {
                return diskDao;
            }

        });
        doReturn(vmNetworkInterfaceDAO).when(command).getVmNetworkInterfaceDAO();
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
        when(diskDao.get(diskImageGuid)).thenReturn(null);
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
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        return disk;
    }
}
