package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.Strict;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(Strict.class)
public class AddVmFromTemplateCommandTest extends AddVmCommandTestBase<AddVmFromTemplateCommand<AddVmParameters>> {

    private static final int MAX_PCI_SLOTS = 26;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
        mockConfig(ConfigValues.MaxVmNameLength, 64),
        mockConfig(ConfigValues.SupportedClusterLevels, new HashSet<>(Collections.singletonList(new Version(3, 0))))
    );

    @Override
    protected AddVmFromTemplateCommand<AddVmParameters> createCommand() {
        initVM();
        return new AddVmFromTemplateCommand<>(new AddVmParameters(vm), null);
    }

    @Override
    public void setUp() {
        super.setUp();
        doNothing().when(cmd).initTemplateDisks();
        doReturn(true).when(cmd).checkNumberOfMonitors();
        doReturn(true).when(cmd).validateCustomProperties(any(), any());
        initCommandMethods();

        initDestSDs();
        generateStorageToDisksMap();

        cmd.init();
    }

    @Test
    public void validateSpaceAndThreshold() {
        mockGetAllSnapshots();
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForClonedDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(any());
    }

    @Test
    public void validateSpaceNotEnough() throws Exception {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(any());
        mockGetAllSnapshots();
        assertFalse(cmd.validateSpaceRequirements());
        //The following is mocked to fail, should happen only once.
        verify(storageDomainValidator).hasSpaceForClonedDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(any());
    }

    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).isDomainWithinThresholds();
        assertFalse(cmd.validateSpaceRequirements());
    }

    @Test
    public void create10GBVmWith11GbAvailableAndA5GbBuffer() throws Exception {
        doReturn(true).when(cmd).areParametersLegal(any());
        doReturn(Collections.emptyList()).when(cmd).getVmInterfaces();
        doReturn(Collections.emptyList()).when(cmd).getDiskVmElements();
        mockMaxPciSlots();

        mockStorageDomainDaoGetAllForStoragePool();
        mockGetAllSnapshots();

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(any());

        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void canAddVmWithVirtioScsiControllerNotSupportedOs() {
        when(cpuFlagsManagerHandler.getCpuId(any(), any())).thenReturn(CPU_ID);
        when(osRepository.isCpuSupported(anyInt(), any(), any())).thenReturn(true);
        doReturn(true).when(cmd).areParametersLegal(any());
        doReturn(Collections.emptyList()).when(cmd).getVmInterfaces();
        doReturn(Collections.emptyList()).when(cmd).getDiskVmElements();
        mockStorageDomainDaoGetAllForStoragePool();
        mockMaxPciSlots();
        mockStorageDomainDaoGetAllForStoragePool();

        cmd.getParameters().setVirtioScsiEnabled(true);
        when(osRepository.isSoundDeviceEnabled(anyInt(), any())).thenReturn(true);
        when(osRepository.getArchitectureFromOS(anyInt())).thenReturn(ArchitectureType.x86_64);
        when(osRepository.getDiskInterfaces(anyInt(), any())).thenReturn(
                new ArrayList<>(Collections.singletonList("VirtIO")));
        mockGetAllSnapshots();

        cmd.initEffectiveCompatibilityVersion();

        Map<String, String> migrationMap = new HashMap<>();
        migrationMap.put("undefined", "true");
        migrationMap.put("x86", "true");
        migrationMap.put("ppc", "true");
        mcr.mockConfigValue(ConfigValues.IsMigrationSupported, cmd.getEffectiveCompatibilityVersion(), migrationMap);
        mcr.mockConfigValue(ConfigValues.MaxNumOfVmCpus, cmd.getEffectiveCompatibilityVersion(), 16);
        mcr.mockConfigValue(ConfigValues.MaxNumOfVmSockets, cmd.getEffectiveCompatibilityVersion(), 16);
        mcr.mockConfigValue(ConfigValues.MaxNumOfCpuPerSocket, cmd.getEffectiveCompatibilityVersion(), 16);
        mcr.mockConfigValue(ConfigValues.MaxNumOfThreadsPerCpu, cmd.getEffectiveCompatibilityVersion(), 8);

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_DOES_NOT_SUPPORT_VIRTIO_SCSI);
    }

    @Test
    public void testUnsupportedCpus() {
        when(cpuFlagsManagerHandler.getCpuId(any(), any())).thenReturn(CPU_ID);
        doReturn(true).when(cmd).areParametersLegal(any());
        doReturn(Collections.emptyList()).when(cmd).getVmInterfaces();
        doReturn(Collections.emptyList()).when(cmd).getDiskVmElements();
        vm.setVmOs(OsRepository.DEFAULT_X86_OS);

        mockMaxPciSlots();
        mockStorageDomainDaoGetAllForStoragePool();
        mockGetAllSnapshots();
        when(osRepository.getArchitectureFromOS(0)).thenReturn(ArchitectureType.x86_64);
        doReturn(storagePool).when(cmd).getStoragePool();

        when(osRepository.isCpuSupported(vm.getVmOsId(), cluster.getCompatibilityVersion(), CPU_ID)).thenReturn(false);

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.CPU_TYPE_UNSUPPORTED_FOR_THE_GUEST_OS);
    }

    private void mockMaxPciSlots() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        doReturn(MAX_PCI_SLOTS).when(osRepository).getMaxPciDevices(anyInt(), any());
    }
}
