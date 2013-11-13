package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.infinispan.transaction.tm.DummyTransactionManager;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;


@RunWith(MockitoJUnitRunner.class)
public class UpdateVmDiskCommandTest {

    private Guid diskImageGuid = Guid.newGuid();
    private Guid vmId = Guid.newGuid();

    @Mock
    private VmDAO vmDAO;
    @Mock
    private VdsDAO vdsDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private VmStaticDAO vmStaticDAO;
    @Mock
    private BaseDiskDao baseDiskDao;
    @Mock
    private ImageDao imageDao;
    @Mock
    private SnapshotDao snapshotDao;
    @Mock
    private DiskImageDAO diskImageDao;
    @Mock
    private VmDeviceDAO vmDeviceDAO;
    @Mock
    private StoragePoolDAO storagePoolDao;
    @Mock
    private DbFacade dbFacade;

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.ShareableDiskEnabled, Version.v3_1.toString(), true)
    );

    /**
     * The command under test.
     */
    protected UpdateVmDiskCommand<UpdateVmDiskParameters> command;

    @Test
    public void getOtherVmDisks() {
        UpdateVmDiskParameters parameters = createParameters();

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.newGuid());
        otherDisk.setActive(true);
        when(diskDao.getAllForVm(vmId)).thenReturn(new LinkedList<Disk>(Arrays.asList(parameters.getDiskInfo(),
                otherDisk)));
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);

        VM vm = createVmStatusDown();
        mockCtorRelatedDaoCalls(Collections.singletonList(vm));
        List<Disk> otherDisks = command.getOtherVmDisks(vm.getId());
        assertEquals("Wrong number of other disks", 1, otherDisks.size());
        assertFalse("Wrong other disk", otherDisks.contains(parameters.getDiskInfo()));
    }

    @Test
    public void canDoActionFailedVMNotFound() throws Exception {
        initializeCommand(createParameters());
        mockNullVm();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND.toString()));
    }

    @Test
    public void canDoActionFailedVMHasNotDisk() throws Exception {
        initializeCommand(createParameters());
        createNullDisk();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionFailedShareableDiskVolumeFormatUnsupported() throws Exception {
        UpdateVmDiskParameters parameters = createParameters();
        parameters.setDiskInfo(createShareableDisk(VolumeFormat.COW));

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT.toString()));
    }

    @Test
    public void nullifiedSnapshotOnUpdateDiskToShareable() {
        UpdateVmDiskParameters parameters = createParameters();
        parameters.setDiskInfo(createShareableDisk(VolumeFormat.RAW));

        DiskImage oldDisk = createDiskImage();
        oldDisk.setVmSnapshotId(Guid.newGuid());

        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        initializeCommand(parameters);

        assertTrue(command.canDoAction());
        command.executeVmCommand();
        assertTrue(oldDisk.getVmSnapshotId() == null);
    }

    @Test
    public void canDoActionMakeDiskBootableSuccess() {
        canDoActionMakeDiskBootable(false);
    }

    @Test
    public void canDoActionMakeDiskBootableFail() {
        canDoActionMakeDiskBootable(true);
    }

    private void canDoActionMakeDiskBootable(boolean boot) {
        UpdateVmDiskParameters parameters = createParameters();
        Disk newDisk = parameters.getDiskInfo();
        newDisk.setBoot(true);

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.newGuid());
        otherDisk.setActive(true);
        otherDisk.setBoot(boot);
        if (boot) {
            when(diskDao.getVmBootActiveDisk(vmId)).thenReturn(otherDisk);
        }
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        initializeCommand(parameters);

        // The command should only succeed if there is no other bootable disk
        assertEquals(!boot, command.canDoAction());
    }

    @Test
    public void canDoActionMakeDiskBootableOnOtherVmSuccess() {
        canDoActionMakeDiskBootableOnOtherVm(false);
    }

    @Test
    public void canDoActionMakeDiskBootableOnOtherVmFail() {
        canDoActionMakeDiskBootableOnOtherVm(true);
    }

    private void canDoActionMakeDiskBootableOnOtherVm(boolean boot) {
        UpdateVmDiskParameters parameters = createParameters();
        Disk newDisk = parameters.getDiskInfo();
        newDisk.setBoot(true);

        Guid otherVmId = Guid.newGuid();
        VM otherVm = new VM();
        otherVm.setId(otherVmId);

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.newGuid());
        otherDisk.setActive(true);
        otherDisk.setBoot(boot);
        if (boot) {
            when(diskDao.getVmBootActiveDisk(otherVmId)).thenReturn(otherDisk);
        }
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters, Arrays.asList(createVmStatusDown(), otherVm));

        // The command should only succeed if there is no other bootable disk
        assertEquals(!boot, command.canDoAction());
    }

    @Test
    public void clearAddressOnInterfaceChange() {
        final UpdateVmDiskParameters parameters = createParameters();
        // update new disk interface so it will be different than the old one
        parameters.getDiskInfo().setDiskInterface(DiskInterface.VirtIO_SCSI);

        // creating old disk with interface different than interface of disk from parameters
        // have to return original disk on each request to dao,
        // since the command updates retrieved instance of disk
        when(diskDao.get(diskImageGuid)).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            final DiskImage oldDisk = createDiskImage();
            oldDisk.setDiskInterface(DiskInterface.VirtIO);
            assert(oldDisk.getDiskInterface() != parameters.getDiskInfo().getDiskInterface());
            return oldDisk;
            }
        });

        initializeCommand(parameters);

        VmDevice device = createVmDevice(diskImageGuid, vmId);
        doReturn(device).when(vmDeviceDAO).get(device.getId());

        command.executeVmCommand();

        // verify that device address was cleared exactly once
        verify(vmDeviceDAO, times(1)).clearDeviceAddress(device.getDeviceId());
    }

    @Test
    public void testUpdateReadOnlyPropertyOnChange() {
        // Disk should be updated as Read Only
        final UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(true);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        VmDevice device = createVmDevice(diskImageGuid, vmId);
        doReturn(device).when(vmDeviceDAO).get(device.getId());

        initializeCommand(parameters);
        command.executeVmCommand();

        device.setIsReadOnly(true);
        verify(vmDeviceDAO, times(1)).update(device);
    }

    @Test
    public void testDoNotUpdateDeviceWhenReadOnlyIsNotChanged() {
        // New disk is a read write
        final UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(false);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        // Disk is already attached to VM as a read write
        VmDevice device = createVmDevice(diskImageGuid, vmId);
        doReturn(device).when(vmDeviceDAO).get(device.getId());

        // To be sure that readOnly property is not changed
        assertEquals(device.getIsReadOnly(), parameters.getDiskInfo().getReadOnly());

        initializeCommand(parameters);
        command.executeVmCommand();

        assertFalse(command.shouldUpdateReadOnly());
        verify(command, atLeast(1)).shouldUpdateReadOnly();
        verify(vmDeviceDAO, never()).update(any(VmDevice.class));
    }

    private void initializeCommand(UpdateVmDiskParameters params) {
        initializeCommand(params, Collections.singletonList(createVmStatusDown()));
    }

    protected void initializeCommand(UpdateVmDiskParameters params, List<VM> vms) {
        // Done before creating the spy to have correct values during the ctor run
        mockCtorRelatedDaoCalls(vms);
        command = spy(new UpdateVmDiskCommand<UpdateVmDiskParameters>(params) {
            // Overridden here and not during spying, since it's called in the constructor
            @SuppressWarnings("synthetic-access")
            @Override
            protected DiskDao getDiskDao() {
                return diskDao;
            }

            @Override
            public VmDAO getVmDAO() {
                return vmDAO;
            }

        });
        doReturn(true).when(command).acquireLockInternal();
        doReturn(snapshotDao).when(command).getSnapshotDao();
        doReturn(diskImageDao).when(command).getDiskImageDao();
        doReturn(storagePoolDao).when(command).getStoragePoolDAO();
        doReturn(vmStaticDAO).when(command).getVmStaticDAO();
        doReturn(baseDiskDao).when(command).getBaseDiskDao();
        doReturn(imageDao).when(command).getImageDao();
        doReturn(vmDeviceDAO).when(command).getVmDeviceDao();
        doReturn(vmDAO).when(command).getVmDAO();
        doReturn(diskDao).when(command).getDiskDao();
        doNothing().when(command).reloadDisks();
        doNothing().when(command).updateBootOrder();
        doNothing().when(vmStaticDAO).incrementDbGeneration(any(Guid.class));

        ejbRule.mockResource(ContainerManagedResourceType.TRANSACTION_MANAGER, new DummyTransactionManager());

        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        doReturn(snapshotsValidator).when(command).getSnapshotsValidator();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));

        mockVds();
        mockVmsStoragePoolInfo(vms);
        mockToUpdateDiskVm(vms);
    }

    private void mockToUpdateDiskVm(List<VM> vms) {
        for (VM vm: vms) {
            if (vm.getId().equals(command.getParameters().getVmId())) {
                when(vmDAO.get(command.getParameters().getVmId())).thenReturn(vm);
                break;
            }
        }
    }

    private void mockNullVm() {
        mockGetForDisk((VM) null);
        mockGetVmsListForDisk(null);
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(null);
    }

    protected VM createVmStatusDown(VM... otherPluggedVMs) {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setGuestOs("rhel6");
        vm.setId(vmId);
        return vm;
    }

    private void mockCtorRelatedDaoCalls(List<VM> vms) {
        mockGetForDisk(vms);
        mockGetVmsListForDisk(vms);

    }

    private void mockVmsStoragePoolInfo(List<VM> vms) {
        StoragePool storagePool = mockStoragePool(Version.v3_1);
        for (VM vm : vms) {
            vm.setStoragePoolId(storagePool.getId());
        }
    }

    private void mockGetForDisk(VM vm) {
        mockGetForDisk(Collections.singletonList(vm));
    }

    private void mockGetForDisk(List<VM> vms) {
        Map<Boolean, List<VM>> vmsMap = new HashMap<Boolean, List<VM>>();
        vmsMap.put(Boolean.TRUE, vms);
        when(vmDAO.getForDisk(diskImageGuid, true)).thenReturn(vmsMap);
    }

    private void mockGetVmsListForDisk(List<VM> vms) {
        List<Pair<VM, VmDevice>> vmsWithVmDevice = new ArrayList<>();
        if (vms != null) {
            for (VM vm : vms) {
            VmDevice device = createVmDevice(diskImageGuid, vm.getId());
            vmsWithVmDevice.add(new Pair<>(vm, device));
            }
        } else {
            vms = Collections.emptyList();
        }

        when(vmDAO.getVmsWithPlugInfo(diskImageGuid)).thenReturn(vmsWithVmDevice);
        when(vmDAO.getVmsListForDisk(diskImageGuid, true)).thenReturn(vms);
    }

    /**
     * Mock VDS
     */
    protected void mockVds() {
        VDS vds = new VDS();
        vds.setVdsGroupCompatibilityVersion(new Version("3.1"));
        command.setVdsId(Guid.Empty);
        doReturn(vdsDao).when(command).getVdsDAO();
        when(vdsDao.get(Guid.Empty)).thenReturn(vds);
    }

    /**
     * Mock a {@link StoragePool}.
     *
     * @param compatibilityVersion
     * @return
     */
    private StoragePool mockStoragePool(Version compatibilityVersion) {
        Guid storagePoolId = Guid.newGuid();
        StoragePool storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setcompatibility_version(compatibilityVersion);
        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);

        return storagePool;
    }

    /**
     * @return Valid parameters for the command.
     */
    protected UpdateVmDiskParameters createParameters() {
        DiskImage diskInfo = createDiskImage();
        return new UpdateVmDiskParameters(vmId, diskImageGuid, diskInfo);
    }

    /**
     * The following method will simulate a situation when disk was not found in DB
     */
    private void createNullDisk() {
        when(diskDao.get(diskImageGuid)).thenReturn(null);
    }

    /**
     * The following method will create a new DiskImage
     */
    private DiskImage createDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setId(diskImageGuid);
        disk.setSize(100000L);
        return disk;
    }

    /**
     * The following method will create a Shareable DiskImage with a specified format
     */
    private DiskImage createShareableDisk(VolumeFormat volumeFormat) {
        DiskImage disk = createDiskImage();
        disk.setvolumeFormat(volumeFormat);
        disk.setShareable(true);
        return disk;
    }

    private VmDevice createVmDevice(Guid diskId, Guid vmId) {
        return new VmDevice(new VmDeviceId(diskId, vmId),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                0,
                null,
                true,
                true,
                false,
                "",
                null,
                null);
    }
}
