package org.ovirt.engine.core.bll;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.action.AddDiskToVmParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ VmHandler.class, ImagesHandler.class, StorageDomainSpaceChecker.class, Config.class,
        AsyncTaskManager.class, SchedulerUtilQuartzImpl.class })
public class AddDiskToVmCommandTest {
    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private StorageDomainStaticDAO storageDomainStaticDAO;

    @Mock
    private StoragePoolIsoMapDAO storagePoolIsoMapDAO;

    @Mock
    private VmNetworkInterfaceDAO vmNetworkInterfaceDAO;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private AsyncTaskManager asyncTaskManager;

    /**
     * The command under test.
     */
    private AddDiskToVmCommand<AddDiskToVmParameters> command;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        mockStatic(SchedulerUtilQuartzImpl.class);
        SchedulerUtilQuartzImpl scheduler = mock(SchedulerUtilQuartzImpl.class);
        when(SchedulerUtilQuartzImpl.getInstance()).thenReturn(scheduler);
        mockStatic(Config.class);
        when(Config.GetValue(ConfigValues.AsyncTaskPollingRate)).thenReturn(Integer.MAX_VALUE);
        when(Config.GetValue(ConfigValues.AsyncTaskStatusCacheRefreshRateInSeconds)).
                thenReturn(Integer.MAX_VALUE);
        when(Config.GetValue(ConfigValues.AsyncTaskStatusCachingTimeInMinutes)).
                thenReturn(Integer.MAX_VALUE);
        mockStatic(AsyncTaskManager.class);
        MockitoAnnotations.initMocks(this);
        mockStatic(VmHandler.class);
        mockStatic(ImagesHandler.class);
        when(ImagesHandler.CheckImagesConfiguration(
                any(Guid.class), any(ArrayList.class), any(ArrayList.class))).thenReturn(true);
        when(ImagesHandler.PerformImagesChecks(
                any(Guid.class),
                any(ArrayList.class),
                any(Guid.class),
                any(Guid.class),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean())).thenReturn(true);
        mockStatic(StorageDomainSpaceChecker.class);
        when(StorageDomainSpaceChecker.isBelowThresholds(any(storage_domains.class))).thenReturn(true);
        when(Config.GetValue(ConfigValues.MaxDiskSize)).thenReturn(Integer.MAX_VALUE);
        when(AsyncTaskManager.getInstance()).thenReturn(asyncTaskManager);
        when(asyncTaskManager.EntityHasTasks(any(Guid.class))).thenReturn(false);
        final int defaultFreeSpaceLow = 10;
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceLow)).thenReturn(defaultFreeSpaceLow);
        final int defaultFreeSpaceGB = 5;
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(defaultFreeSpaceGB);
    }

    @Test
    public void canDoActionSucceedsOnDiskDomainCheckWhenNoDisks() throws Exception {
        Guid storageId = Guid.NewGuid();
        initializeCommand(storageId);

        mockVm();
        mockVmNetworks();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionSucceedsOnDiskDomainCheckWhenEmptyStorageGuidInParams() throws Exception {
        initializeCommand(Guid.Empty);
        Guid storageId = Guid.NewGuid();

        mockVmWithDisk(storageId);
        mockVmNetworks();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMatches() throws Exception {
        Guid storageId = Guid.NewGuid();
        initializeCommand(storageId);

        mockVmWithDisk(storageId);
        mockVmNetworks();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMismatches() throws Exception {
        Guid storageId = Guid.NewGuid();
        initializeCommand(storageId);

        mockVmWithDisk(Guid.NewGuid());
        mockVmNetworks();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        doReturn(storageDomainStaticDAO).when(command).getStorageDomainStaticDao();

        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNullDiskType() throws Exception {
        Guid storageId = Guid.NewGuid();
        DiskImageBase image = new DiskImageBase();
        image.setdisk_interface(DiskInterface.IDE);
        image.setvolume_type(VolumeType.Preallocated);
        image.setvolume_format(VolumeFormat.COW);
        AddDiskToVmParameters params = new AddDiskToVmParameters(Guid.NewGuid(), image);
        initializeCommand(storageId, params);
        assertFalse(command.validateInputs());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains("VALIDATION.DISK_TYPE.NOT_NULL"));
    }

    @Test
    public void canDoActionFailsOnNullDiskInterface() throws Exception {
        Guid storageId = Guid.NewGuid();
        DiskImageBase image = new DiskImageBase();
        image.setdisk_type(DiskType.Data);
        image.setvolume_format(VolumeFormat.COW);
        image.setvolume_type(VolumeType.Preallocated);
        AddDiskToVmParameters params = new AddDiskToVmParameters(Guid.NewGuid(), image);
        initializeCommand(storageId, params);
        assertFalse(command.validateInputs());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains("VALIDATION.DISK_INTERFACE.NOT_NULL"));
    }

    @Test
    public void canDoActionThinProvisioningSpaceCheckSucceeds() throws Exception {
        final int availableSize = 6;
        final int usedSize = 4;
        Guid sdid = Guid.NewGuid();
        initializeCommand(sdid, VolumeType.Sparse);

        mockVm();
        mockVmNetworks();
        storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
        mockStoragePoolIsoMap();
        mockStorageDomainSpaceChecker(domains, true);

        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionThinProvisioningSpaceCheckFailsSize() {
        final int availableSize = 4;
        final int usedSize = 6;
        Guid sdid = Guid.NewGuid();
        initializeCommand(sdid, VolumeType.Sparse);

        mockVm();
        mockVmNetworks();
        storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
        mockStoragePoolIsoMap();
        mockStorageDomainSpaceChecker(domains, false);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canDoActionThinProvisioningSpaceCheckFailsPct() {
        final int availableSize = 9;
        final int usedSize = 191;
        Guid sdid = Guid.NewGuid();
        initializeCommand(sdid, VolumeType.Sparse);

        mockVm();
        mockVmNetworks();
        storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
        mockStoragePoolIsoMap();
        mockStorageDomainSpaceChecker(domains, false);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canDoActionPreallocatedSpaceCheckSucceeds() {
        final int availableSize = 12;
        final int usedSize = 8;
        Guid sdid = Guid.NewGuid();
        initializeCommand(sdid, VolumeType.Preallocated);

        mockVm();
        mockVmNetworks();
        storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
        mockStoragePoolIsoMap();
        mockStorageDomainSpaceCheckerRequest(domains, true);
        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionPreallocatedSpaceCheckFailsSize() {
        final int availableSize = 8;
        final int usedSize = 7;
        Guid sdid = Guid.NewGuid();
        initializeCommand(sdid, VolumeType.Preallocated);

        mockVm();
        mockVmNetworks();
        storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
        mockStoragePoolIsoMap();
        mockStorageDomainSpaceCheckerRequest(domains, false);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canDoActionPreallocatedSpaceCheckFailsPct() {
        final int availableSize = 9;
        final int usedSize = 191;
        Guid sdid = Guid.NewGuid();
        initializeCommand(sdid, VolumeType.Preallocated);

        mockVm();
        mockVmNetworks();
        storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
        mockStoragePoolIsoMap();
        mockStorageDomainSpaceCheckerRequest(domains, false);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    /**
     * Initialize the command for testing, using the given storage domain id for the parameters.
     *
     * @param storageId
     *            Storage domain id for the parameters
     */
    private void initializeCommand(Guid storageId) {
        initializeCommand(storageId, VolumeType.Unassigned);
    }

    private void initializeCommand(Guid storageId, VolumeType volumeType) {
        AddDiskToVmParameters parameters = createParameters();
        parameters.setStorageDomainId(storageId);
        if (volumeType == VolumeType.Preallocated) {
            parameters.setDiskInfo(createPreallocDiskImageBase());
        } else if (volumeType == VolumeType.Sparse) {
            parameters.setDiskInfo(createSparseDiskImageBase());
        }
        initializeCommand(storageId, parameters);
    }

    private void initializeCommand(Guid storageId, AddDiskToVmParameters params) {
        params.setStorageDomainId(storageId);
        command = spy(new AddDiskToVmCommand<AddDiskToVmParameters>(params));
    }

    /**
     * Mock a VM that has a disk.
     *
     * @param storageId
     *            Storage domain id of the disk.
     */
    private void mockVmWithDisk(Guid storageId) {
        DiskImage image = new DiskImage();
        image.setstorage_id(storageId);
        mockVm().addDriveToImageMap(RandomUtils.instance().nextNumericString(1), image);
    }

    /**
     * Mock a good {@link storage_pool_iso_map}.
     */
    private void mockStoragePoolIsoMap() {
        storage_pool_iso_map spim = new storage_pool_iso_map();
        doReturn(storagePoolIsoMapDAO).when(command).getStoragePoolIsoMapDao();
        when(storagePoolIsoMapDAO.get(any(StoragePoolIsoMapId.class))).thenReturn(spim);
    }

    /**
     * Mock a VM.
     */
    private VM mockVm() {
        VM vm = new VM();
        vm.setstatus(VMStatus.Down);
        vm.setstorage_pool_id(Guid.NewGuid());
        AuditLogableBaseMockUtils.mockVmDao(command, vmDAO);
        when(vmDAO.getById(command.getParameters().getVmId())).thenReturn(vm);

        return vm;
    }

    /**
     * Mock the VM networks (none).
     */
    private void mockVmNetworks() {
        doReturn(vmNetworkInterfaceDAO).when(command).getVmNetworkInterfaceDao();
    }

    /**
     * Mock a {@link storage_domains}.
     *
     * @param storageId
     *            Id of the domain.
     */
    private storage_domains mockStorageDomain(Guid storageId) {
        return mockStorageDomain(storageId, 6, 4);
    }

    private storage_domains mockStorageDomain(Guid storageId, int availableSize, int usedSize) {
        storage_domains sd = new storage_domains();
        sd.setavailable_disk_size(availableSize);
        sd.setused_disk_size(usedSize);
        doReturn(storageDomainDAO).when(command).getStorageDomainDao();
        when(storageDomainDAO.get(storageId)).thenReturn(sd);
        return sd;
    }

    /**
     * Run the canDoAction and assert that it succeeds, while printing the messages (for easier debug if test fails).
     */
    private void runAndAssertCanDoActionSuccess() {
        boolean canDoAction = command.canDoAction();
        System.out.println(command.getReturnValue().getCanDoActionMessages());
        assertTrue(canDoAction);
    }

    /**
     * @return Valid parameters for the command.
     */
    private AddDiskToVmParameters createParameters() {
        DiskImageBase image = new DiskImageBase();
        image.setdisk_type(DiskType.Data);
        image.setdisk_interface(DiskInterface.IDE);
        AddDiskToVmParameters parameters = new AddDiskToVmParameters(Guid.NewGuid(), image);
        return parameters;
    }

    private DiskImageBase createSparseDiskImageBase() {
        DiskImageBase base = new DiskImageBase();
        base.setvolume_type(VolumeType.Sparse);
        base.setdisk_type(DiskType.Data);
        base.setdisk_interface(DiskInterface.IDE);
        return base;
    }

    private DiskImageBase createPreallocDiskImageBase() {
        DiskImageBase base = new DiskImageBase();
        base.setvolume_type(VolumeType.Preallocated);
        base.setdisk_type(DiskType.Data);
        base.setdisk_interface(DiskInterface.IDE);
        base.setSizeInGigabytes(5);
        return base;
    }

    private void mockStorageDomainSpaceChecker(storage_domains domain, boolean succeeded) {
        when(StorageDomainSpaceChecker.isBelowThresholds(domain)).thenReturn(succeeded);
    }

    private void mockStorageDomainSpaceCheckerRequest(storage_domains domain, boolean succeeded) {
        when(StorageDomainSpaceChecker.hasSpaceForRequest(eq(domain), anyInt())).thenReturn(succeeded);
    }
}
