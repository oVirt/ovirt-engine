package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.Strict;
import org.ovirt.engine.core.bll.validator.QuotaValidator;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(Strict.class)
public class AddVmCommandTest extends AddVmCommandTestBase<AddVmCommand<AddVmParameters>> {
    private static final String CPU_ID = "0";

    @Mock
    private VmValidationUtils vmValidationUtils;

    @Mock
    private QuotaValidator quotaValidator;

    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
        mockConfig(ConfigValues.MaxIoThreadsPerVm, 127),
        mockConfig(ConfigValues.MaxVmNameLength, 64),
        mockConfig(ConfigValues.ResumeBehaviorSupported, Version.v4_3, true),
        mockConfig(ConfigValues.ResumeBehaviorSupported, Version.v4_0, false),
        mockConfig(ConfigValues.SupportedClusterLevels, new HashSet<>(Collections.singletonList(new Version(3, 0)))),
        mockConfig(ConfigValues.ValidNumOfMonitors, Arrays.asList("1", "2", "4"))
    );

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
        assertTrue("vm could not be added",
                cmd.canAddVm(Collections.singletonList(createStorageDomain(STORAGE_DOMAIN_ID_1))));
    }

    @Test
    public void isVirtioScsiEnabledDefaultedToTrue() {
        cmd.getParameters().getVm().setClusterId(cluster.getId());
        cmd.initEffectiveCompatibilityVersion();
        when(vmValidationUtils.isDiskInterfaceSupportedByOs(anyInt(), any(), eq(DiskInterface.VirtIO_SCSI)))
                .thenReturn(true);
        assertTrue("isVirtioScsiEnabled hasn't been defaulted to true on cluster >= 3.3.", cmd.isVirtioScsiEnabled());
    }

    @Test
    public void validateSpaceAndThreshold() {
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForNewDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForClonedDisks(any());
    }

    @Test
    public void validateSpaceNotEnough() throws Exception {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForNewDisks(any());
        assertFalse(cmd.validateSpaceRequirements());
        verify(storageDomainValidator).hasSpaceForNewDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForClonedDisks(any());
    }

    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
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
        assertFalse("Pattern-based name should not be supported for VM", cmd.validateInputs());
    }

    @Test
    public void refuseBalloonOnPPC() {
        setupCanAddPpcTest();
        cmd.getParameters().setBalloonEnabled(true);
        doNothing().when(cmd).initTemplateDisks();
        cmd.init();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH);
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
        cmd.setEffectiveCompatibilityVersion(Version.v4_0);
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
        Map<String, String> migrationMap = new HashMap<>();
        migrationMap.put("undefined", "true");
        migrationMap.put("x86", "true");
        migrationMap.put("ppc", "true");
        mcr.mockConfigValue(ConfigValues.IsMigrationSupported, cmd.getEffectiveCompatibilityVersion(), migrationMap);
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
