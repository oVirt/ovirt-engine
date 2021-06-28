package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.errors.EngineMessage.ACTION_TYPE_FAILED_EDITING_HOSTED_ENGINE_IS_DISABLED;
import static org.ovirt.engine.core.common.errors.EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_HOSTED_ENGINE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.numa.vm.NumaValidator;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.bll.validator.QuotaValidator;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;

/** A test case for the {@link UpdateVmCommand}. */
@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateVmCommandTest extends BaseCommandTest {

    private VM vm;
    private VmStatic vmStatic;
    private Cluster group;

    @Spy
    @InjectMocks
    private UpdateVmCommand<VmManagementParametersBase> command = new UpdateVmCommand<>(initParams(), null);

    private static final Guid[] GUIDS = {
        new Guid("00000000-0000-0000-0000-000000000000"),
        new Guid("11111111-1111-1111-1111-111111111111"),
        new Guid("22222222-2222-2222-2222-222222222222"),
        new Guid("33333333-3333-3333-3333-333333333333")
    };

    private static final String CPU_ID = "0";
    private static final Version version = Version.v4_2;
    private static final Guid clusterId = Guid.newGuid();
    protected static final int MAX_MEMORY_SIZE = 4096;
    protected static final int MEMORY_SIZE = 1024;

    @Mock
    private VmDao vmDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    @InjectedMock
    public CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Mock
    private QuotaValidator quotaValidator;
    @Mock
    private DiskVmElementDao diskVmElementDao;
    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;
    @Mock
    private OsRepository osRepository;

    @Mock
    InClusterUpgradeValidator inClusterUpgradeValidator;

    @Mock
    private VmValidationUtils vmValidationUtils;

    @InjectMocks
    private VmDeviceUtils vmDeviceUtils;
    @InjectMocks
    private NumaValidator numaValidator;

    @Mock
    private VmHandler vmHandler;

    @Mock
    private CloudInitHandler cloudInitHandler;
    @Mock
    private AffinityValidator affinityValidator;
    @Mock
    private VmNumaNodeDao vmNumaNodeDao;

    private static Map<String, String> createMigrationMap() {
        Map<String, String> migrationMap = new HashMap<>();
        migrationMap.put("undefined", "true");
        migrationMap.put("x86", "true");
        migrationMap.put("ppc", "true");
        return migrationMap;
    }

    private static Map<String, Integer> createMaxNumberOfVmCpusMap() {
        Map<String, Integer> maxVmCpusMap = new HashMap<>();
        maxVmCpusMap.put("s390x", 384);
        maxVmCpusMap.put("x86", 512);
        maxVmCpusMap.put("ppc", 384);
        return maxVmCpusMap;
    }

    private static Map<String, String> createHotPlugMemorySupportedMap() {
        Map<String, String> hotPlugMemoryMap = new HashMap<>();
        hotPlugMemoryMap.put("s390x", "false");
        hotPlugMemoryMap.put("x86", "true");
        hotPlugMemoryMap.put("ppc", "true");
        return hotPlugMemoryMap;
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxVmNameLength, 64),
                MockConfigDescriptor.of(ConfigValues.ValidNumOfMonitors, Arrays.asList("1", "2", "4")),
                MockConfigDescriptor.of(ConfigValues.VmPriorityMaxValue, 100),
                MockConfigDescriptor.of(ConfigValues.MaxIoThreadsPerVm, 127),
                MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels,
                        new HashSet<>(Collections.singleton(Version.getLast()))),
                MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, version, createMigrationMap()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfCpuPerSocket, version, 16),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfThreadsPerCpu, version, 8),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmCpus, version, createMaxNumberOfVmCpusMap()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmSockets, version, 16),
                MockConfigDescriptor.of(ConfigValues.VM32BitMaxMemorySizeInMB, version, 20480),
                MockConfigDescriptor.of(ConfigValues.VM64BitMaxMemorySizeInMB, version, 4194304),
                MockConfigDescriptor.of(ConfigValues.VMPpc64BitMaxMemorySizeInMB, version, 1048576),
                MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, version, true),
                MockConfigDescriptor.of(ConfigValues.HotPlugMemorySupported, version, createHotPlugMemorySupportedMap())
        );
    }

    private static VmManagementParametersBase initParams() {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setClusterId(clusterId);
        vmStatic.setName("my_vm");
        vmStatic.setMaxMemorySizeMb(MAX_MEMORY_SIZE);
        vmStatic.setMemSizeMb(MEMORY_SIZE);

        VmManagementParametersBase params = new VmManagementParametersBase();
        params.setCommandType(ActionType.UpdateVm);
        params.setVmStaticData(vmStatic);
        params.setUpdateNuma(true);

        return params;
    }

    @BeforeEach
    public void setUp() {
        final int osId = 0;

        when(cpuFlagsManagerHandler.getCpuId(any(), any())).thenReturn(CPU_ID);

        when(osRepository.getMinimumRam(osId, version)).thenReturn(0);
        when(osRepository.getMinimumRam(osId, null)).thenReturn(0);
        when(osRepository.getMaximumRam(osId, version)).thenReturn(256);
        when(osRepository.getMaximumRam(osId, null)).thenReturn(256);
        when(osRepository.isWindows(osId)).thenReturn(false);
        when(osRepository.getArchitectureFromOS(osId)).thenReturn(ArchitectureType.x86_64);
        when(osRepository.isCpuSupported(anyInt(), any(), any())).thenReturn(true);
        when(osRepository.isQ35Supported(anyInt())).thenReturn(true);

        when(vmValidationUtils.isOsTypeSupported(anyInt(), any())).thenReturn(true);
        when(vmValidationUtils.isGraphicsAndDisplaySupported(anyInt(), any(), any(), any())).thenReturn(true);

        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<>());
        displayTypeMap.get(osId).put(version, Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);

        when(vmHandler.isUpdateValid(any(), any(), any())).thenReturn(true);

        when(affinityValidator.validateAffinityUpdateForVm(any(), any(), any(), any()))
                .thenReturn(AffinityValidator.Result.VALID);
        when(vmNumaNodeDao.getAllVmNumaNodeByVmId(any())).thenReturn(Collections.emptyList());

        vm = new VM();
        vmStatic = command.getParameters().getVmStaticData();
        group = new Cluster();
        group.setCpuName("Intel Conroe Family");
        group.setId(clusterId);
        group.setCompatibilityVersion(version);
        group.setArchitecture(ArchitectureType.x86_64);
        group.setBiosType(BiosType.Q35_SEA_BIOS);
        vm.setClusterId(clusterId);
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        vm.setVmMemSizeMb(MEMORY_SIZE);
        vm.setMaxMemorySizeMb(MAX_MEMORY_SIZE);
        vm.setOrigin(OriginType.OVIRT);

        doReturn(group).when(command).getCluster();
        doReturn(vm).when(command).getVm();
        doReturn(ActionType.UpdateVm).when(command).getActionType();
        doReturn(false).when(command).isVirtioScsiEnabledForVm(any());

        doReturn(vmDeviceUtils).when(command).getVmDeviceUtils();
        doReturn(numaValidator).when(command).getNumaValidator();
        doReturn(quotaValidator).when(command).createQuotaValidator(any());
        doReturn(Boolean.TRUE).when(command).isSystemSuperUser();

        command.init();
    }

    @Test
    public void testBeanValidations() {
        assertTrue(command.validateInputs());
    }

    @Test
    public void testPatternBasedNameFails() {
        vmStatic.setName("aa-??bb");
        assertFalse(command.validateInputs(), "Pattern-based name should not be supported for VM");
    }

    @Test
    public void testLongName() {
        vmStatic.setName("this_should_be_very_long_vm_name_so_it will_fail_can_do_action_validation");
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
    }

    @Test
    public void testValidName() {
        prepareVmToPassValidate();
        mockVmValidator();

        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testChangeToExistingName() {
        prepareVmToPassValidate();
        mockSameNameQuery(true);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
    }

    @Test
    public void testNameNotChanged() {
        prepareVmToPassValidate();
        vm.setName("vm1");
        mockSameNameQuery(true);
        mockVmValidator();
        command.initEffectiveCompatibilityVersion();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testDedicatedHostNotExistOrNotSameCluster() {
        prepareVmToPassValidate();

        doReturn(false).when(command).isDedicatedVdsExistOnSameCluster(any());

        vmStatic.setDedicatedVmForVdsList(Collections.singletonList(Guid.newGuid()));

        assertFalse(command.validate(), "validate should have failed with invalid dedicated host.");
    }

    @Test
    public void testValidDedicatedHost() {
        prepareVmToPassValidate();
        mockVmValidator();

        VDS vds = new VDS();
        vds.setClusterId(group.getId());
        when(vdsDao.get(any())).thenReturn(vds);
        vmStatic.setDedicatedVmForVdsList(Collections.singletonList(Guid.newGuid()));

        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testBlockSettingHaOnHostedEngine() {
        // given
        prepareVmToPassValidate();
        command.initEffectiveCompatibilityVersion();
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        vmStatic.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        command.getParameters().getVm().setAutoStartup(true);

        ValidateTestUtils.runAndAssertValidateFailure
                (command, ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_HOSTED_ENGINE);
    }

    @Test
    public void testAllowSettingHaOnNonHostedEngine() {
        // given
        prepareVmToPassValidate();
        command.initEffectiveCompatibilityVersion();
        vm.setOrigin(OriginType.RHEV);
        vmStatic.setOrigin(OriginType.RHEV);
        command.getParameters().getVm().setAutoStartup(true);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private void mockGraphicsDevice() {
        VmDevice graphicsDevice = new GraphicsDevice(VmDeviceType.SPICE);
        graphicsDevice.setDeviceId(Guid.Empty);
        graphicsDevice.setVmId(vm.getId());

        mockVmDevice(graphicsDevice);
    }

    private void mockVmDevice(VmDevice vmDevice) {
        when(vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), vmDevice.getType())).thenReturn(Collections.singletonList(vmDevice));
    }

    @Test
    public void testUpdateFieldsQuotaEnforcementType() {
        vm.setQuotaEnforcementType(QuotaEnforcementTypeEnum.DISABLED);
        vmStatic.setQuotaEnforcementType(QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT);

        assertTrue(command.areUpdatedFieldsLegal(), "Quota enforcement type should be updatable");
    }

    @Test
    public void testUpdateFieldsQutoaDefault() {
        vm.setIsQuotaDefault(true);
        vmStatic.setQuotaDefault(false);

        assertTrue(command.areUpdatedFieldsLegal(), "Quota default should be updatable");
    }

    @Test
    public void testChangeClusterForbidden() {
        prepareVmToPassValidate();
        Cluster newGroup = new Cluster();
        newGroup.setId(Guid.newGuid());
        newGroup.setCompatibilityVersion(Version.v4_2);
        vmStatic.setClusterId(newGroup.getId());

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VM_CANNOT_UPDATE_CLUSTER);
    }

    @Test
    public void testCannotDisableVirtioScsi() {
        prepareVmToPassValidate();
        command.getParameters().setVirtioScsiEnabled(false);

        Disk disk = new DiskImage();
        disk.setPlugged(true);
        DiskVmElement dve = new DiskVmElement(disk.getId(), vm.getId());
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        disk.setDiskVmElements(Collections.singletonList(dve));
        mockDiskDaoGetAllForVm(Collections.singletonList(disk));
        mockVmValidator();

        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS);
    }

    @Test
    public void testCanEditARunningVM() {
        prepareVmToPassValidate();
        vm.setStatus(VMStatus.Up);
        mockDiskDaoGetAllForVm(Collections.emptyList());
        mockVmValidator();

        doReturn(true).when(command).areUpdatedFieldsLegal();

        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    @MockedConfig("mockConfigurationBlockingEditingHostedEngine")
    public void testBlockingHostedEngineEditing() {
        // given
        vmStatic.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        // when
        boolean validInput = command.validateInputs();
        // then
        assertThat(validInput, is(false));
        assertTrue(command.getReturnValue().getValidationMessages()
                .contains(ACTION_TYPE_FAILED_EDITING_HOSTED_ENGINE_IS_DISABLED.name()));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationBlockingEditingHostedEngine() {
        return Stream.concat(mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(ConfigValues.AllowEditingHostedEngine, false)));
    }

    @Test
    @MockedConfig("mockConfigurationAllowedEditingHostedEngine")
    public void testAllowedHostedEngineEditing() {
        // given
        vmStatic.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        // when
        boolean validInput = command.validateInputs();
        // then
        assertThat(validInput, is(true));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationAllowedEditingHostedEngine() {
        return Stream.concat(mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(ConfigValues.AllowEditingHostedEngine, true)));
    }

    @Test
    public void testHostedEngineConstraintsIneffectiveOnRegularVm() {
        // given
        vmStatic.setOrigin(OriginType.OVIRT);
        // when
        boolean validInput = command.validateInputs();
        // then
        assertThat(validInput, is(true));
    }

    /**
    * Migration policy from pinned to migrateable, VM status is down
    * VM update take effect normally.
    */
    @Test
    public void testMigrationPolicyChangeVmDown() {
        prepareVmToPassValidate();
        vm.setStatus(VMStatus.Down);
        vm.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vmStatic.setMigrationSupport(MigrationSupport.MIGRATABLE);
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    /**
     * Migration policy from pinned to migrateable, VM status is down
     * VM update take effect normally.
     */
    @Test
    public void testMigrationPolicyChangeVmDown2() {
        prepareVmToPassValidate();
        vm.setStatus(VMStatus.Down);
        vm.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vmStatic.setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    /**
    * Migration policy from migrateable to pinned,
    * VM status is up
    * VM is pinned to host_1
    * VM is running on host_2
    * Validate should fail
    */
    @Test
    public void testMigrationPolicyChangeFail() {
        prepareVmToPassValidate();
        vm.setStatus(VMStatus.Up);
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);
        vm.setRunOnVds(GUIDS[1]);
        vm.setRunOnVdsName("host_1");
        vmStatic.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vmStatic.setDedicatedVmForVdsList(Collections.singletonList(GUIDS[2]));
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PINNED_VM_NOT_RUNNING_ON_DEDICATED_HOST);

    }

    /**
    * Migration policy from migrateable to pinned,
    * VM status is up
    * VM is pinned to host_2
    * VM is running on host_2
    * Validate should pass
    */
    @Test
    public void testMigrationPolicyChangeVmUp() {
        prepareVmToPassValidate();
        vm.setStatus(VMStatus.Up);
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);
        vm.setRunOnVds(GUIDS[2]);
        vm.setRunOnVdsName("host_2");
        vmStatic.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vmStatic.setDedicatedVmForVdsList(Collections.singletonList(GUIDS[2]));
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testShouldCheckVmOnClusterUpgrade() {
        prepareVmToPassValidate();
        mockVmValidator();
        group.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        verify(inClusterUpgradeValidator, times(1)).isVmReadyForUpgrade(any());
    }

    @Test
    public void testCheckVmOnlyOnClusterUpgrade() {
        prepareVmToPassValidate();
        mockVmValidator();
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        verify(inClusterUpgradeValidator, times(0)).isVmReadyForUpgrade(any());
    }

    @Test
    public void testFailOnClusterUpgrade() {
        prepareVmToPassValidate();
        mockVmValidator();
        final ValidationResult validationResult = new ValidationResult(EngineMessage
                .BOUND_TO_HOST_WHILE_UPGRADING_CLUSTER);
        doReturn(validationResult).when(inClusterUpgradeValidator).isVmReadyForUpgrade(any());
        group.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.BOUND_TO_HOST_WHILE_UPGRADING_CLUSTER);
        verify(inClusterUpgradeValidator, times(1)).isVmReadyForUpgrade(any());
    }

    @Test
    public void testBlockUseHostCpuWithPPCArch() {
        // given
        prepareVmToPassValidate();
        command.initEffectiveCompatibilityVersion();
        vm.setClusterArch(ArchitectureType.ppc64le);
        group.setArchitecture(ArchitectureType.ppc);
        when(osRepository.getArchitectureFromOS(OsType.Windows.ordinal())).thenReturn(ArchitectureType.ppc);
        vmStatic.setUseHostCpuFlags(true);
        vmStatic.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.USE_HOST_CPU_REQUESTED_ON_UNSUPPORTED_ARCH);
    }

    @Test
    public void testAllowUseHostCpuWithX86Arch() {
        // given
        prepareVmToPassValidate();
        command.initEffectiveCompatibilityVersion();
        vm.setClusterArch(ArchitectureType.x86_64);
        vmStatic.setUseHostCpuFlags(true);
        vmStatic.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateQuota() {
        command.validateQuota(Guid.newGuid());

        verify(quotaValidator, times(1)).isValid();
        verify(quotaValidator, times(1)).isDefinedForStoragePool(any());
    }

    @Test
    public void testFailIncreaseMemOverMaxMemOnRunningVm() {
        prepareVmToPassValidate();
        mockVmValidator();

        vm.setStatus(VMStatus.Up);
        vmStatic.setMemSizeMb(vm.getMaxMemorySizeMb() + 1024);

        command.initEffectiveCompatibilityVersion();
        vm.setClusterCompatibilityVersion(version);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_MAX_MEMORY_CANNOT_BE_SMALLER_THAN_MEMORY_SIZE);
    }

    @Test
    public void testUpdateFailOnHostedEngineVmWithNonSuperUser() {
        doReturn(Boolean.FALSE).when(command).isSystemSuperUser();
        vm.setOrigin(OriginType.HOSTED_ENGINE);
        ValidateTestUtils.runAndAssertValidateFailure(
                command, EngineMessage.NON_ADMIN_USER_NOT_AUTHORIZED_TO_PERFORM_ACTION_ON_HE);
    }

    private void mockVmValidator() {
        VmValidator vmValidator = spy(new VmValidator(vm));
        doReturn(vmValidator).when(command).createVmValidator(vm);
        doReturn(diskDao).when(vmValidator).getDiskDao();
        doReturn(getNoVirtioScsiDiskElement()).when(diskVmElementDao).get(any());
        doReturn(diskVmElementDao).when(vmValidator).getDiskVmElementDao();
    }

    private DiskVmElement getNoVirtioScsiDiskElement() {
        DiskVmElement dve = new DiskVmElement(Guid.Empty, vm.getId());
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        return dve;
    }

    private void prepareVmToPassValidate() {
        vmStatic.setName("vm1");
        vmStatic.setMemSizeMb(256);
        mockVmDaoGetVm();
        mockSameNameQuery(false);
        mockValidateCustomProperties();
        mockValidatePciAndIdeLimit();
        doReturn(true).when(command).setAndValidateCpuProfile();
        doReturn(Collections.emptyList()).when(cloudInitHandler).validate(any());
        mockGraphicsDevice();
    }

    private void mockDiskDaoGetAllForVm(List<Disk> disks) {
        doReturn(disks).when(diskDao).getAllForVm(vm.getId(), true);
    }

    private void mockVmDaoGetVm() {
        when(vmDao.get(any())).thenReturn(vm);
    }

    private void mockValidateCustomProperties() {
        doReturn(true).when(command).validateCustomProperties(any());
    }

    private void mockValidatePciAndIdeLimit() {
        doReturn(true).when(command).isValidPciAndIdeLimit(any());
    }

    private void mockSameNameQuery(boolean result) {
        doReturn(result).when(command).isVmWithSameNameExists(any(), any());
    }
}
