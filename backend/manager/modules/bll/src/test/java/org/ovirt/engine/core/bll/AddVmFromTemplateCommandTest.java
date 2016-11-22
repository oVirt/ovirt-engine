package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@RunWith(MockitoJUnitRunner.class)
public class AddVmFromTemplateCommandTest extends AddVmCommandTestBase<AddVmFromTemplateCommand<AddVmParameters>> {

    private static final int MAX_PCI_SLOTS = 26;

    @Override
    protected AddVmFromTemplateCommand<AddVmParameters> createCommand() {
        initVM();
        return new AddVmFromTemplateCommand<>(new AddVmParameters(vm), null);
    }

    @Override
    public void setUp() {
        super.setUp();
        doReturn(true).when(cmd).checkNumberOfMonitors();
        doReturn(true).when(cmd).validateCustomProperties(any(VmStatic.class), anyListOf(String.class));
        initCommandMethods();

        initDestSDs();
        generateStorageToDisksMap();

        cmd.init();
    }

    @Test
    public void validateSpaceAndThreshold() {
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        mockGetAllSnapshots();
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForClonedDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(anyList());
    }

    @Test
    public void validateSpaceNotEnough() throws Exception {
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        mockGetAllSnapshots();
        assertFalse(cmd.validateSpaceRequirements());
        //The following is mocked to fail, should happen only once.
        verify(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(anyList());
    }

    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).isDomainWithinThresholds();
        assertFalse(cmd.validateSpaceRequirements());
    }

    @Test
    public void create10GBVmWith11GbAvailableAndA5GbBuffer() throws Exception {
        mockStorageDomainDaoGetForStoragePool();
        mockVerifyAddVM();
        mockMaxPciSlots();

        mockOsRepositoryGraphics(0, Version.v4_0, new Pair<>(GraphicsType.SPICE, DisplayType.qxl));
        mockGraphicsDevices(vm.getId());

        mockStorageDomainDaoGetAllForStoragePool();
        mockUninterestingMethods();
        mockGetAllSnapshots();

        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(anyList());

        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void canAddVmWithVirtioScsiControllerNotSupportedOs() {
        mockStorageDomainDaoGetForStoragePool();
        mockVerifyAddVM();
        mockMaxPciSlots();
        mockStorageDomainDaoGetAllForStoragePool();
        mockUninterestingMethods();
        mockDisplayTypes(vm.getOs());
        mockGraphicsDevices(vm.getId());

        cmd.getParameters().setVirtioScsiEnabled(true);
        when(osRepository.isSoundDeviceEnabled(any(Integer.class), any(Version.class))).thenReturn(true);
        when(osRepository.getArchitectureFromOS(any(Integer.class))).thenReturn(ArchitectureType.x86_64);
        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Collections.singletonList("VirtIO")));
        mockGetAllSnapshots();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForClonedDisks(anyList());

        cmd.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_DOES_NOT_SUPPORT_VIRTIO_SCSI);
    }

    @Test
    public void testUnsupportedCpus() {
        vm.setVmOs(OsRepository.DEFAULT_X86_OS);

        mockStorageDomainDaoGetForStoragePool();
        mockVerifyAddVM();
        mockMaxPciSlots();
        mockStorageDomainDaoGetAllForStoragePool();
        mockDisplayTypes(vm.getOs());
        mockUninterestingMethods();
        mockGetAllSnapshots();
        when(osRepository.getArchitectureFromOS(0)).thenReturn(ArchitectureType.x86_64);
        doReturn(storagePool).when(cmd).getStoragePool();

        // prepare the mock values
        Map<Pair<Integer, Version>, Set<String>> unsupported = new HashMap<>();
        Set<String> value = new HashSet<>();
        value.add(CPU_ID);
        unsupported.put(new Pair<>(vm.getVmOsId(), cluster.getCompatibilityVersion()), value);

        when(osRepository.isCpuSupported(vm.getVmOsId(), cluster.getCompatibilityVersion(), CPU_ID)).thenReturn(false);
        when(osRepository.getUnsupportedCpus()).thenReturn(unsupported);

        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForClonedDisks(anyList());

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.CPU_TYPE_UNSUPPORTED_FOR_THE_GUEST_OS);
    }

    private void mockVerifyAddVM() {
        doReturn(true).when(cmd).verifyAddVM(anyListOf(String.class), anyInt());
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

        when(vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.GRAPHICS)).thenReturn(Collections.singletonList(graphicsDevice));
    }

    private void mockDisplayTypes(int osId) {
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<>());
        displayTypeMap.get(osId).put(null, Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
    }
}
