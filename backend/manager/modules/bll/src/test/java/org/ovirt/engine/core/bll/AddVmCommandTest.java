package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.numa.vm.NumaValidator;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.bll.validator.QuotaValidator;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;


@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddVmCommandTest extends AddVmCommandTestBase<AddVmCommand<AddVmParameters>> {
    private static final String CPU_ID = "0";

    @Mock
    private VmValidationUtils vmValidationUtils;

    @Mock
    private QuotaValidator quotaValidator;

    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Mock
    @InjectedMock
    private CpuProfileHelper cpuProfileHelper;

    @Mock
    @InjectedMock
    private NumaValidator numaValidator;

    @Mock
    @InjectedMock
    private AffinityValidator affinityValidator;

    @Mock
    @InjectedMock
    private CloudInitHandler cloudInitHandler;

    @Mock
    @InjectedMock
    private VmStaticDao vmStaticDao;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        Map<String, String> migrationMap = new HashMap<>();
        migrationMap.put("undefined", "true");
        migrationMap.put("x86", "true");
        migrationMap.put("ppc", "true");

        Version version = Version.v4_2;

        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxIoThreadsPerVm, 127),
                MockConfigDescriptor.of(ConfigValues.MaxVmNameLength, 64),
                MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels,
                        new HashSet<>(Collections.singletonList(Version.v4_2))),
                MockConfigDescriptor.of(ConfigValues.ValidNumOfMonitors, Arrays.asList("1", "2", "4")),
                MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, version, migrationMap),
                MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, Version.getLast(), migrationMap),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfCpuPerSocket, version, 16),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfThreadsPerCpu, version, 8),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmCpus, version, createMaxNumberOfVmCpusMap()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfCpusCoefficient, 2),
                MockConfigDescriptor.of(ConfigValues.ManyVmCpus, 128),
                MockConfigDescriptor.of(ConfigValues.UefiBigVmMemoryGB, 16),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmSockets, version, 16),
                MockConfigDescriptor.of(ConfigValues.VM32BitMaxMemorySizeInMB, version, 20480),
                MockConfigDescriptor.of(ConfigValues.VM64BitMaxMemorySizeInMB, version, 4194304),
                MockConfigDescriptor.of(ConfigValues.VMPpc64BitMaxMemorySizeInMB, version, 1048576),
                MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, version, true),
                MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, Version.v4_8, true),
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

    private static Map<String, Integer> createMaxNumberOfVmCpusMap() {
        Map<String, Integer> maxVmCpusMap = new HashMap<>();
        maxVmCpusMap.put("s390x", 384);
        maxVmCpusMap.put("x86", 512);
        maxVmCpusMap.put("ppc", 384);
        return maxVmCpusMap;
    }

    @Override
    protected AddVmCommand<AddVmParameters> createCommand() {
        return new AddVmCommand<>(new AddVmParameters(vm), null);
    }

    @Test
    public void canAddVm() {
        doNothing().when(cmd).initTemplateDisks();
        initCommandMethods();
        cmd.init();

        doReturn(true).when(cmd).validateCustomProperties(any());
        doReturn(true).when(cmd).validateSpaceRequirements();
        assertTrue(cmd.canAddVm(Collections.singletonList(createStorageDomain(STORAGE_DOMAIN_ID_1))),
                "vm could not be added");
    }

    @Test
    public void isVirtioScsiEnabledDefaultedToTrue() {
        cmd.getParameters().getVm().setClusterId(cluster.getId());
        cmd.getParameters().getVm().getStaticData().setBiosType(BiosType.Q35_SEA_BIOS);
        cmd.initEffectiveCompatibilityVersion();
        when(vmValidationUtils.isDiskInterfaceSupportedByOs(anyInt(), any(), any(), eq(DiskInterface.VirtIO_SCSI)))
                .thenReturn(true);
        assertTrue(cmd.isVirtioScsiEnabled(), "isVirtioScsiEnabled hasn't been defaulted to true on cluster >= 3.3.");
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateSpaceAndThreshold() {
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForNewDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForClonedDisks(any());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateSpaceNotEnough() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForNewDisks(any());
        assertFalse(cmd.validateSpaceRequirements());
        verify(storageDomainValidator).hasSpaceForNewDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForClonedDisks(any());
    }

    @Test
    public void validateSpaceNotWithinThreshold() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
               when(storageDomainValidator).isDomainWithinThresholds();
        assertFalse(cmd.validateSpaceRequirements());
    }

    private void initPpcCluster() {
        initCluster();
        cluster.setCpuName("PPC8");
        cluster.setArchitecture(ArchitectureType.ppc64);
    }

    @Test
    public void testBeanValidations() {
        assertTrue(cmd.validateInputs());
    }

    @Test
    public void testPatternBasedNameFails() {
        cmd.getParameters().getVm().setName("aa-??bb");
        assertFalse(cmd.validateInputs(), "Pattern-based name should not be supported for VM");
    }

    @Test
    public void refuseSoundDeviceOnPPC() {
        doNothing().when(cmd).initTemplateDisks();
        setupCanAddPpcTest();
        cmd.getParameters().setSoundDeviceEnabled(true);
        cmd.init();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
    }

    private void setupCanAddPpcTest() {
        doReturn(true).when(cmd).buildAndCheckDestStorageDomains();
        cmd.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        cluster.setArchitecture(ArchitectureType.ppc64);
        cluster.setCompatibilityVersion(Version.getLast());
    }

    @Test
    public void testStoragePoolDoesntExist() {
        doReturn(null).when(cmd).getStoragePool();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testBlockUseHostCpuWithPPCArch() {
        when(vmValidationUtils.isOsTypeSupported(anyInt(), any())).thenReturn(true);
        when(vmValidationUtils.isGraphicsAndDisplaySupported(anyInt(), any(), any(), any())).thenReturn(true);
        when(cpuFlagsManagerHandler.getCpuId(any(), any())).thenReturn(CPU_ID);
        when(osRepository.isCpuSupported(anyInt(), any(), any())).thenReturn(true);
        doNothing().when(cmd).initTemplateDisks();
        setupCanAddPpcTest();
        cmd.setEffectiveCompatibilityVersion(Version.v4_2);
        doReturn(Collections.emptyList()).when(cmd).getImagesToCheckDestinationStorageDomains();
        initPpcCluster();
        doReturn(true).when(cmd).validateAddVmCommand();
        doReturn(true).when(cmd).isVmNameValidLength(any());
        cmd.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        cmd.getParameters().getVm().setUseHostCpuFlags(true);
        cmd.getParameters().getVm().setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        cmd.getParameters().getVm().setClusterId(cluster.getId());
        cmd.getParameters().getVm().setVmOs(OsType.Other.ordinal());
        cmd.init();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.USE_HOST_CPU_REQUESTED_ON_UNSUPPORTED_ARCH);
    }

    @Test
    public void testValidateQuota() {
        doReturn(quotaValidator).when(cmd).createQuotaValidator(any());
        cmd.validateQuota(Guid.newGuid());

        verify(quotaValidator, times(1)).isValid();
        verify(quotaValidator, times(1)).isDefinedForStoragePool(any());
    }

    // --- i440fx custom emulated machine validation ---

    /**
     * Initializes the command so that full validate() can run: populates diskInfoDestinationMap via init()
     * and stubs out the heavy sub-validations not under test here.
     */
    private void initForEmulatedMachineValidation() {
        doNothing().when(cmd).initTemplateDisks();
        doReturn(true).when(cmd).validateAddVmCommand();
        doReturn(true).when(cmd).isVmNameValidLength(any());
        doReturn(Collections.emptyList()).when(cmd).getImagesToCheckDestinationStorageDomains();
        when(vmValidationUtils.isOsTypeSupported(anyInt(), any())).thenReturn(true);
        when(vmValidationUtils.isGraphicsAndDisplaySupported(anyInt(), any(), any(), any())).thenReturn(true);
        when(cpuFlagsManagerHandler.getCpuId(any(), any())).thenReturn(CPU_ID);
        when(osRepository.isCpuSupported(anyInt(), any(), any())).thenReturn(true);
        when(osRepository.isQ35Supported(anyInt())).thenReturn(true);
        when(cpuProfileHelper.setAndValidateCpuProfile(any(), any())).thenReturn(ValidationResult.VALID);
        when(numaValidator.checkVmNumaNodesIntegrity(any(), any())).thenReturn(ValidationResult.VALID);
        when(affinityValidator.validateAffinityUpdateForVm(any(), any(), any(), any()))
                .thenReturn(AffinityValidator.Result.VALID);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        cmd.init();
        // avoid VM_ID_EXISTS path — vmStaticDao is not injected into the command in this test
        doReturn(null).when(cmd).getVmId();
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testI440fxVmWithoutCustomEmulatedMachineIsAutoPinned() {
        initForEmulatedMachineValidation();
        // >= 4.8 cluster: strict rules apply
        cluster.setCompatibilityVersion(Version.v4_8);
        // i440fx VM without customEmulatedMachine gets the newest machine type common to
        // all cluster hosts — the selector decides; here it derives the newest
        cmd.getParameters().getVm().getStaticData().setBiosType(BiosType.I440FX_SEA_BIOS);
        cmd.getParameters().getVm().getStaticData().setCustomEmulatedMachine(null);
        when(vdsDao.getClusterI440fxCommonMachines(cluster.getId()))
                .thenReturn(List.of("pc-i440fx-rhel7.6.0", "pc-i440fx-rhel10.0.0"));

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);

        assertEquals("pc-i440fx-rhel10.0.0",
                cmd.getParameters().getVm().getStaticData().getCustomEmulatedMachine(),
                "the newest common i440fx machine type must be auto-selected");
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testI440fxVmWithoutCustomEmulatedMachineAndNoCommonTypeFailsValidation() {
        initForEmulatedMachineValidation();
        // >= 4.8 cluster: strict rules apply
        cluster.setCompatibilityVersion(Version.v4_8);
        // mixed EL9/EL10 cluster: no single i440fx type works on every host — the caller
        // must choose explicitly (the selector mock derives nothing)
        cmd.getParameters().getVm().getStaticData().setBiosType(BiosType.I440FX_SEA_BIOS);
        cmd.getParameters().getVm().getStaticData().setCustomEmulatedMachine(null);
        when(vdsDao.getClusterI440fxCommonMachines(cluster.getId()))
                .thenReturn(Collections.emptyList());

        ValidateTestUtils.runAndAssertValidateFailure(
                cmd, EngineMessage.ACTION_TYPE_FAILED_I440FX_VM_REQUIRES_CUSTOM_EMULATED_MACHINE);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testI440fxVmWithCustomEmulatedMachinePassesValidation() {
        initForEmulatedMachineValidation();
        // i440fx VM with explicit customEmulatedMachine must pass the emulated machine check
        cmd.getParameters().getVm().getStaticData().setBiosType(BiosType.I440FX_SEA_BIOS);
        cmd.getParameters().getVm().getStaticData().setCustomEmulatedMachine("pc-i440fx-rhel7.6.0");

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testQ35VmWithoutCustomEmulatedMachinePassesValidation() {
        initForEmulatedMachineValidation();
        // Q35 VM without customEmulatedMachine inherits from cluster default — must pass
        cmd.getParameters().getVm().getStaticData().setBiosType(BiosType.Q35_SEA_BIOS);
        cmd.getParameters().getVm().getStaticData().setCustomEmulatedMachine(null);

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testI440fxVmWithoutCustomEmulatedMachineIsValidBelow48() {
        initForEmulatedMachineValidation();
        // The test base cluster is at 4.2: the dual cluster default is a legitimate state
        // there and an unpinned i440fx VM inherits its i440fx part — no pin required and
        // no auto-selection either.
        cmd.getParameters().getVm().getStaticData().setBiosType(BiosType.I440FX_SEA_BIOS);
        cmd.getParameters().getVm().getStaticData().setCustomEmulatedMachine(null);

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);

        assertNull(cmd.getParameters().getVm().getStaticData().getCustomEmulatedMachine(),
                "below 4.8 no auto-pin must happen");
        verify(vdsDao, never()).getClusterI440fxCommonMachines(any());
    }
}
