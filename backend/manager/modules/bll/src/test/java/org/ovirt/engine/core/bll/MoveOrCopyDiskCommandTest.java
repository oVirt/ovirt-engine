package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({StorageDomainSpaceChecker.class, ImagesHandler.class, ImagesHandler.class})
public class MoveOrCopyDiskCommandTest {

    private Guid diskImageGuid = Guid.NewGuid();
    private Guid destStorageId = Guid.NewGuid();
    private Guid srcStorageId = Guid.NewGuid();

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

    @Before
    public void setUp() {
        mockStatic(StorageDomainSpaceChecker.class);
        mockStatic(ImagesHandler.class);
        when(StorageDomainSpaceChecker.hasSpaceForRequest(any(storage_domains.class), anyLong())).thenReturn(true);
        when(StorageDomainSpaceChecker.isBelowThresholds(any(storage_domains.class))).thenReturn(true);
        when(ImagesHandler.getAllImageSnapshots(any(Guid.class), any(Guid.class))).thenReturn(new ArrayList<DiskImage>());
        when(ImagesHandler.CheckImageConfiguration(any(storage_domain_static.class), any(DiskImageBase.class), any(ArrayList.class))).thenReturn(true);
        MockitoAnnotations.initMocks(this);
    }

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

    protected void initVm() {
        VM vm = new VM();
        vm.setstatus(VMStatus.PoweredDown);
        AuditLogableBaseMockUtils.mockVmDao(command, vmDao);
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
    }

    private void initSrcStorageDomain() {
        storage_domains stDomain = new storage_domains();
        stDomain.setstatus(StorageDomainStatus.Active);
        doReturn(storageDomainDao).when(command).getStorageDomainDAO();
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(stDomain);
    }

    private void initDestStorageDomain() {
        storage_domains destDomain = new storage_domains();
        destDomain.setstatus(StorageDomainStatus.Active);
        destDomain.setstorage_type(StorageType.NFS);
        doReturn(destDomain).when(command).getStorageDomain();
    }

    private void initVmDevice() {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setIsPlugged(true);
        when(vmDeviceDao.get(any(VmDeviceId.class))).thenReturn(vmDevice);
    }

    protected void initializeCommand(ImageOperation operation) {
        command =
                spy(new MoveOrCopyDiskCommandDummy(new MoveOrCopyImageGroupParameters(diskImageGuid,
                        srcStorageId,
                        destStorageId,
                        operation)));
    }

    private void initTemplateDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setVmEntityType(VmEntityType.TEMPLATE);
        when(diskImageDao.get(any(Guid.class))).thenReturn(diskImage);
    }

    private void initVmDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setVmEntityType(VmEntityType.VM);
        diskImage.setvm_guid(Guid.NewGuid());
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

    }
}
