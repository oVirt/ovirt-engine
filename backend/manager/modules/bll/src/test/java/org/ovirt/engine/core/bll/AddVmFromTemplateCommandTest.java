package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@RunWith(MockitoJUnitRunner.class)
public class AddVmFromTemplateCommandTest extends AddVmCommandTest {

    private static final int MAX_PCI_SLOTS = 26;

    /**
     * The command under test.
     */
    protected AddVmFromTemplateCommand<AddVmParameters> command;

    @Override
    @Test
    public void validateSpaceAndThreshold() {
        mockOsRepository();
        initCommand();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        mockGetAllSnapshots();
        assertTrue(command.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForClonedDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(anyList());
    }

    @Override
    @Test
    public void validateSpaceNotEnough() throws Exception {
        mockOsRepository();
        initCommand();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        mockGetAllSnapshots();
        assertFalse(command.validateSpaceRequirements());
        //The following is mocked to fail, should happen only once.
        verify(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(anyList());
    }

    @Override
    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
        mockOsRepository();
        initCommand();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        assertFalse(command.validateSpaceRequirements());
    }

    @Override
    protected List<DiskImage> createDiskSnapshot(Guid diskId, int numOfImages) {
        List<DiskImage> disksList = new ArrayList<>();
        for (int i = 0; i < numOfImages; ++i) {
            DiskImage diskImage = new DiskImage();
            diskImage.setActive(false);
            diskImage.setId(diskId);
            diskImage.setImageId(Guid.newGuid());
            diskImage.setParentId(Guid.newGuid());
            diskImage.setImageStatus(ImageStatus.OK);
            disksList.add(diskImage);
        }
        return disksList;
    }

    @Test
    public void create10GBVmWith11GbAvailableAndA5GbBuffer() throws Exception {
        VM vm = createVm();
        AddVmFromTemplateCommand<AddVmParameters> cmd = createVmFromTemplateCommand(vm);

        mockStorageDomainDaoGetForStoragePool();
        mockClusterDaoReturnCluster();
        mockVmTemplateDaoReturnVmTemplate();
        mockVerifyAddVM(cmd);
        mockMaxPciSlots();

        mockOsRepository();
        mockOsRepositoryGraphics(0, Version.v4_0, new Pair<>(GraphicsType.SPICE, DisplayType.qxl));
        mockGraphicsDevices(vm.getId());

        mockStorageDomainDaoGetAllStoragesForPool(AVAILABLE_SPACE_GB);
        mockUninterestingMethods(cmd);
        mockGetAllSnapshots(cmd);
        doReturn(createStoragePool()).when(cmd).getStoragePool();

        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void canAddCloneVmFromSnapshotSnapshotDoesNotExist() {
        final int domainSizeGB = 15;
        final Guid sourceSnapshotId = Guid.newGuid();
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd =
                setupCanAddVmFromSnapshotTests(domainSizeGB, sourceSnapshotId);
        cmd.getVm().setName("vm1");
        mockNonInterestingMethodsForCloneVmFromSnapshot(cmd);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void canAddCloneVmFromSnapshotNoConfiguration() {
        final int domainSizeGB = 15;
        final Guid sourceSnapshotId = Guid.newGuid();
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd =
                setupCanAddVmFromSnapshotTests(domainSizeGB, sourceSnapshotId);
        cmd.getVm().setName("vm1");
        mockNonInterestingMethodsForCloneVmFromSnapshot(cmd);
        SnapshotsValidator sv = spy(new SnapshotsValidator());
        doReturn(ValidationResult.VALID).when(sv).vmNotDuringSnapshot(any(Guid.class));
        doReturn(sv).when(cmd).createSnapshotsValidator();
        when(snapshotDao.get(sourceSnapshotId)).thenReturn(new Snapshot());
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION);
    }

    @Test
    public void canAddVmWithVirtioScsiControllerNotSupportedOs() {
        VM vm = createVm();
        AddVmFromTemplateCommand<AddVmParameters> cmd = createVmFromTemplateCommand(vm);
        cluster = createCluster();
        vm.setClusterId(cluster.getId());

        mockStorageDomainDaoGetForStoragePool();
        mockVmTemplateDaoReturnVmTemplate();
        mockVerifyAddVM(cmd);
        mockMaxPciSlots();
        mockStorageDomainDaoGetAllStoragesForPool(20);
        mockUninterestingMethods(cmd);
        mockDisplayTypes(vm.getOs());
        mockGraphicsDevices(vm.getId());
        doReturn(true).when(cmd).checkCpuSockets();

        doReturn(cluster).when(cmd).getCluster();
        doReturn(createStoragePool()).when(cmd).getStoragePool();
        cmd.getParameters().setVirtioScsiEnabled(true);
        when(osRepository.isSoundDeviceEnabled(any(Integer.class), any(Version.class))).thenReturn(true);
        when(osRepository.getArchitectureFromOS(any(Integer.class))).thenReturn(ArchitectureType.x86_64);
        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Collections.singletonList("VirtIO")));
        mockGetAllSnapshots(cmd);

        cmd.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_DOES_NOT_SUPPORT_VIRTIO_SCSI);
    }

    @Test
    public void testUnsupportedCpus() {
        // prepare a command to pass validate
        VM vm = createVm();
        vm.setVmOs(OsRepository.DEFAULT_X86_OS);
        cluster = createCluster();
        vm.setClusterId(cluster.getId());
        when(clusterDao.get(cluster.getId())).thenReturn(cluster);

        AddVmFromTemplateCommand<AddVmParameters> cmd = createVmFromTemplateCommand(vm);

        mockStorageDomainDaoGetForStoragePool();
        mockVmTemplateDaoReturnVmTemplate();
        mockVerifyAddVM(cmd);
        mockMaxPciSlots();
        mockStorageDomainDaoGetAllStoragesForPool(20);
        mockDisplayTypes(vm.getOs());
        mockUninterestingMethods(cmd);
        mockGetAllSnapshots(cmd);
        when(osRepository.getArchitectureFromOS(0)).thenReturn(ArchitectureType.x86_64);
        doReturn(createStoragePool()).when(cmd).getStoragePool();

        // prepare the mock values
        Map<Pair<Integer, Version>, Set<String>> unsupported = new HashMap<>();
        Set<String> value = new HashSet<>();
        value.add(CPU_ID);
        unsupported.put(new Pair<>(vm.getVmOsId(), cluster.getCompatibilityVersion()), value);

        when(osRepository.isCpuSupported(vm.getVmOsId(), cluster.getCompatibilityVersion(), CPU_ID)).thenReturn(false);
        when(osRepository.getUnsupportedCpus()).thenReturn(unsupported);

        ValidateTestUtils.runAndAssertValidateFailure(
                cmd,
                EngineMessage.CPU_TYPE_UNSUPPORTED_FOR_THE_GUEST_OS);
    }

    protected AddVmFromTemplateCommand<AddVmParameters> createVmFromTemplateCommand(VM vm) {
        AddVmParameters param = new AddVmParameters();
        param.setVm(vm);
        AddVmFromTemplateCommand<AddVmParameters> concrete = new AddVmFromTemplateCommand<>(param, null);
        AddVmFromTemplateCommand<AddVmParameters> result = spy(concrete);
        doReturn(true).when(result).checkNumberOfMonitors();
        doReturn(createVmTemplate()).when(result).getVmTemplate();
        doReturn(true).when(result).validateCustomProperties(any(VmStatic.class), anyListOf(String.class));
        doNothing().when(result).initTemplateDisks();
        mockDaos(result);
        mockBackend(result);
        mockVmDeviceUtils(result);
        initCommandMethods(result);
        result.macPoolPerCluster = this.macPoolPerCluster;
        result.init();
        return result;
    }

    private void mockStorageDomainDaoGetAllStoragesForPool(int domainSpaceGB) {
        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.singletonList(createStorageDomain(domainSpaceGB)));
    }

    private void mockStorageDomainDaoGetForStoragePool() {
        mockStorageDomainDaoGetForStoragePool(AVAILABLE_SPACE_GB);
    }

    private void mockGetAllSnapshots(AddVmFromTemplateCommand<AddVmParameters> command) {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            DiskImage arg = (DiskImage) args[0];
            return createDiskSnapshot(arg.getId(), 3);
        }).when(command).getAllImageSnapshots(any(DiskImage.class));
    }

    private static void mockVerifyAddVM(AddVmCommand<?> cmd) {
        doReturn(true).when(cmd).verifyAddVM(anyListOf(String.class), anyInt());
    }

    private void mockClusterDaoReturnCluster() {
        when(clusterDao.get(any(Guid.class))).thenReturn(createCluster());
    }

    private void mockMaxPciSlots() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        doReturn(MAX_PCI_SLOTS).when(osRepository).getMaxPciDevices(anyInt(), any(Version.class));
    }

    private void mockOsRepositoryGraphics(int osId, Version ver, Pair<GraphicsType, DisplayType> supportedGraphicsAndDisplay) {
        Map<Version, List<Pair<GraphicsType, DisplayType>>> value = new HashMap<>();
        value.put(ver, Collections.singletonList(supportedGraphicsAndDisplay));

        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> g = new HashMap<>();
        g.put(osId, value);
        when(osRepository.getGraphicsAndDisplays()).thenReturn(g);
    }

    private void mockGraphicsDevices(Guid vmId) {
        VmDevice graphicsDevice = new GraphicsDevice(VmDeviceType.SPICE);
        graphicsDevice.setDeviceId(Guid.Empty);
        graphicsDevice.setVmId(vmId);

        when(deviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.GRAPHICS)).thenReturn(Collections.singletonList(graphicsDevice));
    }

    private void mockDisplayTypes(int osId) {
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<>());
        displayTypeMap.get(osId).put(null, Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
    }

    private void mockGetAllSnapshots() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            DiskImage arg = (DiskImage) args[0];
            return createDiskSnapshot(arg.getId(), 3);
        }).when(command).getAllImageSnapshots(any(DiskImage.class));
    }

    private void initCommand() {
        VM vm = createVm();
        command = createVmFromTemplateCommand(vm);
        generateStorageToDisksMap(command);
        initDestSDs(command);
        storageDomainValidator = mock(StorageDomainValidator.class);
    }
}
