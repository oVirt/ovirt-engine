package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class MoveOrCopyDiskCommandTest {

    private final Guid diskImageGuid = Guid.NewGuid();
    private Guid destStorageId = Guid.NewGuid();
    private final Guid srcStorageId = Guid.NewGuid();
    private final static int FREE_SPACE_CRITICAL_LOW_IN_GB = 0;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, FREE_SPACE_CRITICAL_LOW_IN_GB));

    @Mock
    private DiskImageDAO diskImageDao;
    @Mock
    private StorageDomainDAO storageDomainDao;
    @Mock
    private VmDAO vmDao;
    @Mock
    private VmTemplateDAO vmTemplateDao;
    @Mock
    private VmDeviceDAO vmDeviceDao;

    /**
     * The command under test.
     */
    protected MoveOrCopyDiskCommand<MoveOrCopyImageGroupParameters> command;

    @Test
    public void canDoActionImageNotFound() throws Exception {
        initializeCommand(ImageOperation.Move);
        when(diskImageDao.get(any(Guid.class))).thenReturn(null);
        when(diskImageDao.getSnapshotById(any(Guid.class))).thenReturn(null);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionWrongDiskImageTypeTemplate() throws Exception {
        initializeCommand(ImageOperation.Move);
        initTemplateDiskImage();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK.toString()));
    }

    @Test
    public void canDoActionWrongDiskImageTypeVm() throws Exception {
        initializeCommand(ImageOperation.Copy);
        initVmDiskImage();
        doReturn(vmTemplateDao).when(command).getVmTemplateDAO();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_TEMPLATE_DISK.toString()));
    }

    @Test
    public void canDoActionCanNotFindTemplet() throws Exception {
        initializeCommand(ImageOperation.Copy);
        initTemplateDiskImage();
        doReturn(vmTemplateDao).when(command).getVmTemplateDAO();
        when(vmTemplateDao.get(any(Guid.class))).thenReturn(null);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionSameSourceAndDest() throws Exception {
        destStorageId = srcStorageId;
        initializeCommand(ImageOperation.Move);
        initVmDiskImage();
        initVm();
        initSrcStorageDomain();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME.toString()));
    }

    @Test
    public void canDoActionVmIsNotDown() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmDiskImage();
        initVm();
        initSrcStorageDomain();
        initDestStorageDomain();
        initVmDevice();
        doReturn(vmDeviceDao).when(command).getVmDeviceDAO();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN.toString()));
    }

    @Test
    public void canDoActionDiskIsLocked() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmDiskImage();
        initVm();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(vmDeviceDao).when(command).getVmDeviceDAO();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED.toString()));
    }

    @Test
    public void canDoActionTemplateImageIsLocked() throws Exception {
        initializeCommand(ImageOperation.Copy);
        initTemplateDiskImage();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(vmTemplateDao).when(command).getVmTemplateDAO();

        Map<Boolean, VmTemplate> map = Collections.singletonMap(true, new VmTemplate());
        doReturn(map).when(vmTemplateDao).getAllForImage(any(Guid.class));
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED.toString()));
    }

    protected void initVm() {
        VM vm = new VM();
        vm.setStatus(VMStatus.PoweredDown);
        doReturn(vmDao).when(command).getVmDAO();
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        mockGetVmsListForDisk();
    }

    private void mockGetVmsListForDisk() {
        List<VM> vmList = new ArrayList<VM>();
        VM vm1 = new VM();
        vm1.setStatus(VMStatus.PoweredDown);
        VM vm2 = new VM();
        vm2.setStatus(VMStatus.Down);
        vmList.add(vm1);
        vmList.add(vm2);
        when(vmDao.getVmsListForDisk(any(Guid.class))).thenReturn(vmList);
    }

    private void initSrcStorageDomain() {
        StorageDomain stDomain = new StorageDomain();
        stDomain.setstatus(StorageDomainStatus.Active);
        doReturn(storageDomainDao).when(command).getStorageDomainDAO();
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(stDomain);
    }

    private void initDestStorageDomain() {
        StorageDomain destDomain = new StorageDomain();
        destDomain.setstatus(StorageDomainStatus.Active);
        destDomain.setstorage_type(StorageType.NFS);
        doReturn(destDomain).when(command).getStorageDomain();
    }

    private void initVmDevice() {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setIsPlugged(true);
        when(vmDeviceDao.get(any(VmDeviceId.class))).thenReturn(vmDevice);
    }

    @SuppressWarnings("unchecked")
    protected void initializeCommand(ImageOperation operation) {
        command = spy(new MoveOrCopyDiskCommandDummy(new MoveOrCopyImageGroupParameters(diskImageGuid,
                srcStorageId,
                destStorageId,
                operation)));

        // Spy away the storage domain checker methods
        doReturn(true).when(command).isStorageDomainSpaceWithinThresholds();
        doReturn(true).when(command).doesStorageDomainHaveSpaceForRequest(anyLong());

        // Spy away the image handler methods
        doReturn(true).when(command).checkImageConfiguration(any(List.class));
        doReturn(Collections.emptyList()).when(command).getAllImageSnapshots();

        doReturn(false).when(command).acquireLock();
    }

    private void initTemplateDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setVmEntityType(VmEntityType.TEMPLATE);
        when(diskImageDao.get(any(Guid.class))).thenReturn(diskImage);
    }

    private void initVmDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setVmEntityType(VmEntityType.VM);
        when(diskImageDao.get(any(Guid.class))).thenReturn(diskImage);
    }

    /**
     * The following class is created in order to allow to use a mock diskImageDao in constructor
     */
    private class MoveOrCopyDiskCommandDummy extends MoveOrCopyDiskCommand<MoveOrCopyImageGroupParameters> {

        private static final long serialVersionUID = -1781827271090649224L;

        public MoveOrCopyDiskCommandDummy(MoveOrCopyImageGroupParameters parameters) {
            super(parameters);
        }

        @Override
        protected DiskImageDAO getDiskImageDao() {
            return diskImageDao;
        }

        @Override
        protected boolean acquireLockInternal() {
            return true;
        }

    }
}
