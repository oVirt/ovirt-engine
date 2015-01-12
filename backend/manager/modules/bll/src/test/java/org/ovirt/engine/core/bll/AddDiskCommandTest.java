package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class AddDiskCommandTest {
    private static final Logger log = LoggerFactory.getLogger(AddDiskCommandTest.class);
    private static int MAX_BLOCK_SIZE = 8192;
    private static int FREE_SPACE_CRITICAL_LOW_IN_GB = 5;
    private static int MAX_PCI_SLOTS = 26;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MaxBlockDiskSize, MAX_BLOCK_SIZE),
            mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, FREE_SPACE_CRITICAL_LOW_IN_GB),
            mockConfig(ConfigValues.ShareableDiskEnabled, Version.v3_1.toString(), true),
            mockConfig(ConfigValues.VirtIoScsiEnabled, Version.v3_3.toString(), true)
            );

    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private StoragePoolIsoMapDAO storagePoolIsoMapDAO;

    @Mock
    private VmNicDao vmNicDAO;

    @Mock
    private DiskLunMapDao diskLunMapDAO;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    @Mock
    private VdsDAO vdsDAO;

    @Mock
    private OsRepository osRepository;

    @Mock
    private DiskValidator diskValidator;

    /**
     * The command under test.
     */
    private AddDiskCommand<AddDiskParameters> command;

    @Test
    public void canDoActionSucceedsOnDiskDomainCheckWhenNoDisks() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVm();
        mockEntities(storageId);
        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionFailWithUnsupportedDiskInterface() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVm();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        when(diskValidator.isReadOnlyPropertyCompatibleWithInterface()).thenReturn(ValidationResult.VALID);
        when(diskValidator.isDiskInterfaceSupported(any(VM.class))).thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(diskValidator.isVirtIoScsiValid(any(VM.class))).thenReturn(ValidationResult.VALID);
        when(command.getDiskValidator(any(Disk.class))).thenReturn(diskValidator);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED.toString()));
    }

    @Test
    public void canDoActionSucceedsOnDiskDomainCheckWhenEmptyStorageGuidInParams() throws Exception {
        initializeCommand(Guid.Empty);
        Guid storageId = Guid.newGuid();

        mockVmWithDisk(storageId);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMatches() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVmWithDisk(storageId);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        runAndAssertCanDoActionSuccess();
    }

    @Test
    public void canDoActionSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMismatches() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVmWithDisk(Guid.newGuid());
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNullDiskInterface() throws Exception {
        Guid storageId = Guid.newGuid();
        DiskImage image = new DiskImage();
        image.setvolumeFormat(VolumeFormat.COW);
        image.setVolumeType(VolumeType.Preallocated);
        AddDiskParameters params = new AddDiskParameters(Guid.newGuid(), image);
        initializeCommand(storageId, params);
        assertFalse(command.validateInputs());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains("VALIDATION.DISK_INTERFACE.NOT_NULL"));
    }

    @Test
    public void canDoActionSpaceValidationSucceeds() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, VolumeType.Preallocated);

        mockVm();
        mockEntities(storageId);
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();

        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionSpaceValidationFails() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, VolumeType.Sparse);

        mockVm();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        doReturn(mockStorageDomainValidatorWithoutSpace()).when(command).createStorageDomainValidator();

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.toString()));
    }

    /**
     * CanDoAction should succeed when the requested disk space is less or equal than 'MaxBlockDiskSize'
     */
    @Test
    public void canDoActionMaxBlockDiskSizeCheckSucceeds() {
        Guid storageId = Guid.newGuid();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(createDiskImage(MAX_BLOCK_SIZE));
        initializeCommand(storageId, parameters);

        mockVm();
        mockStorageDomain(storageId, StorageType.ISCSI);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        runAndAssertCanDoActionSuccess();
    }

    /**
     * CanDoAction should fail when the requested disk space is larger than 'MaxBlockDiskSize'
     */
    @Test
    public void canDoActionMaxBlockDiskSizeCheckFails() {
        Guid storageId = Guid.newGuid();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(createDiskImage(MAX_BLOCK_SIZE * 2));
        initializeCommand(storageId, parameters);

        mockVm();
        mockStorageDomain(storageId, StorageType.ISCSI);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED.toString()));
    }

    /**
     * CanDoAction should succeed when creating a Shareable Disk with RAW volume format
     */
    @Test
    public void canDoActionShareableDiskVolumeFormatSucceeds() {
        DiskImage image = createShareableDiskImage();
        image.setvolumeFormat(VolumeFormat.RAW);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(image);
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);

        mockVm();
        mockStorageDomain(storageId, Version.v3_1);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        runAndAssertCanDoActionSuccess();
    }

    /**
     * CanDoAction should fail when creating a Shareable Disk with COW volume format
     */
    @Test
    public void canDoActionShareableDiskVolumeFormatFails() {
        DiskImage image = createShareableDiskImage();
        image.setvolumeFormat(VolumeFormat.COW);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(image);
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);

        mockVm();
        mockStorageDomain(storageId, Version.v3_1);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT.toString()));
    }

    @Test
    public void canDoActionShareableDiskOnGlusterFails() {
        DiskImage image = createShareableDiskImage();
        image.setvolumeFormat(VolumeFormat.RAW);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(image);

        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockVm();
        mockStorageDomain(storageId, StorageType.GLUSTERFS, Version.v3_1);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().
                getCanDoActionMessages().
                contains(VdcBllMessages.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN.toString()));
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
        AddDiskParameters parameters = createParameters();
        parameters.setStorageDomainId(storageId);
        if (volumeType == VolumeType.Preallocated) {
            parameters.setDiskInfo(createPreallocDiskImage());
        } else if (volumeType == VolumeType.Sparse) {
            parameters.setDiskInfo(createSparseDiskImage());
        }
        initializeCommand(storageId, parameters);
    }

    private void initializeCommand(Guid storageId, AddDiskParameters params) {
        params.setStorageDomainId(storageId);
        command = spy(new AddDiskCommand<AddDiskParameters>(params));
        doReturn(true).when(command).acquireLockInternal();
        doReturn(storageDomainDAO).when(command).getStorageDomainDAO();
        doReturn(storagePoolIsoMapDAO).when(command).getStoragePoolIsoMapDao();
        doReturn(storagePoolDAO).when(command).getStoragePoolDAO();
        doReturn(vmNicDAO).when(command).getVmNicDao();
        doReturn(diskLunMapDAO).when(command).getDiskLunMapDao();
        doReturn(vmDAO).when(command).getVmDAO();
        doNothing().when(command).updateDisksFromDb();
        doReturn(true).when(command).checkImageConfiguration();
        doReturn(mockSnapshotValidator()).when(command).getSnapshotsValidator();
        doReturn(false).when(command).isVirtioScsiControllerAttached(any(Guid.class));
        doReturn(false).when(command).hasWatchdog(any(Guid.class));
        doReturn(false).when(command).isBalloonEnabled(any(Guid.class));
        doReturn(false).when(command).isSoundDeviceEnabled(any(Guid.class));
        doReturn(true).when(command).setAndValidateDiskProfiles();
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
    }

    /**
     * Mock a VM that has a disk.
     *
     * @param storageId
     *            Storage domain id of the disk.
     */
    private void mockVmWithDisk(Guid storageId) {
        DiskImage image = new DiskImage();
        image.setStorageIds(new ArrayList<Guid>(Arrays.asList(storageId)));
        mockVm().getDiskMap().put(image.getId(), image);
    }

    /**
     * Mock a good {@link StoragePoolIsoMap}.
     */
    private void mockStoragePoolIsoMap() {
        StoragePoolIsoMap spim = new StoragePoolIsoMap();
        when(storagePoolIsoMapDAO.get(any(StoragePoolIsoMapId.class))).thenReturn(spim);
    }

    protected void mockInterfaceList() {
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);

        ArrayList<String> diskInterfaces = new ArrayList<String>(
                Arrays.asList(new String[] {
                        "IDE",
                        "VirtIO",
                        "VirtIO_SCSI"
                }));

        when(osRepository.getDiskInterfaces(anyInt(), any(Version.class))).thenReturn(diskInterfaces);
    }

    /**
     * Mock a VM.
     */
    private VM mockVm() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(Guid.newGuid());
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(vm);

        return vm;
    }

    private SnapshotsValidator mockSnapshotValidator() {
        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        when(snapshotsValidator.vmNotDuringSnapshot(any(Guid.class))).thenReturn(ValidationResult.VALID);
        when(snapshotsValidator.vmNotInPreview(any(Guid.class))).thenReturn(ValidationResult.VALID);
        return snapshotsValidator;
    }

    private static StorageDomainValidator mockStorageDomainValidatorWithoutSpace() {
        StorageDomainValidator storageDomainValidator = mockStorageDomainValidator();
        when(storageDomainValidator.hasSpaceForNewDisk(any(DiskImage.class))).thenReturn(
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        return storageDomainValidator;
    }

    private static StorageDomainValidator mockStorageDomainValidatorWithSpace() {
        StorageDomainValidator storageDomainValidator = mockStorageDomainValidator();
        when(storageDomainValidator.hasSpaceForNewDisk(any(DiskImage.class))).thenReturn(ValidationResult.VALID);
        return storageDomainValidator;
    }

    private static StorageDomainValidator mockStorageDomainValidator() {
        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        when(storageDomainValidator.isDomainExistAndActive()).thenReturn(ValidationResult.VALID);
        when(storageDomainValidator.isDomainWithinThresholds()).thenReturn(ValidationResult.VALID);
        return storageDomainValidator;
    }

    private DiskValidator spyDiskValidator(Disk disk) {
        DiskValidator diskValidator = spy(new DiskValidator(disk));
        doReturn(diskValidator).when(command).getDiskValidator(disk);
        return diskValidator;
    }

    private void mockMaxPciSlots() {
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
        doReturn(MAX_PCI_SLOTS).when(osRepository).getMaxPciDevices(anyInt(), any(Version.class));
    }

    private VDS mockVds() {
        Guid vdsId = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        when(vdsDAO.get(vdsId)).thenReturn(vds);
        return vds;
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
        storagePool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDAO.get(storagePoolId)).thenReturn(storagePool);

        return storagePool;
    }

    /**
     * Mock a {@link StorageDomain}.
     *
     * @param storageId
     *            Id of the domain.
     */
    private StorageDomain mockStorageDomain(Guid storageId) {
        return mockStorageDomain(storageId, 6, 4, StorageType.UNKNOWN, new Version());
    }

    private StorageDomain mockStorageDomain(Guid storageId, StorageType storageType) {
        return mockStorageDomain(storageId, 6, 4, storageType, new Version());
    }

    private StorageDomain mockStorageDomain(Guid storageId, StorageType storageType, Version version) {
        return mockStorageDomain(storageId, 6, 4, storageType, version);
    }

    private StorageDomain mockStorageDomain(Guid storageId, Version version) {
        return mockStorageDomain(storageId, 6, 4, StorageType.UNKNOWN, version);
    }

    private StorageDomain mockStorageDomain(Guid storageId, int availableSize, int usedSize,
            StorageType storageType, Version version) {
        StoragePool storagePool = mockStoragePool(version);
        Guid storagePoolId = storagePool.getId();

        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(availableSize);
        sd.setUsedDiskSize(usedSize);
        sd.setStoragePoolId(storagePoolId);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStorageType(storageType);
        when(storageDomainDAO.get(storageId)).thenReturn(sd);
        when(storageDomainDAO.getAllForStorageDomain(storageId)).thenReturn(Collections.singletonList(sd));
        when(storageDomainDAO.getForStoragePool(storageId, storagePoolId)).thenReturn(sd);

        return sd;
    }

    /**
     * Run the canDoAction and assert that it succeeds
     */
    private void runAndAssertCanDoActionSuccess() {
        boolean canDoAction = command.canDoAction();
        log.info("{}", command.getReturnValue().getCanDoActionMessages());
        assertTrue(canDoAction);
    }

    /**
     * @return Valid parameters for the command.
     */
    private static AddDiskParameters createParameters() {
        DiskImage image = new DiskImage();
        image.setDiskInterface(DiskInterface.IDE);
        AddDiskParameters parameters = new AddDiskParameters(Guid.newGuid(), image);
        return parameters;
    }

    private static DiskImage createSparseDiskImage() {
        DiskImage image = new DiskImage();
        image.setVolumeType(VolumeType.Sparse);
        image.setDiskInterface(DiskInterface.IDE);
        return image;
    }

    private static DiskImage createPreallocDiskImage() {
        DiskImage image = new DiskImage();
        image.setVolumeType(VolumeType.Preallocated);
        image.setDiskInterface(DiskInterface.IDE);
        image.setSizeInGigabytes(5);
        return image;
    }

    private static DiskImage createDiskImage(long sizeInGigabytes) {
        DiskImage image = new DiskImage();
        image.setSizeInGigabytes(sizeInGigabytes);
        image.setDiskInterface(DiskInterface.IDE);
        return image;
    }

    private static DiskImage createShareableDiskImage() {
        DiskImage image = new DiskImage();
        image.setShareable(true);
        image.setDiskInterface(DiskInterface.IDE);
        return image;
    }

    private static DiskImage createVirtIoScsiDiskImage() {
        DiskImage image = new DiskImage();
        image.setDiskInterface(DiskInterface.VirtIO_SCSI);
        return image;
    }

    private static LunDisk createISCSILunDisk() {
        LunDisk disk = new LunDisk();
        LUNs lun = new LUNs();
        lun.setLUN_id("lunid");
        lun.setLunType(StorageType.ISCSI);
        StorageServerConnections connection = new StorageServerConnections();
        connection.setiqn("a");
        connection.setconnection("0.0.0.0");
        connection.setport("1234");
        ArrayList<StorageServerConnections> connections = new ArrayList<StorageServerConnections>();
        connections.add(connection);
        lun.setLunConnections(connections);
        disk.setLun(lun);
        return disk;
    }

    @Test
    public void testIscsiLunCanBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        when(diskLunMapDAO.getDiskIdByLunId(disk.getLun().getLUN_id())).thenReturn(null);
        assertTrue("checkIfLunDiskCanBeAdded() failed for valid iscsi lun",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
    }

    @Test
    public void testUnknownTypeLunCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        disk.getLun().setLunType(StorageType.UNKNOWN);
        assertFalse("checkIfLunDiskCanBeAdded() succeded for LUN with UNKNOWN type",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyCanDoActionMessagesContainMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_HAS_NO_VALID_TYPE));
    }

    @Test
    public void testIscsiLunDiskWithNoIqnCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        disk.getLun().getLunConnections().get(0).setiqn(null);
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null iqn",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyCanDoActionMessagesContainMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));

        clearCanDoActionMessages();

        disk.getLun().getLunConnections().get(0).setiqn("");
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with an empty iqn",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyCanDoActionMessagesContainMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));
    }

    @Test
    public void testIscsiLunDiskWithNoAddressCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        disk.getLun().getLunConnections().get(0).setconnection(null);
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null address",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyCanDoActionMessagesContainMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));

        clearCanDoActionMessages();

        disk.getLun().getLunConnections().get(0).setconnection("");
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a empty address",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyCanDoActionMessagesContainMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));
    }

    @Test
    public void testIscsiLunDiskWithNoPortCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        disk.getLun().getLunConnections().get(0).setport(null);
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null port",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyCanDoActionMessagesContainMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));

        clearCanDoActionMessages();

        disk.getLun().getLunConnections().get(0).setport("");
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a empty port",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyCanDoActionMessagesContainMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));
    }

    @Test
    public void testLunDiskValid() {
        VDS vds = mockVds();
        LunDisk disk = createISCSILunDisk();
        disk.setDiskInterface(DiskInterface.VirtIO);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.setVdsId(vds.getId());
        initializeCommand(Guid.newGuid(), parameters);
        command.setVds(vds);

        mockVm();
        mockMaxPciSlots();
        mockInterfaceList();

        List<LUNs> luns = Collections.singletonList(disk.getLun());
        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(luns).when(diskValidator).executeGetDeviceList(any(Guid.class), any(StorageType.class));
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void testLunDiskInvalid() {
        VDS vds = mockVds();
        LunDisk disk = createISCSILunDisk();
        disk.setDiskInterface(DiskInterface.VirtIO);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.setVdsId(vds.getId());
        initializeCommand(Guid.newGuid(), parameters);
        command.setVds(vds);

        mockVm();
        mockMaxPciSlots();
        mockInterfaceList();

        List<LUNs> luns = Collections.emptyList();
        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(luns).when(diskValidator).executeGetDeviceList(any(Guid.class), any(StorageType.class));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_INVALID);
    }

    @Test
    public void testAddingIDELunExceedsSlotLimit() {
        mockInterfaceList();
        LunDisk disk = createISCSILunDisk();
        disk.setDiskInterface(DiskInterface.IDE);
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        when(diskLunMapDAO.getDiskIdByLunId(disk.getLun().getLUN_id())).thenReturn(null);
        VM vm = mockVm();

        mockMaxPciSlots();

        // use maximum slots for IDE - canDo expected to succeed.
        fillDiskMap(disk, vm, VmCommand.MAX_IDE_SLOTS - 1);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);

        vm.getDiskMap().put(Guid.newGuid(), disk);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_EXCEEDED_MAX_IDE_SLOTS);
    }

    @Test
    public void testAddingPCILunExceedsSlotLimit() {
        mockInterfaceList();
        LunDisk disk = createISCSILunDisk();
        disk.setDiskInterface(DiskInterface.VirtIO);
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        when(diskLunMapDAO.getDiskIdByLunId(disk.getLun().getLUN_id())).thenReturn(null);
        VM vm = mockVm();
        mockMaxPciSlots();

        // use maximum slots for PCI. canDo expected to succeed.
        fillDiskMap(disk, vm, MAX_PCI_SLOTS - 2);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);

        vm.getDiskMap().put(Guid.newGuid(), disk);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_EXCEEDED_MAX_PCI_SLOTS);
    }

    @Test
    public void testVirtIoScsiNotSupportedByOs() {
        DiskImage disk = createVirtIoScsiDiskImage();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);

        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        VM vm = mockVm();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        mockMaxPciSlots();

        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Arrays.asList("VirtIO")));

        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(true).when(diskValidator).isVirtioScsiControllerAttached(any(Guid.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
    }

    @Test
    public void testVirtioScsiDiskWithoutControllerCantBeAdded() {
        DiskImage disk = createVirtIoScsiDiskImage();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);

        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        VM vm = mockVm();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        mockMaxPciSlots();

        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(false).when(diskValidator).isVirtioScsiControllerAttached(any(Guid.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED);
    }

    @Test
    public void testDiskImageWithSgioCantBeAdded() {
        DiskImage disk = createVirtIoScsiDiskImage();
        disk.setSgio(ScsiGenericIO.UNFILTERED);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);

        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        VM vm = mockVm();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        mockMaxPciSlots();

        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(true).when(diskValidator).isVirtioScsiControllerAttached(any(Guid.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.SCSI_GENERIC_IO_IS_NOT_SUPPORTED_FOR_IMAGE_DISK);
    }

    @Test
    public void testLunDiskWithSgioCanBeAdded() {
        LunDisk disk = createISCSILunDisk();
        disk.setDiskInterface(DiskInterface.VirtIO_SCSI);
        disk.setSgio(ScsiGenericIO.UNFILTERED);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);

        VM vm = mockVm();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        mockMaxPciSlots();

        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Arrays.asList("VirtIO_SCSI")));

        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(true).when(diskValidator).isVirtioScsiControllerAttached(any(Guid.class));

        mockInterfaceList();

        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void testCanDoFailOnAddFloatingDiskWithPlugSet() {
        DiskImage disk = createDiskImage(1);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.setVmId(Guid.Empty);
        parameters.setPlugDiskToVm(true);

        initializeCommand(null, parameters);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.CANNOT_ADD_FLOATING_DISK_WITH_PLUG_VM_SET);
    }

    @Test
    public void testCanDoSuccessOnAddFloatingDiskWithPlugUnset() {
        DiskImage disk = createDiskImage(1);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.setVmId(Guid.Empty);
        parameters.setPlugDiskToVm(false);
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void testCanDoFailReadOnlyOnInterface() {
        AddDiskParameters parameters = createParameters();
        initializeCommand(Guid.newGuid(), parameters);
        mockVm();

        doReturn(true).when(command).isDiskPassPciAndIdeLimit(any(Disk.class));
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR)).
                when(diskValidator).isReadOnlyPropertyCompatibleWithInterface();
        doReturn(diskValidator).when(command).getDiskValidator(any(Disk.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR);
    }

    @Test
    public void testCanDoSucceedReadOnly() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVm();
        mockEntities(storageId);

        doReturn(true).when(command).isDiskPassPciAndIdeLimit(any(Disk.class));
        doReturn(true).when(command).checkIfImageDiskCanBeAdded(any(VM.class), any(DiskValidator.class));
        doReturn(ValidationResult.VALID).when(diskValidator).isReadOnlyPropertyCompatibleWithInterface();
        doReturn(diskValidator).when(command).getDiskValidator(any(Disk.class));

        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    private void fillDiskMap(LunDisk disk, VM vm, int expectedMapSize) {
        Map<Guid, Disk> diskMap = new HashMap<Guid, Disk>();
        for (int i = 0; i < expectedMapSize; i++) {
            diskMap.put(Guid.newGuid(), disk);
        }
        vm.setDiskMap(diskMap);
    }

    private boolean verifyCanDoActionMessagesContainMessage(VdcBllMessages message) {
        return command.getReturnValue()
                .getCanDoActionMessages()
                .contains(message.toString());
    }

    private void clearCanDoActionMessages(){
        command.getReturnValue()
        .getCanDoActionMessages()
        .clear();
    }

    private void mockEntities(Guid storageId) {
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();
    }
}
