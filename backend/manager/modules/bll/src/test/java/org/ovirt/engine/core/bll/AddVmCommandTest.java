package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import  org.ovirt.engine.core.utils.MockedConfig;


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

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        Map<String, String> migrationMap = new HashMap<>();
        migrationMap.put("undefined", "true");
        migrationMap.put("x86", "true");
        migrationMap.put("ppc", "true");

        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxIoThreadsPerVm, 127),
                MockConfigDescriptor.of(ConfigValues.MaxVmNameLength, 64),
                MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels,
                        new HashSet<>(Collections.singletonList(new Version(3, 0)))),
                MockConfigDescriptor.of(ConfigValues.ValidNumOfMonitors, Arrays.asList("1", "2", "4")),
                MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, Version.getLast(), migrationMap),
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
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
}
