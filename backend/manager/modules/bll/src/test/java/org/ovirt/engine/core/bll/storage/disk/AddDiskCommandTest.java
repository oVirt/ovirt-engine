package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
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
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddDiskCommandTest extends BaseCommandTest {
    private static final Logger log = LoggerFactory.getLogger(AddDiskCommandTest.class);
    private static int MAX_PCI_SLOTS = 26;
    private static Guid vmId = Guid.newGuid();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

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
    private QuotaDao quotaDao;

    @Mock
    private OsRepository osRepository;

    @Mock
    private VmDeviceUtils vmDeviceUtils;

    @Mock
    private DiskVmElementValidator diskVmElementValidator;

    @Mock
    private QuotaManager quotaManager;

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private AddDiskCommand<AddDiskParameters> command = new AddDiskCommand<>(createParameters(), null);

    @Test
    public void validateSucceedsOnDiskDomainCheckWhenNoDisks() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockEntities(storageId);
        runAndAssertValidateSuccess();
    }

    @Test
    public void validateFailWithUnsupportedDiskInterface() throws Exception {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        when(diskVmElementValidator.isReadOnlyPropertyCompatibleWithInterface()).thenReturn(ValidationResult.VALID);
        when(diskVmElementValidator.isDiskInterfaceSupported(any(VM.class))).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(diskVmElementValidator.isVirtIoScsiValid(any(VM.class))).thenReturn(ValidationResult.VALID);
        when(command.getDiskVmElementValidator(any(Disk.class), any(DiskVmElement.class))).thenReturn(diskVmElementValidator);

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
        command.getParameters().setDiskInfo(image);
        command.getParameters().setStorageDomainId(storageId);
        command.getParameters().getDiskVmElement().setDiskInterface(null);
        assertFalse(command.validateInputs());
        assertTrue(command.getReturnValue().getValidationMessages().contains("VALIDATION_DISK_INTERFACE_NOT_NULL"));
    }

    @Test
    public void validateSpaceValidationSucceeds() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, VolumeType.Preallocated);

        mockEntities(storageId);
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();

        assertTrue(command.validate());
    }

    @Test
    public void validateSpaceValidationFails() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, VolumeType.Sparse);

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
        DiskImage disk = new DiskImage();
        disk.setSizeInGigabytes(Config.<Integer>getValue(ConfigValues.MaxBlockDiskSize));
        command.getParameters().setStorageDomainId(storageId);
        command.getParameters().setDiskInfo(disk);

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
        DiskImage disk = new DiskImage();
        disk.setSizeInGigabytes(Config.<Integer>getValue(ConfigValues.MaxBlockDiskSize) * 2L);
        command.getParameters().setStorageDomainId(storageId);
        command.getParameters().setDiskInfo(disk);

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
        DiskImage image = new DiskImage();
        image.setShareable(true);
        image.setVolumeFormat(VolumeFormat.RAW);
        Guid storageId = Guid.newGuid();
        command.getParameters().setDiskInfo(image);
        command.getParameters().setStorageDomainId(storageId);

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
        DiskImage image = new DiskImage();
        image.setShareable(true);
        image.setVolumeFormat(VolumeFormat.COW);
        Guid storageId = Guid.newGuid();
        command.getParameters().setDiskInfo(image);
        command.getParameters().setStorageDomainId(storageId);

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
        DiskImage image = new DiskImage();
        image.setShareable(true);
        image.setVolumeFormat(VolumeFormat.RAW);
        Guid storageId = Guid.newGuid();
        command.getParameters().setDiskInfo(image);
        command.getParameters().setStorageDomainId(storageId);

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
        AddDiskParameters parameters = command.getParameters();
        DiskImage disk = new DiskImage();
        disk.setVolumeType(volumeType);
        parameters.setDiskInfo(disk);
        command.getParameters().setStorageDomainId(storageId);
    }

    @Before
    public void initializeMocks() {
        doNothing().when(command).updateDisksFromDb();
        doReturn(true).when(command).checkImageConfiguration();
        doReturn(mockSnapshotValidator()).when(command).getSnapshotsValidator();
        doReturn(false).when(command).isVirtioScsiControllerAttached(any(Guid.class));
        doReturn(false).when(command).hasWatchdog(any(Guid.class));
        doReturn(false).when(command).isBalloonEnabled(any(Guid.class));
        doReturn(false).when(command).isSoundDeviceEnabled(any(Guid.class));
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(new ArrayList<>()).when(diskVmElementDao).getAllForVm(vmId);
        doReturn(true).when(command).validateQuota();

        doAnswer(invocation -> invocation.getArguments()[0] != null ?
                    invocation.getArguments()[0] : Guid.newGuid())
                .when(quotaManager).getDefaultQuotaIfNull(any(Guid.class), any(Guid.class));

        doReturn(ValidationResult.VALID).when(diskVmElementValidator).isPassDiscardSupported(any(Guid.class));
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);

        injectorRule.bind(VmDeviceUtils.class, vmDeviceUtils);
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
        image.setStorageIds(new ArrayList<>(Collections.singletonList(storageId)));
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
        return mockStorageDomain(storageId, StorageType.UNKNOWN);
    }

    private StorageDomain mockStorageDomain(Guid storageId, StorageType storageType) {
        StoragePool storagePool = mockStoragePool();
        Guid storagePoolId = storagePool.getId();

        StorageDomain sd = new StorageDomain();
        sd.setId(storageId);
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
        return new AddDiskParameters(dve, image);
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
        return disk;
    }

    @Test
    public void testIscsiLunCanBeAdded() {
        LunDisk disk = createISCSILunDisk();
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setUsingScsiReservation(false);
        assertTrue("checkIfLunDiskCanBeAdded() failed for valid iscsi lun",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
    }

    private LunDisk createISCSILunDisk(ScsiGenericIO sgio) {
        LunDisk disk = createISCSILunDisk();
        disk.setSgio(sgio);
        return disk;
    }

    @Test
    public void testIscsiLunCannotBeAddedIfSgioIsFilteredAndScsiReservationEnabled() {
        LunDisk disk = createISCSILunDisk(ScsiGenericIO.FILTERED);
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setUsingScsiReservation(true);
        mockVm();
        mockInterfaceList();
        assertFalse("Lun disk added successfully WHILE sgio is filtered and scsi reservation is enabled",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        verifyValidationMessagesContainMessage(EngineMessage.ACTION_TYPE_FAILED_SGIO_IS_FILTERED);
    }

    @Test
    public void testIscsiLunCanBeAddedIfScsiPassthroughEnabledAndScsiReservationEnabled() {
        LunDisk disk = createISCSILunDisk(ScsiGenericIO.UNFILTERED);
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setUsingScsiReservation(true);
        mockVm();
        mockInterfaceList();
        assertTrue("Failed to add Lun disk when scsi passthrough and scsi reservation are enabled",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
    }

    @Test
    public void testUnknownTypeLunCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        command.getParameters().setDiskInfo(disk);
        disk.getLun().setLunType(StorageType.UNKNOWN);
        assertFalse("checkIfLunDiskCanBeAdded() succeded for LUN with UNKNOWN type",
                command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)));
        assertTrue("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response", verifyValidationMessagesContainMessage(
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_HAS_NO_VALID_TYPE));
    }

    @Test
    public void testIscsiLunDiskWithNoIqnCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        command.getParameters().setDiskInfo(disk);
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
        command.getParameters().setDiskInfo(disk);
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
        command.getParameters().setDiskInfo(disk);
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

        command.getParameters().setDiskInfo(disk);
        command.getParameters().setVdsId(vds.getId());
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

        command.getParameters().setDiskInfo(disk);
        command.getParameters().setVdsId(vds.getId());
        command.setVds(vds);

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

        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.IDE);

        VM vm = mockVm();

        mockMaxPciSlots();

        // use maximum slots for IDE - validate expected to succeed.
        mockOtherVmDisks(vm, VmCommand.MAX_IDE_SLOTS - 1, DiskInterface.IDE);
        ValidateTestUtils.runAndAssertValidateSuccess(command);

        LunDisk newDisk = createISCSILunDisk();
        DiskVmElement dve = new DiskVmElement(disk.getId(), vmId);
        dve.setDiskInterface(DiskInterface.IDE);
        newDisk.setDiskVmElements(Collections.singletonList(dve));

        vm.getDiskMap().put(newDisk.getId(), newDisk);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_IDE_SLOTS);
    }

    @Test
    public void testAddingPCILunExceedsSlotLimit() {
        mockInterfaceList();
        LunDisk disk = createISCSILunDisk();
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO);

        VM vm = mockVm();
        mockMaxPciSlots();

        // use maximum slots for PCI. validate expected to succeed.
        mockOtherVmDisks(vm, MAX_PCI_SLOTS - 2, DiskInterface.VirtIO);
        ValidateTestUtils.runAndAssertValidateSuccess(command);

        LunDisk newDisk = createISCSILunDisk();
        DiskVmElement dve = new DiskVmElement(disk.getId(), vmId);
        dve.setDiskInterface(DiskInterface.VirtIO);
        newDisk.setDiskVmElements(Collections.singletonList(dve));
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
        Guid storageId = Guid.newGuid();
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);
        command.getParameters().setStorageDomainId(storageId);

        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        mockVm();
        mockMaxPciSlots();

        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Collections.singletonList("VirtIO")));

        doReturn(true).when(vmDeviceUtils).hasVirtioScsiController(any(Guid.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
    }

    @Test
    public void testVirtioScsiDiskWithoutControllerCantBeAdded() {
        DiskImage disk = new DiskImage();
        Guid storageId = Guid.newGuid();
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);
        command.getParameters().setStorageDomainId(storageId);

        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        mockVm();
        mockMaxPciSlots();

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED);
    }

    @Test
    public void testDiskImageWithSgioCantBeAdded() {
        DiskImage disk = new DiskImage();
        disk.setSgio(ScsiGenericIO.UNFILTERED);
        Guid storageId = Guid.newGuid();
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);
        command.getParameters().setStorageDomainId(storageId);

        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        mockMaxPciSlots();

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.SCSI_GENERIC_IO_IS_NOT_SUPPORTED_FOR_IMAGE_DISK);
    }

    @Test
    public void testLunDiskWithSgioCanBeAdded() {
        LunDisk disk = createISCSILunDisk();
        disk.setSgio(ScsiGenericIO.UNFILTERED);
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);

        mockVm();
        mockMaxPciSlots();

        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Collections.singletonList("VirtIO_SCSI")));

        doReturn(true).when(vmDeviceUtils).hasVirtioScsiController(any(Guid.class));

        mockInterfaceList();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateFailOnAddFloatingDiskWithPlugSet() {
        DiskImage disk = new DiskImage();

        command.getParameters().setDiskInfo(disk);
        command.getParameters().setVmId(Guid.Empty);
        command.getParameters().setPlugDiskToVm(true);

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.CANNOT_ADD_FLOATING_DISK_WITH_PLUG_VM_SET);
    }

    @Test
    public void testValidateSuccessOnAddFloatingDiskWithPlugUnset() {
        DiskImage disk = new DiskImage();

        command.getParameters().setDiskInfo(disk);
        command.getParameters().setVmId(Guid.Empty);
        command.getParameters().setPlugDiskToVm(false);
        Guid storageId = Guid.newGuid();
        command.getParameters().setStorageDomainId(storageId);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateFailReadOnlyOnInterface() {
        command.getParameters().setStorageDomainId(Guid.newGuid());
        mockVm();

        doReturn(true).when(command).isDiskPassPciAndIdeLimit();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR)).
                when(diskVmElementValidator).isReadOnlyPropertyCompatibleWithInterface();
        doReturn(diskVmElementValidator).when(command).getDiskVmElementValidator(any(Disk.class), any(DiskVmElement.class));

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
        doReturn(true).when(command).checkIfImageDiskCanBeAdded(any(VM.class), any(DiskVmElementValidator.class));
        doReturn(ValidationResult.VALID).when(diskVmElementValidator).isReadOnlyPropertyCompatibleWithInterface();
        doReturn(diskVmElementValidator).when(command).getDiskVmElementValidator(any(Disk.class), any(DiskVmElement.class));

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testExistingQuota() {
        Quota quota = new Quota();
        quota.setId(Guid.newGuid());

        DiskImage img = new DiskImage();
        img.setQuotaId(quota.getId());

        command.getParameters().setDiskInfo(img);

        Guid storageId = Guid.newGuid();
        command.getParameters().setStorageDomainId(storageId);

        StoragePool pool = mockStoragePool();
        command.setStoragePoolId(pool.getId());
        quota.setStoragePoolId(pool.getId());

        mockVm();
        mockEntities(storageId);

        when(quotaDao.getById(quota.getId())).thenReturn(quota);

        doCallRealMethod().when(command).validateQuota();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testNonExistingQuota() {
        DiskImage img = new DiskImage();
        img.setQuotaId(Guid.newGuid());

        AddDiskParameters params = createParameters();
        params.setDiskInfo(img);
        command.getParameters().setDiskInfo(img);

        Guid storageId = Guid.newGuid();
        command.getParameters().setStorageDomainId(storageId);

        mockEntities(storageId);

        doCallRealMethod().when(command).validateQuota();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
    }

    @Test
    public void testValidateFailsForPassDiscard() {
        initializeCommand(Guid.newGuid());
        mockVm();
        StoragePool storagePool = new StoragePool();
        storagePool.setCompatibilityVersion(Version.v4_1);
        command.setStoragePool(storagePool);
        command.getParameters().getDiskVmElement().setPassDiscard(true);
        doReturn(diskVmElementValidator).when(command).getDiskVmElementValidator(
                any(Disk.class), any(DiskVmElement.class));
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE))
                .when(diskVmElementValidator).isPassDiscardSupported(any(Guid.class));

        ValidateTestUtils.runAndAssertValidateFailure(
                command, EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE);
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
