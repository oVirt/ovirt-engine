package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddDiskCommandTest extends BaseCommandTest {
    private static final Logger log = LoggerFactory.getLogger(AddDiskCommandTest.class);
    private static int MAX_BLOCK_SIZE = 8192;
    private static int MAX_PCI_SLOTS = 26;
    private static Guid vmId = Guid.newGuid();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MaxBlockDiskSize, MAX_BLOCK_SIZE)
            );

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private DiskLunMapDao diskLunMapDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private OsRepository osRepository;

    @Mock
    private DiskValidator diskValidator;

    /**
     * The command under test.
     */
    private AddDiskCommand<AddDiskParameters> command;

    @Test
    public void validateSucceedsOnDiskDomainCheckWhenNoDisks() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVm();
        mockEntities(storageId);
        runAndAssertValidateSuccess();
    }

    @Test
    public void validateFailWithUnsupportedDiskInterface() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVm();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        when(diskValidator.isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class))).thenReturn(ValidationResult.VALID);
        when(diskValidator.isDiskInterfaceSupported(any(VM.class), any(DiskVmElement.class))).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(diskValidator.isVirtIoScsiValid(any(VM.class), any(DiskVmElement.class))).thenReturn(ValidationResult.VALID);
        when(command.getDiskValidator(any(Disk.class))).thenReturn(diskValidator);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED.toString()));
    }

    @Test
    public void validateSucceedsOnDiskDomainCheckWhenEmptyStorageGuidInParams() throws Exception {
        initializeCommand(Guid.Empty);
        Guid storageId = Guid.newGuid();

        mockVmWithDisk(storageId);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        runAndAssertValidateSuccess();
    }

    @Test
    public void validateSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMatches() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVmWithDisk(storageId);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        runAndAssertValidateSuccess();
    }

    @Test
    public void validateSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMismatches() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVmWithDisk(Guid.newGuid());
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        assertTrue(command.validate());
    }

    @Test
    public void validateFailsOnNullDiskInterface() throws Exception {
        Guid storageId = Guid.newGuid();
        DiskImage image = new DiskImage();
        image.setVolumeFormat(VolumeFormat.COW);
        image.setVolumeType(VolumeType.Preallocated);
        AddDiskParameters params = new AddDiskParameters(new DiskVmElement(null, Guid.newGuid()), image);
        initializeCommand(storageId, params);
        assertFalse(command.validateInputs());
        assertTrue(command.getReturnValue().getValidationMessages().contains("VALIDATION_DISK_INTERFACE_NOT_NULL"));
    }

    @Test
    public void validateSpaceValidationSucceeds() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, VolumeType.Preallocated);

        mockVm();
        mockEntities(storageId);
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();

        assertTrue(command.validate());
    }

    @Test
    public void validateSpaceValidationFails() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, VolumeType.Sparse);

        mockVm();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        doReturn(mockStorageDomainValidatorWithoutSpace()).when(command).createStorageDomainValidator();

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.toString()));
    }

    /**
     * Validate should succeed when the requested disk space is less or equal than 'MaxBlockDiskSize'
     */
    @Test
    public void validateMaxBlockDiskSizeCheckSucceeds() {
        Guid storageId = Guid.newGuid();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(createDiskImage(MAX_BLOCK_SIZE));
        initializeCommand(storageId, parameters);

        mockVm();
        mockStorageDomain(storageId, StorageType.ISCSI);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        runAndAssertValidateSuccess();
    }

    /**
     * Validate should fail when the requested disk space is larger than 'MaxBlockDiskSize'
     */
    @Test
    public void validateMaxBlockDiskSizeCheckFails() {
        Guid storageId = Guid.newGuid();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(createDiskImage(MAX_BLOCK_SIZE * 2));
        initializeCommand(storageId, parameters);

        mockVm();
        mockStorageDomain(storageId, StorageType.ISCSI);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED.toString()));
    }

    /**
     * Validate should succeed when creating a Shareable Disk with RAW volume format
     */
    @Test
    public void validateShareableDiskVolumeFormatSucceeds() {
        DiskImage image = createShareableDiskImage();
        image.setVolumeFormat(VolumeFormat.RAW);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(image);
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);

        mockVm();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        runAndAssertValidateSuccess();
    }

    /**
     * Validate should fail when creating a Shareable Disk with COW volume format
     */
    @Test
    public void validateShareableDiskVolumeFormatFails() {
        DiskImage image = createShareableDiskImage();
        image.setVolumeFormat(VolumeFormat.COW);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(image);
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);

        mockVm();
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT.toString()));
    }

    @Test
    public void validateShareableDiskOnGlusterFails() {
        DiskImage image = createShareableDiskImage();
        image.setVolumeFormat(VolumeFormat.RAW);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(image);

        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockVm();
        mockStorageDomain(storageId, StorageType.GLUSTERFS);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();

        assertFalse(command.validate());
        assertTrue(command.getReturnValue().
                getValidationMessages().
                contains(EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN.toString()));
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
        command = spy(new AddDiskCommand<>(params, null));
        doReturn(storageDomainDao).when(command).getStorageDomainDao();
        doReturn(storagePoolIsoMapDao).when(command).getStoragePoolIsoMapDao();
        doReturn(storagePoolDao).when(command).getStoragePoolDao();
        doReturn(vmNicDao).when(command).getVmNicDao();
        doReturn(diskLunMapDao).when(command).getDiskLunMapDao();
        doReturn(diskVmElementDao).when(command).getDiskVmElementDao();
        doReturn(vmDao).when(command).getVmDao();
        doNothing().when(command).updateDisksFromDb();
        doReturn(true).when(command).checkImageConfiguration();
        doReturn(mockSnapshotValidator()).when(command).getSnapshotsValidator();
        doReturn(false).when(command).isVirtioScsiControllerAttached(any(Guid.class));
        doReturn(false).when(command).hasWatchdog(any(Guid.class));
        doReturn(false).when(command).isBalloonEnabled(any(Guid.class));
        doReturn(false).when(command).isSoundDeviceEnabled(any(Guid.class));
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(new ArrayList<>()).when(diskVmElementDao).getAllForVm(vmId);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
    }

    /**
     * Mock a VM that has a disk.
     *
     * @param storageId
     *            Storage domain id of the disk.
     */
    private void mockVmWithDisk(Guid storageId) {
        DiskImage image = new DiskImage();
        image.setId(Guid.newGuid());
        image.setStorageIds(new ArrayList<>(Arrays.asList(storageId)));
        DiskVmElement dve = new DiskVmElement(image.getId(), vmId);
        image.setDiskVmElements(Collections.singletonList(dve));
        mockVm().getDiskMap().put(image.getId(), image);
    }

    /**
     * Mock a good {@link StoragePoolIsoMap}.
     */
    private void mockStoragePoolIsoMap() {
        StoragePoolIsoMap spim = new StoragePoolIsoMap();
        when(storagePoolIsoMapDao.get(any(StoragePoolIsoMapId.class))).thenReturn(spim);
    }

    protected void mockInterfaceList() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);

        ArrayList<String> diskInterfaces = new ArrayList<>(
                Arrays.asList(new String[]{
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
        vm.setId(vmId);
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(Guid.newGuid());
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);

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
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
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
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        doReturn(MAX_PCI_SLOTS).when(osRepository).getMaxPciDevices(anyInt(), any(Version.class));
    }

    private VDS mockVds() {
        Guid vdsId = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        when(vdsDao.get(vdsId)).thenReturn(vds);
        return vds;
    }

    /**
     * Mock a {@link StoragePool}.
     */
    private StoragePool mockStoragePool() {
        Guid storagePoolId = Guid.newGuid();
        StoragePool storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);

        return storagePool;
    }

    /**
     * Mock a {@link StorageDomain}.
     *
     * @param storageId
     *            Id of the domain.
     */
    private StorageDomain mockStorageDomain(Guid storageId) {
        return mockStorageDomain(storageId, 6, 4, StorageType.UNKNOWN);
    }

    private StorageDomain mockStorageDomain(Guid storageId, StorageType storageType) {
        return mockStorageDomain(storageId, 6, 4, storageType);
    }


    private StorageDomain mockStorageDomain(Guid storageId, int availableSize, int usedSize,
            StorageType storageType) {
        StoragePool storagePool = mockStoragePool();
        Guid storagePoolId = storagePool.getId();

        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(availableSize);
        sd.setUsedDiskSize(usedSize);
        sd.setStoragePoolId(storagePoolId);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStorageType(storageType);
        when(storageDomainDao.get(storageId)).thenReturn(sd);
        when(storageDomainDao.getAllForStorageDomain(storageId)).thenReturn(Collections.singletonList(sd));
        when(storageDomainDao.getForStoragePool(storageId, storagePoolId)).thenReturn(sd);

        return sd;
    }

    /**
     * Run the validate and assert that it succeeds
     */
    private void runAndAssertValidateSuccess() {
        boolean validate = command.validate();
        log.info("{}", command.getReturnValue().getValidationMessages());
        assertTrue(validate);
    }

    /**
     * @return Valid parameters for the command.
     */
    private static AddDiskParameters createParameters() {
        DiskImage image = new DiskImage();
        DiskVmElement dve = new DiskVmElement(null, vmId);
        dve.setDiskInterface(DiskInterface.IDE);
        AddDiskParameters parameters = new AddDiskParameters(dve, image);
        return parameters;
    }

    private static DiskImage createSparseDiskImage() {
        DiskImage image = new DiskImage();
        image.setVolumeType(VolumeType.Sparse);
        return image;
    }

    private static DiskImage createPreallocDiskImage() {
        DiskImage image = new DiskImage();
        image.setVolumeType(VolumeType.Preallocated);
        image.setSizeInGigabytes(5);
        return image;
    }

    private static DiskImage createDiskImage(long sizeInGigabytes) {
        DiskImage image = new DiskImage();
        image.setSizeInGigabytes(sizeInGigabytes);
        return image;
    }

    private static DiskImage createShareableDiskImage() {
        DiskImage image = new DiskImage();
        image.setShareable(true);
        return image;
    }

    private static LunDisk createISCSILunDisk() {
        LunDisk disk = new LunDisk();
        LUNs lun = new LUNs();
        lun.setLUNId("lunid");
        lun.setLunType(StorageType.ISCSI);
        StorageServerConnections connection = new StorageServerConnections();
        connection.setIqn("a");
        connection.setConnection("0.0.0.0");
        connection.setPort("1234");
        ArrayList<StorageServerConnections> connections = new ArrayList<>();
        connections.add(connection);
        lun.setLunConnections(connections);
        disk.setLun(lun);
        disk.setId(Guid.newGuid());
        DiskVmElement dve = new DiskVmElement(disk.getId(), vmId);
        disk.setDiskVmElements(Collections.singletonList(dve));
        return disk;
    }

    @Test
    public void testIscsiLunCanBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        when(diskLunMapDao.getDiskIdByLunId(disk.getLun().getLUNId())).thenReturn(null);
        assertTrue("checkIfLunDiskCanBeAdded() failed for valid iscsi lun",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
    }

    private LunDisk createISCSILunDisk(ScsiGenericIO sgio, boolean isUsingScsiReservation, DiskInterface diskInterface) {
        LunDisk disk = createISCSILunDisk();
        disk.setSgio(sgio);
        disk.setUsingScsiReservation(isUsingScsiReservation);
        return disk;
    }

    @Test
    public void testIscsiLunCannotBeAddedIfSgioIsFilteredAndScsiReservationEnabled() {
        LunDisk disk = createISCSILunDisk(ScsiGenericIO.FILTERED, true, DiskInterface.IDE);
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        mockVm();
        mockInterfaceList();
        assertFalse("Lun disk added successfully WHILE sgio is filtered and scsi reservation is enabled",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        verifyValidationMessagesContainMessage(EngineMessage.ACTION_TYPE_FAILED_SGIO_IS_FILTERED);
    }

    @Test
    public void testIscsiLunCanBeAddedIfScsiPassthroughEnabledAndScsiReservationEnabled() {
        LunDisk disk = createISCSILunDisk(ScsiGenericIO.UNFILTERED, true, DiskInterface.IDE);
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        mockVm();
        mockInterfaceList();
        assertTrue("Failed to add Lun disk when scsi passthrough and scsi reservation are enabled",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
    }

    @Test
    public void testIscsiLunCannotBeAddedIfAddingFloatingDisk() {
        LunDisk disk = createISCSILunDisk(ScsiGenericIO.UNFILTERED, true, DiskInterface.IDE);
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        assertFalse("Floating disk with SCSI reservation set successfully added",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        verifyValidationMessagesContainMessage(EngineMessage.ACTION_TYPE_FAILED_SCSI_RESERVATION_NOT_VALID_FOR_FLOATING_DISK);
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
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyValidationMessagesContainMessage(
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_HAS_NO_VALID_TYPE));
    }

    @Test
    public void testIscsiLunDiskWithNoIqnCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        disk.getLun().getLunConnections().get(0).setIqn(null);
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null iqn",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyValidationMessagesContainMessage(
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));

        clearValidationMessages();

        disk.getLun().getLunConnections().get(0).setIqn("");
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with an empty iqn",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyValidationMessagesContainMessage(
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));
    }

    @Test
    public void testIscsiLunDiskWithNoAddressCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        disk.getLun().getLunConnections().get(0).setConnection(null);
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null address",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyValidationMessagesContainMessage(
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));

        clearValidationMessages();

        disk.getLun().getLunConnections().get(0).setConnection("");
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a empty address",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyValidationMessagesContainMessage(
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));
    }

    @Test
    public void testIscsiLunDiskWithNoPortCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);
        disk.getLun().getLunConnections().get(0).setPort(null);
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null port",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyValidationMessagesContainMessage(
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));

        clearValidationMessages();

        disk.getLun().getLunConnections().get(0).setPort("");
        assertFalse("checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a empty port",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyValidationMessagesContainMessage(
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS));
    }

    @Test
    public void testLunDiskValid() {
        VDS vds = mockVds();
        LunDisk disk = createISCSILunDisk();

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.setVdsId(vds.getId());
        initializeCommand(Guid.newGuid(), parameters);
        command.setVds(vds);

        mockVm();
        mockMaxPciSlots();
        mockInterfaceList();

        List<LUNs> luns = Collections.singletonList(disk.getLun());
        doReturn(luns).when(command).executeGetDeviceList(any(Guid.class),
                any(StorageType.class),
                any(String.class));
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testGetLunDiskSucceeds() {
        VDS vds = mockVds();
        LunDisk disk = createISCSILunDisk();
        List<LUNs> luns = Collections.singletonList(disk.getLun());
        initializeCommand(Guid.newGuid());

        doReturn(luns).when(command).executeGetDeviceList(any(Guid.class), any(StorageType.class), any(String.class));
        assertEquals(disk.getLun(), command.getLunDisk(disk.getLun(), vds));
    }

    @Test
    public void testLunDiskInvalid() {
        VDS vds = mockVds();
        LunDisk disk = createISCSILunDisk();

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.setVdsId(vds.getId());
        initializeCommand(Guid.newGuid(), parameters);
        command.setVds(vds);

        mockVm();
        mockMaxPciSlots();
        mockInterfaceList();

        List<LUNs> luns = Collections.emptyList();
        doReturn(luns).when(command).executeGetDeviceList(any(Guid.class),
                any(StorageType.class),
                any(String.class));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_INVALID);
    }

    @Test
    public void testGetLunDiskFails() {
        VDS vds = mockVds();
        LunDisk disk = createISCSILunDisk();
        List<LUNs> luns = Collections.emptyList();
        initializeCommand(Guid.newGuid());

        doReturn(luns).when(command).executeGetDeviceList(any(Guid.class), any(StorageType.class), any(String.class));
        assertNull(command.getLunDisk(disk.getLun(), vds));
    }

    @Test
    public void testAddingIDELunExceedsSlotLimit() {
        mockInterfaceList();
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.getDiskVmElement().setDiskInterface(DiskInterface.IDE);
        initializeCommand(Guid.newGuid(), parameters);
        when(diskLunMapDao.getDiskIdByLunId(disk.getLun().getLUNId())).thenReturn(null);
        VM vm = mockVm();

        mockMaxPciSlots();

        // use maximum slots for IDE - validate expected to succeed.
        mockOtherVmDisks(vm, VmCommand.MAX_IDE_SLOTS - 1, DiskInterface.IDE);
        ValidateTestUtils.runAndAssertValidateSuccess(command);

        LunDisk newDisk = createISCSILunDisk();
        newDisk.getDiskVmElementForVm(vmId).setDiskInterface(DiskInterface.IDE);
        vm.getDiskMap().put(newDisk.getId(), newDisk);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_IDE_SLOTS);
    }

    @Test
    public void testAddingPCILunExceedsSlotLimit() {
        mockInterfaceList();
        LunDisk disk = createISCSILunDisk();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.getDiskVmElement().setDiskInterface(DiskInterface.VirtIO);
        initializeCommand(Guid.newGuid(), parameters);
        when(diskLunMapDao.getDiskIdByLunId(disk.getLun().getLUNId())).thenReturn(null);
        VM vm = mockVm();
        mockMaxPciSlots();

        // use maximum slots for PCI. validate expected to succeed.
        mockOtherVmDisks(vm, MAX_PCI_SLOTS - 2, DiskInterface.VirtIO);
        ValidateTestUtils.runAndAssertValidateSuccess(command);

        LunDisk newDisk = createISCSILunDisk();
        newDisk.getDiskVmElementForVm(vmId).setDiskInterface(DiskInterface.VirtIO);
        vm.getDiskMap().put(newDisk.getId(), newDisk);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_PCI_SLOTS);
    }

    private void mockOtherVmDisks(VM vm, int numOfDisks, DiskInterface iface) {
        List<DiskVmElement> otherDisks = new ArrayList<>(numOfDisks);
        for (int i = 0; i < numOfDisks; i++) {
            DiskVmElement dve = new DiskVmElement(Guid.newGuid(), vm.getId());
            dve.setDiskInterface(iface);
            otherDisks.add(dve);
        }
        doReturn(otherDisks).when(diskVmElementDao).getAllForVm(vmId);
    }

    @Test
    public void testVirtIoScsiNotSupportedByOs() {
        DiskImage disk = new DiskImage();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);

        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        VM vm = mockVm();
        mockMaxPciSlots();

        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Arrays.asList("VirtIO")));

        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(true).when(diskValidator).isVirtioScsiControllerAttached(any(Guid.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
    }

    @Test
    public void testVirtioScsiDiskWithoutControllerCantBeAdded() {
        DiskImage disk = new DiskImage();
        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);

        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        VM vm = mockVm();
        mockMaxPciSlots();

        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(false).when(diskValidator).isVirtioScsiControllerAttached(any(Guid.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED);
    }

    @Test
    public void testDiskImageWithSgioCantBeAdded() {
        DiskImage disk = new DiskImage();
        disk.setSgio(ScsiGenericIO.UNFILTERED);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);

        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        VM vm = mockVm();
        mockMaxPciSlots();

        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(true).when(diskValidator).isVirtioScsiControllerAttached(any(Guid.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.SCSI_GENERIC_IO_IS_NOT_SUPPORTED_FOR_IMAGE_DISK);
    }

    @Test
    public void testLunDiskWithSgioCanBeAdded() {
        LunDisk disk = createISCSILunDisk();
        disk.setSgio(ScsiGenericIO.UNFILTERED);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        initializeCommand(Guid.newGuid(), parameters);

        VM vm = mockVm();
        mockMaxPciSlots();

        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Arrays.asList("VirtIO_SCSI")));

        DiskValidator diskValidator = spyDiskValidator(disk);
        doReturn(true).when(diskValidator).isVirtioScsiControllerAttached(any(Guid.class));

        mockInterfaceList();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateFailOnAddFloatingDiskWithPlugSet() {
        DiskImage disk = createDiskImage(1);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.setVmId(Guid.Empty);
        parameters.setPlugDiskToVm(true);

        initializeCommand(null, parameters);

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.CANNOT_ADD_FLOATING_DISK_WITH_PLUG_VM_SET);
    }

    @Test
    public void testValidateSuccessOnAddFloatingDiskWithPlugUnset() {
        DiskImage disk = createDiskImage(1);

        AddDiskParameters parameters = createParameters();
        parameters.setDiskInfo(disk);
        parameters.setVmId(Guid.Empty);
        parameters.setPlugDiskToVm(false);
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, parameters);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateFailReadOnlyOnInterface() {
        AddDiskParameters parameters = createParameters();
        initializeCommand(Guid.newGuid(), parameters);
        mockVm();

        doReturn(true).when(command).isDiskPassPciAndIdeLimit();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR)).
                when(diskValidator).isReadOnlyPropertyCompatibleWithInterface(parameters.getDiskVmElement());
        doReturn(diskValidator).when(command).getDiskValidator(any(Disk.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR);
    }

    @Test
    public void testValidateSucceedReadOnly() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVm();
        mockEntities(storageId);

        doReturn(true).when(command).isDiskPassPciAndIdeLimit();
        doReturn(true).when(command).checkIfImageDiskCanBeAdded(any(VM.class), any(DiskValidator.class));
        doReturn(ValidationResult.VALID).when(diskValidator).isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class));
        doReturn(diskValidator).when(command).getDiskValidator(any(Disk.class));

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private boolean verifyValidationMessagesContainMessage(EngineMessage message) {
        return command.getReturnValue()
                .getValidationMessages()
                .contains(message.toString());
    }

    private void clearValidationMessages(){
        command.getReturnValue()
        .getValidationMessages()
        .clear();
    }

    private void mockEntities(Guid storageId) {
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();
    }
}
