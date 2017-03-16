package org.ovirt.engine.core.bll;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.errors.EngineMessage.ACTION_TYPE_FAILED_EDITING_HOSTED_ENGINE_IS_DISABLED;
import static org.ovirt.engine.core.common.errors.EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_HOSTED_ENGINE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.numa.vm.NumaValidator;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
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
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

/** A test case for the {@link UpdateVmCommand}. */
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
    private static final Version version = Version.v3_6;
    private static final Guid clusterId = Guid.newGuid();
    protected static final int MAX_MEMORY_SIZE = 4096;

    @Mock
    private VmDao vmDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Mock
    private QuotaDao quotaDao;
    @Mock
    private DiskVmElementDao diskVmElementDao;
    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;
    static OsRepository osRepository;

    @Mock
    InClusterUpgradeValidator inClusterUpgradeValidator;

    @InjectMocks
    private VmDeviceUtils vmDeviceUtils;
    @InjectMocks
    private NumaValidator numaValidator;

    @Spy
    @InjectMocks
    private VmHandler vmHandler;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    private static VmManagementParametersBase initParams() {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setClusterId(clusterId);
        vmStatic.setName("my_vm");
        vmStatic.setMaxMemorySizeMb(MAX_MEMORY_SIZE);

        VmManagementParametersBase params = new VmManagementParametersBase();
        params.setCommandType(VdcActionType.UpdateVm);
        params.setVmStaticData(vmStatic);

        return params;
    }

    @BeforeClass
    public static void setUpOsRepository() {
        osRepository = mock(OsRepository.class);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
    }

    @Before
    public void setUp() {
        final int osId = 0;

        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);

        when(cpuFlagsManagerHandler.getCpuId(any(), any())).thenReturn(CPU_ID);

        when(osRepository.getMinimumRam(osId, version)).thenReturn(0);
        when(osRepository.getMinimumRam(osId, null)).thenReturn(0);
        when(osRepository.getMaximumRam(osId, version)).thenReturn(256);
        when(osRepository.getMaximumRam(osId, null)).thenReturn(256);
        when(osRepository.isWindows(osId)).thenReturn(false);
        when(osRepository.getArchitectureFromOS(osId)).thenReturn(ArchitectureType.x86_64);
        when(osRepository.isCpuSupported(anyInt(), any(), any())).thenReturn(true);

        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<>());
        displayTypeMap.get(osId).put(version, Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);

        vmHandler.init();
        vm = new VM();
        vmStatic = command.getParameters().getVmStaticData();
        group = new Cluster();
        group.setCpuName("Intel Conroe Family");
        group.setId(clusterId);
        group.setCompatibilityVersion(version);
        group.setArchitecture(ArchitectureType.x86_64);
        vm.setClusterId(clusterId);
        vm.setClusterArch(ArchitectureType.x86_64);

        doReturn(group).when(command).getCluster();
        doReturn(vm).when(command).getVm();
        doReturn(VdcActionType.UpdateVm).when(command).getActionType();
        doReturn(false).when(command).isVirtioScsiEnabledForVm(any(Guid.class));
        doReturn(true).when(command).isBalloonEnabled();
        doReturn(true).when(osRepository).isBalloonEnabled(vm.getVmOsId(), group.getCompatibilityVersion());

        doReturn(vmDeviceUtils).when(command).getVmDeviceUtils();
        doReturn(numaValidator).when(command).getNumaValidator();
    }

    @Test
    public void testBeanValidations() {
        assertTrue(command.validateInputs());
    }

    @Test
    public void testPatternBasedNameFails() {
        vmStatic.setName("aa-??bb");
        assertFalse("Pattern-based name should not be supported for VM", command.validateInputs());
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
        boolean c = command.validate();
        assertTrue("validate should have passed.", c);
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

        assertTrue("validate should have passed.", command.validate());
    }

    @Test
    public void testDedicatedHostNotExistOrNotSameCluster() {
        prepareVmToPassValidate();

        doReturn(false).when(command).isDedicatedVdsExistOnSameCluster(any(VmBase.class), any(ArrayList.class));

        vmStatic.setDedicatedVmForVdsList(Guid.newGuid());

        assertFalse("validate should have failed with invalid dedicated host.", command.validate());
    }

    @Test
    public void testValidDedicatedHost() {
        prepareVmToPassValidate();
        mockVmValidator();

        VDS vds = new VDS();
        vds.setClusterId(group.getId());
        when(vdsDao.get(any(Guid.class))).thenReturn(vds);
        doReturn(true).when(command).isDedicatedVdsExistOnSameCluster(any(VmBase.class), any(ArrayList.class));
        vmStatic.setDedicatedVmForVdsList(Guid.newGuid());

        command.initEffectiveCompatibilityVersion();
        assertTrue("validate should have passed.", command.validate());
    }

    @Test
    public void testInvalidNumberOfMonitors() {
        prepareVmToPassValidate();
        vmStatic.setNumOfMonitors(99);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_NUM_OF_MONITORS);
    }

    @Test
    public void testBlockSettingHaOnHostedEngine() {
        // given
        prepareVmToPassValidate();
        command.initEffectiveCompatibilityVersion();
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        vmStatic.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        command.getParameters().getVm().setAutoStartup(true);
        // when
        boolean validInput = command.validate();
        // then
        assertFalse(validInput);
        assertTrue(command.getReturnValue().getValidationMessages().contains(ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_HOSTED_ENGINE.name()));
    }

    @Test
    public void testAllowSettingHaOnNonHostedEngine() {
        // given
        prepareVmToPassValidate();
        command.initEffectiveCompatibilityVersion();
        vm.setOrigin(OriginType.RHEV);
        vmStatic.setOrigin(OriginType.RHEV);
        command.getParameters().getVm().setAutoStartup(true);
        // when
        boolean validInput = command.validate();
        // then
        assertTrue(validInput);
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

        assertTrue("Quota enforcement type should be updatable", command.areUpdatedFieldsLegal());
    }

    @Test
    public void testUpdateFieldsQutoaDefault() {
        vm.setIsQuotaDefault(true);
        vmStatic.setQuotaDefault(false);

        assertTrue("Quota default should be updatable", command.areUpdatedFieldsLegal());
    }

    @Test
    public void testChangeClusterForbidden() {
        prepareVmToPassValidate();
        Cluster newGroup = new Cluster();
        newGroup.setId(Guid.newGuid());
        newGroup.setCompatibilityVersion(Version.v3_6);
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
    public void testUnsupportedCpus() {
        prepareVmToPassValidate();

        // prepare the mock values
        HashMap<Pair<Integer, Version>, Set<String>> unsupported = new HashMap<>();
        HashSet<String> value = new HashSet<>();
        value.add(CPU_ID);
        unsupported.put(new Pair<>(0, Version.v3_6), value);

        when(osRepository.isCpuSupported(0, Version.v3_6, CPU_ID)).thenReturn(false);
        when(osRepository.getUnsupportedCpus()).thenReturn(unsupported);

        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(
                command,
                EngineMessage.CPU_TYPE_UNSUPPORTED_FOR_THE_GUEST_OS);
    }

    @Test
    public void testCannotUpdateOSNotSupportVirtioScsi() {
        prepareVmToPassValidate();
        group.setCompatibilityVersion(Version.v3_6);

        doReturn(true).when(command).isVirtioScsiEnabledForVm(any(Guid.class));
        when(osRepository.getDiskInterfaces(anyInt(), any(Version.class))).thenReturn(
                Collections.singletonList("VirtIO"));

        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_DOES_NOT_SUPPORT_VIRTIO_SCSI);
    }

    @Test
    public void testBlockingHostedEngineEditing() {
        // given
        mcr.mockConfigValue(ConfigValues.AllowEditingHostedEngine, false);
        vmStatic.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        // when
        boolean validInput = command.validateInputs();
        // then
        assertThat(validInput, is(false));
        assertTrue(command.getReturnValue().getValidationMessages()
                .contains(ACTION_TYPE_FAILED_EDITING_HOSTED_ENGINE_IS_DISABLED.name()));
    }

    @Test
    public void testAllowedHostedEngineEditing() {
        // given
        mcr.mockConfigValue(ConfigValues.AllowEditingHostedEngine, true);
        vmStatic.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        // when
        boolean validInput = command.validateInputs();
        // then
        assertThat(validInput, is(true));
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
        doReturn(true).when(command).isDedicatedVdsExistOnSameCluster(any(VmBase.class), any(ArrayList.class));
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
        doReturn(true).when(command).isDedicatedVdsExistOnSameCluster(any(VmBase.class), any(ArrayList.class));
        vm.setStatus(VMStatus.Up);
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);
        vm.setRunOnVds(GUIDS[2]);
        vm.setRunOnVdsName("host_2");
        vmStatic.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vmStatic.setDedicatedVmForVdsList(Collections.singletonList(GUIDS[2]));
        command.initEffectiveCompatibilityVersion();
        assertTrue("validate should allow pinning VM.", command.validate());
    }

    @Test
    public void testShouldCheckVmOnClusterUpgrade() {
        prepareVmToPassValidate();
        mockVmValidator();
        group.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        verify(inClusterUpgradeValidator, times(1)).isVmReadyForUpgrade(any(VM.class));
    }

    @Test
    public void testCheckVmOnlyOnClusterUpgrade() {
        prepareVmToPassValidate();
        mockVmValidator();
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        verify(inClusterUpgradeValidator, times(0)).isVmReadyForUpgrade(any(VM.class));
    }

    @Test
    public void testFailOnClusterUpgrade() {
        prepareVmToPassValidate();
        mockVmValidator();
        final ValidationResult validationResult = new ValidationResult(EngineMessage
                .BOUND_TO_HOST_WHILE_UPGRADING_CLUSTER);
        doReturn(validationResult).when(inClusterUpgradeValidator).isVmReadyForUpgrade(any(VM.class));
        group.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        command.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.BOUND_TO_HOST_WHILE_UPGRADING_CLUSTER);
        verify(inClusterUpgradeValidator, times(1)).isVmReadyForUpgrade(any(VM.class));
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

        // when
        boolean validInput = command.validate();

        // then
        assertTrue(validInput);
    }

    @Test
    public void testValidQuota() {
        Guid quotaId = Guid.newGuid();

        Quota quota = new Quota();
        quota.setId(quotaId);

        when(quotaDao.getById(quotaId)).thenReturn(quota);

        StoragePool storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());

        quota.setStoragePoolId(storagePool.getId());
        command.setStoragePool(storagePool);

        command.getParameters().getVm().setQuotaId(quotaId);

        assertTrue(command.validateQuota(quotaId));
        assertTrue(command.getReturnValue().getValidationMessages().isEmpty());
    }

    @Test
    public void testNonExistingQuota() {
        prepareVmToPassValidate();
        vmStatic.setQuotaId(Guid.newGuid());

        assertFalse(command.validateQuota(vmStatic.getQuotaId()));
        ValidateTestUtils.assertValidationMessages("", command, EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
    }

    private void mockVmValidator() {
        VmValidator vmValidator = spy(new VmValidator(vm));
        doReturn(vmValidator).when(command).createVmValidator(vm);
        doReturn(diskDao).when(vmValidator).getDiskDao();
        doReturn(getNoVirtioScsiDiskElement()).when(diskVmElementDao).get(any(VmDeviceId.class));
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
        vmStatic.setSingleQxlPci(false);
        mockVmDaoGetVm();
        mockSameNameQuery(false);
        mockValidateCustomProperties();
        mockValidatePciAndIdeLimit();
        doReturn(true).when(command).setAndValidateCpuProfile();
        mockGraphicsDevice();
    }

    private void mockDiskDaoGetAllForVm(List<Disk> disks) {
        doReturn(disks).when(diskDao).getAllForVm(vm.getId(), true);
    }

    private void mockVmDaoGetVm() {
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
    }

    private void mockValidateCustomProperties() {
        doReturn(true).when(command).validateCustomProperties(any(VmStatic.class), any(ArrayList.class));
    }

    private void mockValidatePciAndIdeLimit() {
        doReturn(true).when(command).isValidPciAndIdeLimit(any(VM.class));
    }

    private void mockSameNameQuery(boolean result) {
        doReturn(result).when(command).isVmWithSameNameExists(anyString(), any(Guid.class));
    }
}
