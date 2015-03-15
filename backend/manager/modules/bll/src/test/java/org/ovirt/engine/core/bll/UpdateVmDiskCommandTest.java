package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
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

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.SetVolumeDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;


@RunWith(MockitoJUnitRunner.class)
public class UpdateVmDiskCommandTest {

    private Guid diskImageGuid = Guid.newGuid();
    private Guid vmId = Guid.newGuid();
    private Guid sdId = Guid.newGuid();
    private Guid spId = Guid.newGuid();

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
    private StorageDomainStaticDAO storageDomainStaticDao;
    @Mock
    private StorageDomainDAO storageDomainDao;
    @Mock
    private DbFacade dbFacade;
    @Mock
    private DiskValidator diskValidator;

    @Mock
    private OsRepository osRepository;

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.ShareableDiskEnabled, Version.v3_1.toString(), true)
    );

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    /**
     * The command under test.
     */
    private UpdateVmDiskCommand<UpdateVmDiskParameters> command;

    @Test
    public void getOtherVmDisks() {
        UpdateVmDiskParameters parameters = createParameters();

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.newGuid());
        otherDisk.setActive(true);
        when(diskDao.getAllForVm(vmId)).thenReturn(new LinkedList<>(Arrays.asList(parameters.getDiskInfo(),
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
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void canDoActionFailedVMHasNotDisk() throws Exception {
        initializeCommand(createParameters());
        createNullDisk();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void canDoActionFailedShareableDiskVolumeFormatUnsupported() throws Exception {
        UpdateVmDiskParameters parameters = createParameters();
        DiskImage disk = createShareableDisk(VolumeFormat.COW);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.NFS);
        parameters.setDiskInfo(disk);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());
        initializeCommand(parameters);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
    }

    @Test
    public void canDoActionFailedUpdateReadOnly() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(true);
        initializeCommand(parameters, Collections.singletonList(createVm(VMStatus.Up)));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void canDoActionFailedROVmAttachedToPool() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(true);
        VM vm = createVm(VMStatus.Down);
        vm.setVmPoolId(Guid.newGuid());
        initializeCommand(parameters, Collections.singletonList(vm));

        VmDevice vmDevice = stubVmDevice(diskImageGuid, vmId); // Default RO is false
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);

        vmDevice.setIsReadOnly(true);
        parameters.getDiskInfo().setReadOnly(false);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
    }

    @Test
    public void canDoActionFailedWipeVmAttachedToPool() {
        Disk oldDisk = createDiskImage();
        oldDisk.setWipeAfterDelete(true);
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);

        UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setWipeAfterDelete(false);
        VM vm = createVm(VMStatus.Down);
        vm.setVmPoolId(Guid.newGuid());
        initializeCommand(parameters, Collections.singletonList(vm));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);

        oldDisk.setWipeAfterDelete(false);
        parameters.getDiskInfo().setWipeAfterDelete(true);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
    }

    @Test
    public void canDoActionFailedShareableDiskOnGlusterDomain() throws Exception {
        UpdateVmDiskParameters parameters = createParameters();
        DiskImage disk = createShareableDisk(VolumeFormat.RAW);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.GLUSTERFS);
        parameters.setDiskInfo(disk);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());
        initializeCommand(parameters);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
    }


    @Test
    public void nullifiedSnapshotOnUpdateDiskToShareable() {
        UpdateVmDiskParameters parameters = createParameters();
        DiskImage disk = createShareableDisk(VolumeFormat.RAW);
        parameters.setDiskInfo(disk);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.NFS);
        parameters.setDiskInfo(disk);

        DiskImage oldDisk = createDiskImage();
        oldDisk.setVmSnapshotId(Guid.newGuid());

        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());

        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        mockInterfaceList();

        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
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

        mockInterfaceList();

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

        mockInterfaceList();

        // The command should only succeed if there is no other bootable disk
        assertEquals(!boot, command.canDoAction());
    }

    @Test
    public void canDoActionUpdateWipeAfterDeleteVmDown() {
        canDoActionUpdateWipeAfterDelete(VMStatus.Down);
    }

    @Test
    public void canDoActionUpdateWipeAfterDeleteVmUp() {
        canDoActionUpdateWipeAfterDelete(VMStatus.Up);
    }

    private void canDoActionUpdateWipeAfterDelete(VMStatus status) {
        DiskImage disk = createDiskImage();
        disk.setReadOnly(false);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(false);
        parameters.getDiskInfo().setWipeAfterDelete(true);
        initializeCommand(parameters, Collections.singletonList(createVm(status)));

        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void canDoActionUpdateDescriptionVmDown() {
        canDoActionUpdateDescription(VMStatus.Down);
    }

    @Test
    public void canDoActionUpdateDescriptionVmUp() {
        canDoActionUpdateDescription(VMStatus.Up);
    }

    private void canDoActionUpdateDescription(VMStatus status) {
        DiskImage disk = createDiskImage();
        disk.setReadOnly(false);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(false);
        disk.setDescription(RandomUtils.instance().nextString(10));
        initializeCommand(parameters, Collections.singletonList(createVm(status)));

        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
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
                assertNotSame(oldDisk.getDiskInterface(), parameters.getDiskInfo().getDiskInterface());
                return oldDisk;
            }
        });
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();

        // verify that device address was cleared exactly once
        verify(vmDeviceDAO).clearDeviceAddress(diskImageGuid);
    }

    private void mockVdsCommandSetVolumeDescription() {
        VDSReturnValue ret = new VDSReturnValue();
        doReturn(ret).when(command).runVdsCommand
                (eq(VDSCommandType.SetVolumeDescription),
                        any(SetVolumeDescriptionVDSCommandParameters.class));
    }

    @Test
    public void testUpdateReadOnlyPropertyOnChange() {
        // Disk should be updated as Read Only
        final UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(true);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);
        stubVmDevice(diskImageGuid, vmId);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();

        verify(command, atLeast(1)).updateReadOnlyRequested();
        assertTrue(command.updateReadOnlyRequested());
        verify(vmDeviceDAO).update(any(VmDevice.class));
    }

    @Test
    public void testUpdateDiskInterfaceUnsupported() {
        final UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setDiskInterface(DiskInterface.IDE);
        when(diskDao.get(diskImageGuid)).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            final DiskImage oldDisk = createDiskImage();
            oldDisk.setDiskInterface(DiskInterface.VirtIO);
            assertNotSame(oldDisk.getDiskInterface(), parameters.getDiskInfo().getDiskInterface());
            return oldDisk;
            }
        });

        initializeCommand(parameters);
        doReturn(true).when(command).validatePciAndIdeLimit(anyList());
        mockVdsCommandSetVolumeDescription();

        when(diskValidator.isReadOnlyPropertyCompatibleWithInterface()).thenReturn(ValidationResult.VALID);
        when(diskValidator.isDiskInterfaceSupported(any(VM.class))).thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(command.getDiskValidator(parameters.getDiskInfo())).thenReturn(diskValidator);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED);
    }

    @Test
    public void testDoNotUpdateDeviceWhenReadOnlyIsNotChanged() {
        final UpdateVmDiskParameters parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(false);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();

        verify(command, atLeast(1)).updateReadOnlyRequested();
        assertFalse(command.updateReadOnlyRequested());
        verify(vmDeviceDAO, never()).update(any(VmDevice.class));
    }

    @Test
    public void testFailInterfaceCanUpdateReadOnly() {
        initializeCommand(new UpdateVmDiskParameters(vmId, diskImageGuid, createDiskImage()));
        doReturn(true).when(command).updateReadOnlyRequested();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR)).
                when(diskValidator).isReadOnlyPropertyCompatibleWithInterface();

        assertFalse(command.validateCanUpdateReadOnly(diskValidator));
    }

    @Test
    public void testSucceedInterfaceCanUpdateReadOnly() {
        initializeCommand(new UpdateVmDiskParameters(vmId, diskImageGuid, createDiskImage()));
        doReturn(true).when(command).updateReadOnlyRequested();
        doReturn(ValidationResult.VALID).when(diskValidator).isReadOnlyPropertyCompatibleWithInterface();

        assertTrue(command.validateCanUpdateReadOnly(diskValidator));
    }

    @Test
    public void testUpdateOvfDiskNotSupported() {
        DiskImage updatedDisk = createDiskImage();
        updatedDisk.setReadOnly(true);
        updatedDisk.setDiskInterface(DiskInterface.IDE);

        DiskImage diskFromDB = createDiskImage();
        diskFromDB.setReadOnly(false);
        diskFromDB.setDiskInterface(DiskInterface.IDE);
        diskFromDB.setOvfStore(true);

        when(diskDao.get(diskImageGuid)).thenReturn(diskFromDB);

        initializeCommand(new UpdateVmDiskParameters(vmId, diskImageGuid, updatedDisk));

        when(diskValidator.isDiskUsedAsOvfStore()).thenCallRealMethod();

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED);
    }

    @Test
    public void testResize() {
        DiskImage oldDisk = createDiskImage();
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);

        UpdateVmDiskParameters parameters = createParameters();
        ((DiskImage) parameters.getDiskInfo()).setSize(oldDisk.getSize() * 2);
        initializeCommand(parameters);

        assertTrue(command.validateCanResizeDisk());
    }

    @Test
    public void testFaultyResize() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        UpdateVmDiskParameters parameters = createParameters();
        ((DiskImage) parameters.getDiskInfo()).setSize(parameters.getDiskInfo().getSize() / 2);
        initializeCommand(parameters);

        assertFalse(command.validateCanResizeDisk());
        CanDoActionTestUtils.assertCanDoActionMessages
                ("wrong failure", command, VdcBllMessages.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
    }

    @Test
    public void testFailedRoDiskResize() {
        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(Integer.MAX_VALUE);
        sd.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(sd);

        UpdateVmDiskParameters parameters = createParameters();
        ((DiskImage) parameters.getDiskInfo()).setSize(parameters.getDiskInfo().getSize() * 2);
        initializeCommand(parameters);

        DiskImage oldDisk = createDiskImage();
        doReturn(oldDisk).when(command).getOldDisk();

        VmDevice vmDevice = stubVmDevice(diskImageGuid, vmId);
        vmDevice.setIsReadOnly(true);

        assertFalse(command.validateCanResizeDisk());
        CanDoActionTestUtils.assertCanDoActionMessages
                ("wrong failure", command, VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_RESIZE_READ_ONLY_DISK);
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
        doReturn(storageDomainStaticDao).when(command).getStorageDomainStaticDAO();
        doReturn(storageDomainDao).when(command).getStorageDomainDAO();
        doReturn(vmStaticDAO).when(command).getVmStaticDAO();
        doReturn(baseDiskDao).when(command).getBaseDiskDao();
        doReturn(imageDao).when(command).getImageDao();
        doReturn(vmDeviceDAO).when(command).getVmDeviceDao();
        doReturn(vmDAO).when(command).getVmDAO();
        doReturn(diskDao).when(command).getDiskDao();
        doNothing().when(command).reloadDisks();
        doNothing().when(command).updateBootOrder();
        doNothing().when(vmStaticDAO).incrementDbGeneration(any(Guid.class));

        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        doReturn(snapshotsValidator).when(command).getSnapshotsValidator();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));
        when(diskValidator.isVirtIoScsiValid(any(VM.class))).thenReturn(ValidationResult.VALID);
        when(diskValidator.isDiskUsedAsOvfStore()).thenReturn(ValidationResult.VALID);
        doReturn(true).when(command).setAndValidateDiskProfiles();

        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);

        mockVds();
        mockVmsStoragePoolInfo(vms);
        mockToUpdateDiskVm(vms);

        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(Integer.MAX_VALUE);
        sd.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(sd);
        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        doReturn(sdValidator).when(command).getStorageDomainValidator(any(DiskImage.class));
    }

    @Test
    public void testDiskAliasAdnDescriptionMetaDataShouldNotBeUpdated() {
        // Disk should be updated as Read Only
        final UpdateVmDiskParameters parameters = createParameters();
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
    }

    @Test
    public void testDiskAliasAdnDescriptionMetaDataShouldBeUpdated() {
        // Disk should be updated as Read Only
        final UpdateVmDiskParameters parameters = createParameters();
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        parameters.getDiskInfo().setDiskAlias("New Disk Alias");
        parameters.getDiskInfo().setDiskDescription("New Disk Description");
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).runVdsCommand(eq(VDSCommandType.SetVolumeDescription),
                any(SetVolumeDescriptionVDSCommandParameters.class));
    }

    @Test
    public void testOnlyDiskAliasChangedMetaDataShouldBeUpdated() {
        // Disk should be updated as Read Only
        final UpdateVmDiskParameters parameters = createParameters();
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        parameters.getDiskInfo().setDiskAlias("New Disk Alias");
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).runVdsCommand(eq(VDSCommandType.SetVolumeDescription),
                any(SetVolumeDescriptionVDSCommandParameters.class));
    }

    @Test
    public void testOnlyDescriptionsChangedMetaDataShouldBeUpdated() {
        // Disk should be updated as Read Only
        final UpdateVmDiskParameters parameters = createParameters();

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        parameters.getDiskInfo().setDiskDescription("New Disk Description");
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).runVdsCommand(eq(VDSCommandType.SetVolumeDescription),
                any(SetVolumeDescriptionVDSCommandParameters.class));
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

    protected void mockInterfaceList() {
        ArrayList<String> diskInterfaces = new ArrayList<>(
                Arrays.asList(new String[] {
                        "IDE",
                        "VirtIO",
                        "VirtIO_SCSI"
                }));

        when(osRepository.getDiskInterfaces(anyInt(), any(Version.class))).thenReturn(diskInterfaces);
    }

    protected VM createVmStatusDown(VM... otherPluggedVMs) {
        return createVm(VMStatus.Down);
    }

    protected VM createVm(VMStatus status) {
        VM vm = new VM();
        vm.setStatus(status);
        vm.setGuestOs("rhel6");
        vm.setId(vmId);
        vm.setVdsGroupCompatibilityVersion(Version.v3_1);
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
        Map<Boolean, List<VM>> vmsMap = new HashMap<>();
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
        storagePool.setCompatibilityVersion(compatibilityVersion);
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
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setStorageIds(new ArrayList<>(Collections.singleton(sdId)));
        disk.setStoragePoolId(spId);
        disk.setDescription(RandomUtils.instance().nextString(10));
        return disk;
    }

    /**
     * The following method will create a Shareable DiskImage with a specified format
     */
    private DiskImage createShareableDisk(VolumeFormat volumeFormat) {
        DiskImage disk = createDiskImage();
        disk.setvolumeFormat(volumeFormat);
        disk.setShareable(true);
        disk.setDiskInterface(DiskInterface.VirtIO);
        return disk;
    }

    private StorageDomain addNewStorageDomainToDisk(DiskImage diskImage, StorageType storageType) {
        StorageDomain storage = new StorageDomain();
        storage.setId(Guid.newGuid());
        storage.setStorageType(storageType);
        storage.setStatus(StorageDomainStatus.Active);
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(storage.getId())));
        return storage;
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
                null,
                "",
                null,
                null,
                null);
    }

    private VmDevice stubVmDevice(Guid diskId, Guid vmId) {
        VmDevice vmDevice = createVmDevice(diskId, vmId);
        doReturn(vmDevice).when(command).getVmDeviceForVm();
        return vmDevice;
    }
}
