package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.QuotaValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddDiskCommandTest extends BaseCommandTest {
    private static final int MAX_PCI_SLOTS = 26;
    private static final Guid vmId = Guid.newGuid();

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxBlockDiskSizeInGibiBytes, 8192)
        );
    }

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    @InjectedMock
    public DiskLunMapDao diskLunMapDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    @InjectedMock
    public OsRepository osRepository;

    @Mock
    private DiskVmElementValidator diskVmElementValidator;

    @Mock
    private QuotaManager quotaManager;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private QuotaValidator quotaValidator;

    @Mock
    private ImageDao imageDao;

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private AddDiskCommand<AddDiskParameters> command = new AddDiskCommand<>(createParameters(), null);

    @Test
    public void validateSucceedsOnDiskDomainCheckWhenNoDisks() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockEntities(storageId);
        mockVm();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailWithUnsupportedDiskInterface() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        mockVm();
        when(diskVmElementValidator.isDiskInterfaceSupported(any())).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED);
    }

    @Test
    public void validateFailsOnDiskDomainCheckWhenEmptyStorageGuidInParams() {
        initializeCommand(Guid.Empty);
        Guid storageId = Guid.newGuid();

        mockVmWithDisk(storageId);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        command.setStorageDomainId(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure
                (command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_SPECIFIED);
    }

    @Test
    public void validateSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMatches() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVmWithDisk(storageId);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMismatches() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);

        mockVmWithDisk(Guid.newGuid());
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailsOnNullDiskInterface() {
        Guid storageId = Guid.newGuid();
        DiskImage image = new DiskImage();
        image.setVolumeFormat(VolumeFormat.COW);
        image.setVolumeType(VolumeType.Preallocated);
        command.getParameters().setDiskInfo(image);
        command.getParameters().setStorageDomainId(storageId);
        command.getParameters().getDiskVmElement().setDiskInterface(null);
        assertFalse(command.validateInputs());
        ValidateTestUtils.assertValidationMessages
                ("Wrong validation method", command, EngineMessage.VALIDATION_DISK_INTERFACE_NOT_NULL);
    }

    @Test
    public void validateFailsOnSizeZero() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId);
        DiskImage image = new DiskImage();
        image.setSize(0);
        command.getParameters().setDiskInfo(image);
        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();
        mockVm();
        ValidateTestUtils.runAndAssertValidateFailure
                (command, EngineMessage.ACTION_TYPE_FAILED_DISK_SIZE_ZERO);
    }

    @Test
    public void validateSpaceValidationSucceeds() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, VolumeType.Preallocated);

        mockEntities(storageId);
        mockVm();
        doReturn(mockStorageDomainValidator()).when(command).createStorageDomainValidator();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateSpaceValidationFails() {
        Guid storageId = Guid.newGuid();
        initializeCommand(storageId, VolumeType.Sparse);

        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        mockVm();
        doReturn(mockStorageDomainValidatorWithoutSpace()).when(command).createStorageDomainValidator();

        ValidateTestUtils.runAndAssertValidateFailure
                (command, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    /**
     * Validate should succeed when the requested disk space is less or equal than 'MaxBlockDiskSizeInGibiBytes'
     */
    @Test
    public void validateMaxBlockDiskSizeCheckSucceeds() {
        Guid storageId = Guid.newGuid();
        DiskImage disk = new DiskImage();
        disk.setSizeInGigabytes(Config.<Integer>getValue(ConfigValues.MaxBlockDiskSizeInGibiBytes));
        command.getParameters().setDiskInfo(disk);

        mockStorageDomain(storageId, StorageType.ISCSI);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();
        mockVm();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    /**
     * Validate should fail when the requested disk space is larger than 'MaxBlockDiskSizeInGibiBytes'
     */
    @Test
    public void validateMaxBlockDiskSizeCheckFails() {
        Guid storageId = Guid.newGuid();
        DiskImage disk = new DiskImage();
        disk.setSizeInGigabytes(Config.<Integer>getValue(ConfigValues.MaxBlockDiskSizeInGibiBytes) * 2L);
        command.getParameters().setDiskInfo(disk);

        mockStorageDomain(storageId, StorageType.ISCSI);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        mockVm();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED);
    }

    /**
     * Validate should succeed when creating a Shareable Disk with RAW volume format
     */
    @Test
    public void validateShareableDiskVolumeFormatSucceeds() {
        DiskImage image = new DiskImage();
        image.setShareable(true);
        image.setVolumeFormat(VolumeFormat.RAW);
        image.setSize(1);
        Guid storageId = Guid.newGuid();
        command.getParameters().setDiskInfo(image);

        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockInterfaceList();
        mockMaxPciSlots();
        mockVm();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
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

        mockStorageDomain(storageId);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();
        mockVm();

        ValidateTestUtils.runAndAssertValidateFailure
                (command, EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
    }

    @Test
    public void validateShareableDiskOnGlusterFails() {
        DiskImage image = new DiskImage();
        image.setShareable(true);
        image.setVolumeFormat(VolumeFormat.RAW);
        Guid storageId = Guid.newGuid();
        command.getParameters().setDiskInfo(image);

        mockVm();
        mockStorageDomain(storageId, StorageType.GLUSTERFS);
        mockStoragePoolIsoMap();
        mockMaxPciSlots();

        ValidateTestUtils.runAndAssertValidateFailure
                (command, EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
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
        disk.setSize(1);
        disk.setVolumeType(volumeType);
        parameters.setDiskInfo(disk);
        command.getParameters().setStorageDomainId(storageId);
    }

    @BeforeEach
    public void initializeMocks() {
        doNothing().when(command).updateDisksFromDb();
        doReturn(diskVmElementValidator).when(command).getDiskVmElementValidator(any(), any());
        doReturn(true).when(command).checkImageConfiguration();
        doReturn(false).when(command).isVirtioScsiControllerAttached(any());
        doReturn(false).when(command).hasWatchdog(any());
        doReturn(false).when(command).isSoundDeviceEnabled(any());
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(true).when(command).validateQuota();
        mockDiskLunMap(null);

        doAnswer(invocation -> invocation.getArguments()[0] != null ?
                    invocation.getArguments()[0] : Guid.newGuid())
                .when(quotaManager).getFirstQuotaForUser(any(), any(), any());
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
        when(storagePoolIsoMapDao.get(any())).thenReturn(spim);
    }

    protected void mockInterfaceList() {
        List<String> diskInterfaces = Arrays.asList("IDE", "VirtIO", "VirtIO_SCSI");
        when(osRepository.getDiskInterfaces(anyInt(), any(), any())).thenReturn(diskInterfaces);
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

    private static StorageDomainValidator mockStorageDomainValidatorWithoutSpace() {
        StorageDomainValidator storageDomainValidator = mockStorageDomainValidator();
        when(storageDomainValidator.hasSpaceForNewDisk(any())).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        return storageDomainValidator;
    }

    private static StorageDomainValidator mockStorageDomainValidator() {
        return mock(StorageDomainValidator.class);
    }

    private DiskValidator spyDiskValidator(Disk disk) {
        DiskValidator diskValidator = spy(new DiskValidator(disk));
        doReturn(diskValidator).when(command).getDiskValidator(disk);
        return diskValidator;
    }

    private void mockMaxPciSlots() {
        doReturn(MAX_PCI_SLOTS).when(osRepository).getMaxPciDevices(anyInt(), any());
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

    private void mockDiskLunMap(DiskLunMap diskLunMap) {
        when(diskLunMapDao.getDiskIdByLunId(any())).thenReturn(diskLunMap);
    }

    private StorageDomain mockStorageDomain(Guid storageId, StorageType storageType) {
        command.getParameters().setStorageDomainId(storageId);

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
        assertTrue(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "checkIfLunDiskCanBeAdded() failed for valid iscsi lun");
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
        assertFalse(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "Lun disk added successfully WHILE sgio is filtered and scsi reservation is enabled");
        ValidateTestUtils.assertValidationMessages("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response",
                command, EngineMessage.ACTION_TYPE_FAILED_SGIO_IS_FILTERED);
    }

    @Test
    public void testIscsiLunCanBeAddedIfScsiPassthroughEnabledAndScsiReservationEnabled() {
        LunDisk disk = createISCSILunDisk(ScsiGenericIO.UNFILTERED);
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setUsingScsiReservation(true);
        mockVm();
        mockInterfaceList();
        assertTrue(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "Failed to add Lun disk when scsi passthrough and scsi reservation are enabled");
    }

    @Test
    public void testUnknownTypeLunCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        command.getParameters().setDiskInfo(disk);
        disk.getLun().setLunType(StorageType.UNKNOWN);
        assertFalse(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "checkIfLunDiskCanBeAdded() succeded for LUN with UNKNOWN type");
        ValidateTestUtils.assertValidationMessages("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response",
                command, EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_HAS_NO_VALID_TYPE);
    }

    @Test
    public void testIscsiLunDiskWithNoIqnCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        command.getParameters().setDiskInfo(disk);
        disk.getLun().getLunConnections().get(0).setIqn(null);
        assertFalse(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null iqn");
        ValidateTestUtils.assertValidationMessages("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response",
                command, EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);

        clearValidationMessages();

        disk.getLun().getLunConnections().get(0).setIqn("");
        assertFalse(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with an empty iqn");
        ValidateTestUtils.assertValidationMessages("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response",
                command, EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
    }

    @Test
    public void testIscsiLunDiskWithNoAddressCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        command.getParameters().setDiskInfo(disk);
        disk.getLun().getLunConnections().get(0).setConnection(null);
        assertFalse(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null address");
        ValidateTestUtils.assertValidationMessages("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response",
                command, EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);

        clearValidationMessages();

        disk.getLun().getLunConnections().get(0).setConnection("");
        assertFalse(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a empty address");
        ValidateTestUtils.assertValidationMessages("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response",
                command, EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
    }

    @Test
    public void testIscsiLunDiskWithNoPortCantBeAdded() {
        LunDisk disk = createISCSILunDisk();
        command.getParameters().setDiskInfo(disk);
        disk.getLun().getLunConnections().get(0).setPort(null);
        assertFalse(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a null port");
        ValidateTestUtils.assertValidationMessages("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response",
                command, EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);

        clearValidationMessages();

        disk.getLun().getLunConnections().get(0).setPort("");
        assertFalse(command.checkIfLunDiskCanBeAdded(spyDiskValidator(disk)),
                "checkIfLunDiskCanBeAdded() succeded for ISCSI lun which LUNs has storage_server_connection with a empty port");
        ValidateTestUtils.assertValidationMessages("checkIfLunDiskCanBeAdded() failed but correct can do action hasn't been added to the return response",
                command, EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
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
        doReturn(luns).when(command).executeGetDeviceList(any(), any(), any());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testGetLunDiskSucceeds() {
        VDS vds = mockVds();
        LunDisk disk = createISCSILunDisk();
        List<LUNs> luns = Collections.singletonList(disk.getLun());
        initializeCommand(Guid.newGuid());

        doReturn(luns).when(command).executeGetDeviceList(any(), any(), any());
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
        mockVm();

        List<LUNs> luns = Collections.emptyList();
        doReturn(luns).when(command).executeGetDeviceList(any(), any(), any());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_INVALID);
    }

    @Test
    public void testGetLunDiskFails() {
        VDS vds = mockVds();
        LunDisk disk = createISCSILunDisk();
        List<LUNs> luns = Collections.emptyList();
        initializeCommand(Guid.newGuid());

        doReturn(luns).when(command).executeGetDeviceList(any(), any(), any());
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
    public void testAddingSATALunExceedsSlotLimit() {
        mockInterfaceList();
        LunDisk disk = createISCSILunDisk();

        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.SATA);
        VM vm = mockVm();

        mockMaxPciSlots();

        // use maximum slots for SATA - validate expected to succeed.
        mockOtherVmDisks(vm, VmCommand.MAX_SATA_SLOTS - 1, DiskInterface.SATA);
        ValidateTestUtils.runAndAssertValidateSuccess(command);

        LunDisk newDisk = createISCSILunDisk();
        DiskVmElement dve = new DiskVmElement(disk.getId(), vmId);
        dve.setDiskInterface(DiskInterface.SATA);
        newDisk.setDiskVmElements(Collections.singletonList(dve));

        vm.getDiskMap().put(newDisk.getId(), newDisk);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_SATA_SLOTS);
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
        mockOtherVmDisks(vm, MAX_PCI_SLOTS - 3, DiskInterface.VirtIO);
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
    public void testLunDiskWithSgioCanBeAdded() {
        LunDisk disk = createISCSILunDisk();
        disk.setSgio(ScsiGenericIO.UNFILTERED);
        command.getParameters().setDiskInfo(disk);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);
        mockVm();
        mockMaxPciSlots();

        when(osRepository.getDiskInterfaces(anyInt(), any(), any())).thenReturn(
                Collections.singletonList("VirtIO_SCSI"));

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
        disk.setSize(1);
        command.getParameters().setDiskInfo(disk);
        command.getParameters().setVmId(Guid.Empty);
        command.getParameters().setPlugDiskToVm(false);
        Guid storageId = Guid.newGuid();
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
        doReturn(true).when(command).checkIfImageDiskCanBeAdded(any(), any());

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateDiskImageNotExisting() {
        Guid imageId = Guid.newGuid();
        Guid diskId = Guid.newGuid();

        DiskImage disk = new DiskImage();

        disk.setSize(1);
        disk.setShareable(true);
        disk.setVolumeFormat(VolumeFormat.RAW);
        disk.setImageId(imageId);
        disk.setId(diskId);
        disk.setStorageTypes(Collections.singletonList(StorageType.ISCSI));

        Guid storageId = Guid.newGuid();
        mockStorageDomain(storageId);

        command.getParameters().setDiskInfo(disk);
        command.getParameters().setVmId(Guid.Empty);
        command.getParameters().setPlugDiskToVm(false);
        command.getParameters().setStorageDomainId(storageId);

        doReturn(disk.getImage()).when(imageDao).get(imageId);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_IMAGE_ALREADY_EXISTS);
    }

    @Test
    public void testValidateQuota() {
        Guid storageId = Guid.newGuid();
        command.getParameters().setStorageDomainId(storageId);

        mockVm();
        mockEntities(storageId);

        Guid quotaId = Guid.newGuid();
        ((DiskImage)command.getParameters().getDiskInfo()).setSize(1);
        ((DiskImage)command.getParameters().getDiskInfo()).setQuotaId(quotaId);

        doReturn(ValidationResult.VALID).when(quotaValidator).isValid();
        doReturn(ValidationResult.VALID).when(quotaValidator).isDefinedForStoragePool(any(Guid.class));
        doReturn(quotaValidator).when(command).createQuotaValidator(any(Guid.class));
        doCallRealMethod().when(command).validateQuota();

        ValidateTestUtils.runAndAssertValidateSuccess(command);

        verify(command, times(1)).createQuotaValidator(quotaId);
        verify(quotaValidator, times(1)).isValid();
        verify(quotaValidator, times(1)).isDefinedForStoragePool(any());
    }

    @Test
    public void testValidateFailsForPassDiscard() {
        initializeCommand(Guid.newGuid());
        mockVm();
        StoragePool storagePool = new StoragePool();
        storagePool.setCompatibilityVersion(Version.v4_2);
        command.setStoragePool(storagePool);
        command.getParameters().getDiskVmElement().setPassDiscard(true);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE))
                .when(diskVmElementValidator).isPassDiscardSupported(any());

        ValidateTestUtils.runAndAssertValidateFailure(
                command, EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE);
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
