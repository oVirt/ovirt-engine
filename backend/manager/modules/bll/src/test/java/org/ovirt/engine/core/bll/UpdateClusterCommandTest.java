package org.ovirt.engine.core.bll;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.network.cluster.DefaultManagementNetworkFinder;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateClusterCommandTest {

    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
    private static final Version VERSION_4_7 = new Version(4, 7);
    private static final Version VERSION_4_8 = new Version(4, 8);
    private static final Version VERSION_4_5 = new Version(4, 5);
    private static final String PC_I440FX_RHEL = "pc-i440fx-rhel7.6.0";
    private static final String PC_Q35_RHEL = "pc-q35-rhel9.6.0";
    private static final String DUAL_DEFAULT = PC_I440FX_RHEL + ";" + PC_Q35_RHEL;
    private static final String PC_I440FX_RHEL10 = "pc-i440fx-rhel10.0.0";
    private static final Guid DC_ID1 = Guid.newGuid();
    private static final Guid DC_ID2 = Guid.newGuid();
    private static final Guid DEFAULT_CLUSTER_ID = new Guid("99408929-82CF-4DC7-A532-9D998063FA95");
    private static final Guid DEFAULT_FEATURE_ID = new Guid("99408929-82CF-4DC7-A532-9D998063FA96");
    private static final Guid TEST_MANAGEMENT_NETWORK_ID = Guid.newGuid();
    private static final Guid NOT_UPGRADE_POLICY_GUID = Guid.newGuid();
    private static final Guid VM_ID1 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid VM_ID2 = new Guid("87296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid VM_ID3 = new Guid("67296e00-0cad-4e5a-9299-008a7b6f4355");

    private static final Set<Version> versions = new HashSet<>(Arrays.asList(VERSION_1_0, VERSION_1_1, VERSION_1_2));

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        Map<String, String> migrationMap = new HashMap<>();
        migrationMap.put("undefined", "true");
        migrationMap.put("x86", "true");
        migrationMap.put("ppc", "true");

        return Stream.concat(
                Stream.of(
                        MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels, versions),
                        MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, VERSION_1_0, true),
                        MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, VERSION_1_1, true),
                        MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, VERSION_1_2, true),
                        MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, VERSION_4_7, true),
                        MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, VERSION_4_8, true),
                        MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, VERSION_4_5, migrationMap),
                        MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, VERSION_4_7, migrationMap),
                        MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, VERSION_4_8, migrationMap)
                ),
                // Permute the migration map to all supported versions
                versions.stream().map(v -> MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, v, migrationMap))
        );
    }

    @Mock
    private VmStaticDao vmStaticDao;
    @Mock
    private VmTemplateDao vmTemplateDao;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private GlusterVolumeDao glusterVolumeDao;
    @Mock
    private VmDao vmDao;
    @Mock
    private NetworkDao networkDao;
    @Mock
    private ClusterFeatureDao clusterFeatureDao;
    @Mock
    private SupportedHostFeatureDao hostFeatureDao;
    @Mock
    private DefaultManagementNetworkFinder defaultManagementNetworkFinder;
    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Mock
    private ClusterCpuFlagsManager clusterCpuFlagsManager;
    @Mock
    private MoveMacs moveMacs;
    @Mock
    private SchedulingManager schedulingManager;
    @Mock
    private InClusterUpgradeValidator inClusterUpgradeValidator;
    @Mock
    private VmNumaNodeDao vmNumaNodeDao;
    @Mock
    private VmDynamicDao vmDynamicDao;
    @Mock
    private AuditLogDirector auditLogDirector;

    @Mock
    private Network mockManagementNetwork;
    private Guid managementNetworkId;

    @Spy
    @InjectMocks
    private UpdateClusterCommand<ClusterOperationParameters> cmd =
            new UpdateClusterCommand<>(new ClusterOperationParameters(), null);

    @Test
    public void nameInUse() {
        createSimpleCommand();
        createCommandWithDifferentName();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_DO_ACTION_NAME_IN_USE);
    }

    @Test
    public void invalidCluster() {
        createSimpleCommand();
        when(clusterDao.get(any())).thenReturn(null);
        validateFailedWithReason(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void legalArchitectureChange() {
        createCommandWithDefaultCluster();
        cpuExists();
        architectureIsUpdatable();
        initAndAssertValidation(true);
    }

    private void initAndAssertValidation(boolean value) {
        cmd.init();
        assertThat(cmd.validate(), is(value));
    }

    @Test
    public void illegalArchitectureChange() {
        createCommandWithDefaultCluster();
        clusterHasVMs();
        cpuExists();
        architectureIsNotUpdatable();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_ARCHITECTURE_ILLEGAL);
    }

    @Test
    public void illegalCpuUpdate() {
        createCommandWithDifferentCpuName();
        cpuManufacturersMatch();
        cpuExists();
        clusterHasVMs();
        clusterHasVds();
        architectureIsUpdatable();
        validateFailedWithReason(EngineMessage.CLUSTER_CPU_IS_NOT_UPDATABLE);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void legalCpuUpdate() {
        createCommandWithDifferentCpuName();
        cpuExists();
        architectureIsUpdatable();
        initAndAssertValidation(true);
    }

    @Test
    public void invalidCpuSelection() {
        createCommandWithDefaultCluster();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_ILLEGAL);
    }

    @Test
    public void illegalCpuChange() {
        createCommandWithDefaultCluster();
        cpuExists();
        cpuManufacturersDontMatch();
        clusterHasVds();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_ILLEGAL);
    }

    @Test
    public void invalidVersion() {
        createCommandWithInvalidVersion();
        setupCpu();
        validateFailedWithReason(EngineMessage.ACTION_TYPE_FAILED_GIVEN_VERSION_NOT_SUPPORTED);
    }

    @Test
    public void versionDecreaseWithHost() {
        createCommandWithOlderVersion();
        setupCpu();
        vdsExist();
        validateFailedWithReason(EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_CLUSTER_WITH_HOSTS_COMPATIBILITY_VERSION);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void versionDecreaseNoHostsOrNetwork() {
        createCommandWithOlderVersion();
        setupCpu();
        when(storagePoolDao.get(any())).thenReturn(createStoragePoolLocalFS());
        initAndAssertValidation(true);
    }

    @Test
    public void versionDecreaseLowerVersionThanDC() {
        createCommandWithOlderVersion();
        when(storagePoolDao.get(any())).thenReturn(createStoragePoolLocalFSOldVersion());
        setupCpu();
        validateFailedWithReason(EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION_UNDER_DC);
    }

    @Test
    public void updateWithLowerVersionThanHosts() {
        createCommandWithDefaultCluster();
        setupCpu();
        vdsExistWithHigherVersion();
        architectureIsUpdatable();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS);
    }

    @Test
    public void updateWithCpuLowerThanHost() {
        createCommandWithDefaultCluster();
        setupCpu();
        clusterHasVds();
        cpuFlagsMissing();
        architectureIsUpdatable();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_WITH_HOSTS_MISSING_FLAGS);
    }

    @Test
    public void updateStoragePool() {
        createCommandWithDifferentPool();
        setupCpu();
        clusterHasVds();
        cpuFlagsNotMissing();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_CHANGE_STORAGE_POOL);
    }

    @Test
    public void clusterAlreadyInLocalFs() {
        newDefaultManagementNetworkFound();

        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        setupCpu();
        storagePoolAlreadyHasCluster();
        architectureIsUpdatable();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
    }

    @Test
    public void clusterMovesToDcWithNoDefaultManagementNetwork() {
        noNewDefaultManagementNetworkFound();

        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        setupCpu();
        validateFailedWithReason(EngineMessage.ACTION_TYPE_FAILED_DEFAULT_MANAGEMENT_NETWORK_NOT_FOUND);
    }

    @Test
    public void detachedClusterMovesToDcWithNonExistentManagementNetwork() {
        managementNetworkNotFoundById();

        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        setupCpu();

        validateFailedWithReason(EngineMessage.NETWORK_NOT_EXISTS);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterAndDetachedClusterMovesToDc")
    public void detachedClusterMovesToDcWithExistingManagementNetwork() {

        newDefaultManagementNetworkFound();

        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        setupCpu();
        storagePoolIsLocalFS();

        initAndAssertValidation(true);
    }

    public static Stream<MockConfigDescriptor<?>> configForClusterWithVirtGlusterAndDetachedClusterMovesToDc() {
        return Stream.concat(
                Stream.concat(mockConfiguration(),
                        Stream.of(MockConfigDescriptor.of(ConfigValues.AllowClusterWithVirtGlusterEnabled,
                                Boolean.FALSE))),
                Stream.of(MockConfigDescriptor.of(ConfigValues.AutoRegistrationDefaultClusterID, Guid.Empty)));
    }

    public static Stream<MockConfigDescriptor<?>> configForDetachedClusterMovesToDcWithExistingManagementNetwork() {
        return Stream.concat(mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(ConfigValues.AutoRegistrationDefaultClusterID, Guid.Empty)));
    }

    private void managementNetworkNotFoundById() {
        managementNetworkId = TEST_MANAGEMENT_NETWORK_ID;
        when(networkDao.get(TEST_MANAGEMENT_NETWORK_ID)).thenReturn(null);
    }

    private void setupCpu() {
        cpuExists();
        cpuManufacturersMatch();
    }

    @Test
    @MockedConfig("configForDefaultClusterInLocalFs")
    public void defaultClusterInLocalFs() {
        newDefaultManagementNetworkFound();

        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        setupCpu();
        architectureIsUpdatable();
        validateFailedWithReason(EngineMessage.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
    }

    public static Stream<MockConfigDescriptor<?>> configForDefaultClusterInLocalFs() {
        return Stream.concat(mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(ConfigValues.AutoRegistrationDefaultClusterID, DEFAULT_CLUSTER_ID)));
    }

    private void newDefaultManagementNetworkFound() {
        managementNetworkId = null;
        when(defaultManagementNetworkFinder.findDefaultManagementNetwork(DC_ID1)).
                thenReturn(mockManagementNetwork);
    }

    private void noNewDefaultManagementNetworkFound() {
        managementNetworkId = null;
        when(defaultManagementNetworkFinder.findDefaultManagementNetwork(DC_ID1)).
                thenReturn(null);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void clusterWithNoCpu() {
        createCommandWithNoCpuName();
        when(clusterDao.get(any())).thenReturn(createClusterWithNoCpuName());
        initAndAssertValidation(true);
    }

    @Test
    public void clusterWithNoServiceEnabled() {
        createCommandWithNoService();
        when(clusterDao.get(any())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        validateFailedWithReason(EngineMessage.CLUSTER_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void clusterWithVirtGlusterServicesNotAllowed() {
        createCommandWithVirtGlusterEnabled();
        when(clusterDao.get(any())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        validateFailedWithReason(EngineMessage.CLUSTER_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED);
    }


    public static Stream<MockConfigDescriptor<?>> configForClusterWithVirtGlusterServicesNotAllowed() {
        return Stream.concat(mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(ConfigValues.AllowClusterWithVirtGlusterEnabled, Boolean.FALSE)));
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void disableVirtWhenVmsExist() {
        createCommandWithGlusterEnabled();
        when(clusterDao.get(any())).thenReturn(createDefaultCluster());
        cpuExists();
        cpuFlagsNotMissing();
        clusterHasVds();
        clusterHasVMs();

        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_DISABLE_VIRT_WHEN_CLUSTER_CONTAINS_VMS);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void disableGlusterWhenVolumesExist() {
        createCommandWithVirtEnabled();
        when(clusterDao.get(any())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        cpuFlagsNotMissing();
        clusterHasGlusterVolumes();

        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_DISABLE_GLUSTER_WHEN_CLUSTER_CONTAINS_VOLUMES);
    }

    @Test
    public void enableNewAddtionalFeatureWhenHostDoesnotSupport() {
        createCommandWithAddtionalFeature();
        when(clusterDao.get(any())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        cpuFlagsNotMissing();
        clusterHasVds();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_UPDATE_SUPPORTED_FEATURES_WITH_LOWER_HOSTS);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void enableNewAddtionalFeatureWhenHostSupports() {
        createCommandWithAddtionalFeature();
        when(clusterDao.get(any())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        cpuFlagsNotMissing();
        clusterHasVds();
        when(hostFeatureDao.getSupportedHostFeaturesByHostId(any())).thenReturn(Collections.singleton("TEST_FEATURE"));
        initAndAssertValidation(true);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void shouldCheckIfClusterCanBeUpgraded() {
        createCommandWithDefaultCluster();
        cpuExists();
        architectureIsUpdatable();
        cmd.getCluster().setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        initAndAssertValidation(true);
        verify(inClusterUpgradeValidator, times(1)).isUpgradePossible(any(), any());
        verify(inClusterUpgradeValidator, times(0)).isUpgradeDone(any());
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void shouldCheckIfClusterUpgradeIsDone() {
        createCommandWithDefaultCluster();
        Cluster oldCluster = oldGroupFromDb();
        cpuExists();
        architectureIsUpdatable();
        oldCluster.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        cmd.getCluster().setClusterPolicyId(NOT_UPGRADE_POLICY_GUID);
        initAndAssertValidation(true);
        verify(inClusterUpgradeValidator, times(0)).isUpgradePossible(any(), any());
        verify(inClusterUpgradeValidator, times(1)).isUpgradeDone(any());
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void shouldStayInUpgradeMode() {
        createCommandWithDefaultCluster();
        Cluster oldCluster = oldGroupFromDb();
        cpuExists();
        architectureIsUpdatable();
        oldCluster.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        cmd.getCluster().setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        initAndAssertValidation(true);
        verify(inClusterUpgradeValidator, times(0)).isUpgradePossible(any(), any());
        verify(inClusterUpgradeValidator, times(0)).isUpgradeDone(any());
    }

    @Test
    public void vmsAreUpdatedByTheOrderOfTheirIds() {
        final Cluster newerCluster = createDefaultCluster();
        newerCluster.setCompatibilityVersion(new Version(1, 2));
        createCommand(newerCluster);
        VmStatic vm1 = new VmStatic();
        vm1.setId(VM_ID1);
        VmStatic vm2 = new VmStatic();
        vm2.setId(VM_ID2);
        VmStatic vm3 = new VmStatic();
        vm3.setId(VM_ID3);
        when(vmStaticDao.getAllByCluster(any())).thenReturn(Arrays.asList(vm1, vm2, vm3));
        cmd.init();
        // the VMs ordered by Guids: v2, v3, v1
        assertEquals(Arrays.asList(vm2, vm3, vm1), cmd.filterVmsInClusterNeedUpdate());
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void hwrngAdditionalRngDeviceUsed() {
        final Cluster cluster = createDefaultCluster();
        cluster.setAdditionalRngSources(Collections.singleton(VmRngDevice.Source.HWRNG));
        createCommand(cluster);
        cpuExists();
        initAndAssertValidation(true);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void randomAdditionalRngDeviceUsed() {
        final Cluster cluster = createDefaultCluster();
        cluster.setAdditionalRngSources(Collections.singleton(VmRngDevice.Source.RANDOM));
        createCommand(cluster);
        cpuExists();
        validateFailedWithReason(EngineMessage.ACTION_TYPE_FAILED_RANDOM_RNG_SOURCE_CANT_BE_ADDED_TO_CLUSTER_ADDITIONAL_RNG_SOURCES);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void memoryOptimizationWithoutKsmOrBallooning() {
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(150);
        cluster.setEnableKsm(false);
        cluster.setEnableBallooning(false);
        createCommand(cluster);
        cpuExists();
        validateFailedWithReason(EngineMessage.CLUSTER_TO_ALLOW_MEMORY_OPTIMIZATION_YOU_MUST_ALLOW_KSM_OR_BALLOONING);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void memoryOptimizationLowerThenZeroWithoutKsmOrBallooning() {
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(-52);
        cluster.setEnableKsm(false);
        cluster.setEnableBallooning(false);
        createCommand(cluster);
        cpuExists();
        initAndAssertValidation(true);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void memoryOptimizationWithoutBallooning() {
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(0);
        cluster.setEnableKsm(true);
        cluster.setEnableBallooning(false);
        createCommand(cluster);
        cpuExists();
        initAndAssertValidation(true);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void memoryOptimizationWithoutKsm() {
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(200);
        cluster.setEnableKsm(false);
        cluster.setEnableBallooning(true);
        createCommand(cluster);
        cpuExists();
        initAndAssertValidation(true);
    }

    @Test
    @MockedConfig("configForClusterWithVirtGlusterServicesNotAllowed")
    public void memoryOptimizationWithKsmAndBallooning() {
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(200);
        cluster.setEnableKsm(true);
        cluster.setEnableBallooning(true);
        createCommand(cluster);
        cpuExists();
        initAndAssertValidation(true);
    }

    private void createSimpleCommand() {
        createCommand(createNewCluster());
    }

    private void createCommandWithOlderVersion() {
        createCommand(createClusterWithOlderVersion());
    }

    private void createCommandWithInvalidVersion() {
        createCommand(createClusterWithBadVersion());
    }

    private void createCommandWithDifferentPool() {
        createCommand(createClusterWithDifferentPool());
    }

    private void createCommandWithDefaultCluster() {
        createCommand(createDefaultCluster());
    }

    private void createCommandWithDifferentCpuName() {
        createCommand(createDefaultClusterWithDifferentCpuName());
    }

    private void createCommandWithNoCpuName() {
        createCommand(createClusterWithNoCpuName());
    }

    private void createCommandWithNoService() {
        createCommand(createClusterWith(false, false));
    }

    private void createCommandWithVirtEnabled() {
        createCommand(createClusterWith(true, false));
    }

    private void createCommandWithAddtionalFeature() {
        createCommand(createClusterWithAddtionalFeature());
    }

    private void createCommandWithGlusterEnabled() {
        createCommand(createClusterWith(false, true));
    }

    private void createCommandWithVirtGlusterEnabled() {
        createCommand(createClusterWith(true, true));
    }

    private void createCommand(final Cluster group) {
        cmd.getParameters().setManagementNetworkId(managementNetworkId);
        cmd.getParameters().setCluster(group);
        cmd.setClusterId(group.getId());

        doReturn(true).when(cmd).isSupportedEmulatedMachinesMatchClusterLevel(any());

        // cluster upgrade
        doReturn(new ClusterPolicy()).when(schedulingManager).getClusterPolicy(any(Guid.class));
        final ClusterPolicy clusterPolicy = new ClusterPolicy();
        clusterPolicy.setId(ClusterPolicy.UPGRADE_POLICY_GUID);
        doReturn(clusterPolicy).when(schedulingManager).getClusterPolicy(eq(ClusterPolicy.UPGRADE_POLICY_GUID));

        if (StringUtils.isEmpty(group.getCpuName())) {
            doReturn(ArchitectureType.undefined).when(cmd).getArchitecture();
        } else {
            doReturn(ArchitectureType.x86_64).when(cmd).getArchitecture();
        }

        when(clusterDao.get(any())).thenReturn(createDefaultCluster());
        when(clusterDao.getByName(any())).thenReturn(createDefaultCluster());
        List<Cluster> clusterList = new ArrayList<>();
        clusterList.add(createDefaultCluster());
        when(clusterDao.getByName(any(), anyBoolean())).thenReturn(clusterList);
    }

    private void createCommandWithDifferentName() {
        createCommand(createClusterWithDifferentName());
    }

    private static Cluster createClusterWithDifferentName() {
        Cluster group = new Cluster();
        group.setName("BadName");
        group.setCompatibilityVersion(VERSION_1_1);
        group.setClusterPolicyId(Guid.newGuid());
        return group;
    }

    private static Cluster createNewCluster() {
        Cluster group = new Cluster();
        group.setCompatibilityVersion(VERSION_1_1);
        group.setName("Default");
        group.setClusterPolicyId(Guid.newGuid());
        return group;
    }

    private static Cluster createDefaultCluster() {
        Cluster group = new Cluster();
        group.setName("Default");
        group.setId(DEFAULT_CLUSTER_ID);
        group.setCpuName("Intel Conroe");
        group.setCompatibilityVersion(VERSION_1_1);
        group.setStoragePoolId(DC_ID1);
        group.setArchitecture(ArchitectureType.x86_64);
        group.setClusterPolicyId(Guid.newGuid());
        group.setMaxVdsMemoryOverCommit(100);
        group.setBiosType(BiosType.I440FX_SEA_BIOS);
        return group;
    }

    private static Cluster createDefaultClusterWithDifferentCpuName() {
        Cluster group = createDefaultCluster();

        group.setCpuName("Another CPU name");

        return group;
    }

    private static Cluster createClusterWithNoCpuName() {
        Cluster group = new Cluster();
        group.setName("Default");
        group.setCpuName("");
        group.setId(DEFAULT_CLUSTER_ID);
        group.setCompatibilityVersion(VERSION_1_1);
        group.setStoragePoolId(DC_ID1);
        group.setArchitecture(ArchitectureType.undefined);
        group.setClusterPolicyId(Guid.newGuid());
        group.setMaxVdsMemoryOverCommit(100);
        return group;
    }

    private static Cluster createDetachedDefaultCluster() {
        Cluster group = createDefaultCluster();
        group.setStoragePoolId(null);
        return group;
    }

    private static Cluster createClusterWithOlderVersion() {
        Cluster group = createNewCluster();
        group.setCompatibilityVersion(VERSION_1_0);
        group.setStoragePoolId(DC_ID1);
        group.setVirtService(true);
        group.setGlusterService(false);
        group.setMaxVdsMemoryOverCommit(100);
        return group;
    }

    private static Cluster createClusterWithBadVersion() {
        Cluster group = createNewCluster();
        group.setCompatibilityVersion(new Version(5, 0));
        return group;
    }

    private static Cluster createClusterWithDifferentPool() {
        Cluster group = createNewCluster();
        group.setStoragePoolId(DC_ID2);
        return group;
    }

    private static Cluster createClusterWith(boolean virtService, boolean glusterService) {
        Cluster group = createDefaultCluster();
        group.setVirtService(virtService);
        group.setGlusterService(glusterService);
        group.setCompatibilityVersion(VERSION_1_1);
        return group;
    }

    private static Cluster createClusterWithAddtionalFeature() {
        Cluster group = createDefaultCluster();
        group.setCompatibilityVersion(VERSION_1_1);
        Set<SupportedAdditionalClusterFeature> addtionalFeaturesSupported = new HashSet<>();
        AdditionalFeature feature =
                new AdditionalFeature(DEFAULT_FEATURE_ID,
                        "TEST_FEATURE",
                        VERSION_1_1,
                        "Test Feature",
                        ApplicationMode.AllModes);
        addtionalFeaturesSupported.add(new SupportedAdditionalClusterFeature(group.getId(), true, feature));
        group.setAddtionalFeaturesSupported(addtionalFeaturesSupported);
        return group;
    }

    private static StoragePool createStoragePoolLocalFSOldVersion() {
        StoragePool pool = new StoragePool();
        pool.setIsLocal(true);
        pool.setCompatibilityVersion(VERSION_1_2);
        return pool;
    }

    private static StoragePool createStoragePoolLocalFS() {
        StoragePool pool = new StoragePool();
        pool.setIsLocal(true);
        return pool;
    }

    private void storagePoolIsLocalFS() {
        when(storagePoolDao.get(DC_ID1)).thenReturn(createStoragePoolLocalFS());
    }

    private void oldGroupIsDetachedDefault() {
        when(clusterDao.get(DEFAULT_CLUSTER_ID)).thenReturn(createDetachedDefaultCluster());
    }

    private Cluster oldGroupFromDb() {
        final Cluster vdsGroup = createDefaultCluster();
        when(clusterDao.get(DEFAULT_CLUSTER_ID)).thenReturn(vdsGroup);
        return vdsGroup;
    }

    private void storagePoolAlreadyHasCluster() {
        Cluster group = new Cluster();
        List<Cluster> groupList = new ArrayList<>();
        groupList.add(group);
        when(clusterDao.getAllForStoragePool(any())).thenReturn(groupList);
    }

    private void vdsExist() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        List<VDS> vdsList = new ArrayList<>();
        vdsList.add(vds);
        when(vdsDao.getAllForCluster(any())).thenReturn(vdsList);
    }

    private void vdsExistWithHigherVersion() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        vds.setClusterCompatibilityVersion(VERSION_1_2);
        List<VDS> vdsList = new ArrayList<>();
        vdsList.add(vds);
        when(vdsDao.getAllForCluster(any())).thenReturn(vdsList);
    }

    private void clusterHasVds() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        vds.setSupportedClusterLevels(VERSION_1_1.toString());
        List<VDS> vdsList = new ArrayList<>();
        vdsList.add(vds);
        when(vdsDao.getAllForCluster(any())).thenReturn(vdsList);
    }

    private void clusterHasGlusterVolumes() {
        List<GlusterVolumeEntity> volumes = new ArrayList<>();
        volumes.add(new GlusterVolumeEntity());
        when(glusterVolumeDao.getByClusterId(any())).thenReturn(volumes);
    }

    private void clusterHasVMs() {
        VM vm = new VM();
        vm.setClusterId(DEFAULT_CLUSTER_ID);
        List<VM> vmList = new ArrayList<>();
        vmList.add(vm);

        when(vmDao.getAllForCluster(any())).thenReturn(vmList);
    }

    private void cpuFlagsMissing() {
        List<String> strings = new ArrayList<>();
        strings.add("foo");
        doReturn(strings).when(cmd).missingServerCpuFlags(any());
    }

    private void cpuFlagsNotMissing() {
        doReturn(Collections.emptyList()).when(cmd).missingServerCpuFlags(any());
    }

    private void cpuManufacturersDontMatch() {
        doReturn(false).when(cmd).checkIfCpusSameManufacture(any());
    }

    private void cpuManufacturersMatch() {
        doReturn(true).when(cmd).checkIfCpusSameManufacture(any());
    }

    private void cpuExists() {
        doReturn(true).when(cmd).checkIfCpusExist();
    }

    private void architectureIsUpdatable() {
        doReturn(true).when(cmd).isArchitectureUpdatable();
    }

    private void architectureIsNotUpdatable() {
        doReturn(false).when(cmd).isArchitectureUpdatable();
    }

    private void validateFailedWithReason(final EngineMessage message) {
        initAndAssertValidation(false);
        ValidateTestUtils.assertValidationMessages("Wrong validation message", cmd, message);
    }
    // --- i440fx raise-time migration (validateI440fxRaise / pinI440fxEntitiesOnRaise) ---

    private static Cluster createCluster(Version version, String emulatedMachine) {
        Cluster cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setName("raise-test-cluster");
        cluster.setCompatibilityVersion(version);
        cluster.setEmulatedMachine(emulatedMachine);
        return cluster;
    }

    private static VmStatic createI440fxVm(String name) {
        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());
        vm.setName(name);
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        vm.setCustomEmulatedMachine("");
        return vm;
    }

    private static VmTemplate createI440fxTemplate(String name) {
        VmTemplate template = new VmTemplate();
        template.setId(Guid.newGuid());
        template.setName(name);
        template.setBiosType(BiosType.I440FX_SEA_BIOS);
        template.setCustomEmulatedMachine("");
        return template;
    }

    private static VDS hostWithMachines(String machines) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setSupportedEmulatedMachines(machines);
        return vds;
    }

    /**
     * Prepares a raise command (oldCluster from the DAO, new cluster in the parameters)
     * and runs init(), so vmsLockedForUpdate/templatesLockedForUpdate are populated like
     * in the real flow.
     */
    private void createRaiseCommand(Cluster oldCluster, Cluster newCluster,
            List<VmStatic> vms, List<VmTemplate> templates) {
        newCluster.setId(oldCluster.getId());
        cmd.getParameters().setCluster(newCluster);
        cmd.setClusterId(oldCluster.getId());
        when(clusterDao.get(any())).thenReturn(oldCluster);
        when(vmStaticDao.getAllByCluster(any())).thenReturn(vms);
        when(vmTemplateDao.getAllForCluster(any())).thenReturn(templates);
        cmd.init();
    }

    private static VmDynamic dynamicWithEmulatedMachine(VmStatic vm, String emulatedMachine) {
        VmDynamic dynamic = new VmDynamic();
        dynamic.setId(vm.getId());
        dynamic.setEmulatedMachine(emulatedMachine);
        return dynamic;
    }

    private void verifyNoPinningWrites() {
        // The raise never writes VM data itself: pins are persisted by the standard
        // UpdateVm/UpdateVmTemplate flow, compensation-enabled.
        verify(vmStaticDao, never()).update(any(VmStatic.class));
        verify(vmTemplateDao, never()).update(any(VmTemplate.class));
    }

    @Test
    public void raisePreservesRunnablePinOnAllEl9Cluster() {
        VmStatic downVm = createI440fxVm("down-vm");
        VmTemplate template = createI440fxTemplate("tpl");
        createRaiseCommand(
                createCluster(VERSION_4_7, DUAL_DEFAULT),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(downVm),
                Collections.singletonList(template));
        when(vdsDao.getClusterI440fxCommonMachines(any()))
                .thenReturn(Collections.singletonList(PC_I440FX_RHEL));
        when(vdsDao.getAllForCluster(any()))
                .thenReturn(Collections.singletonList(hostWithMachines(PC_I440FX_RHEL + "," + PC_Q35_RHEL)));

        assertTrue(cmd.validateI440fxRaise(), "raise with pinnable entities must pass validation");
        cmd.pinI440fxEntitiesOnRaise();
        cmd.auditI440fxRaiseMigration();

        assertEquals(PC_I440FX_RHEL, downVm.getCustomEmulatedMachine(),
                "down VM must keep the machine type it effectively uses");
        assertEquals(PC_I440FX_RHEL, template.getCustomEmulatedMachine());
        verifyNoPinningWrites();
        verify(auditLogDirector, times(2)).log(any(AuditLogable.class),
                eq(AuditLogType.CLUSTER_UPDATE_I440FX_VM_PINNED));
        verify(auditLogDirector, never()).log(any(AuditLogable.class),
                eq(AuditLogType.CLUSTER_UPDATE_I440FX_VM_RETARGETED));
        verify(auditLogDirector).log(any(AuditLogable.class),
                eq(AuditLogType.CLUSTER_UPDATE_I440FX_DEFAULT_MIGRATED));
    }

    @Test
    public void raisePreservesRunningVmsMachineFromVmDynamic() {
        VmStatic runningVm = createI440fxVm("running-vm");
        createRaiseCommand(
                createCluster(VERSION_4_7, DUAL_DEFAULT),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(runningVm),
                Collections.emptyList());
        when(vdsDao.getClusterI440fxCommonMachines(any()))
                .thenReturn(Collections.singletonList(PC_I440FX_RHEL));
        when(vmDynamicDao.get(runningVm.getId()))
                .thenReturn(dynamicWithEmulatedMachine(runningVm, "pc-i440fx-rhel9.4.0"));
        // the host must support what the VM is running, or the pin is dead and gets retargeted
        when(vdsDao.getAllForCluster(any()))
                .thenReturn(Collections.singletonList(
                        hostWithMachines("pc-i440fx-rhel9.4.0," + PC_I440FX_RHEL + "," + PC_Q35_RHEL)));

        assertTrue(cmd.validateI440fxRaise());
        cmd.pinI440fxEntitiesOnRaise();

        assertEquals("pc-i440fx-rhel9.4.0", runningVm.getCustomEmulatedMachine(),
                "a running VM must keep what it is actually running (vm_dynamic)");
        verifyNoPinningWrites();
    }

    @Test
    public void raiseRetargetsDeadPinToNewestCommonMachine() {
        // Hosts were refreshed to EL10 before the raise; the stored dual default still
        // says pc-i440fx-rhel7.6.0, which no host supports: dead hardware is revived with
        // the newest machine type common to all hosts, loudly.
        VmStatic downVm = createI440fxVm("dead-vm");
        createRaiseCommand(
                createCluster(VERSION_4_7, DUAL_DEFAULT),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(downVm),
                Collections.emptyList());
        when(vdsDao.getClusterI440fxCommonMachines(any()))
                .thenReturn(Arrays.asList(PC_I440FX_RHEL10));
        when(vdsDao.getAllForCluster(any()))
                .thenReturn(Collections.singletonList(
                        hostWithMachines(PC_I440FX_RHEL10 + ",pc-q35-rhel10.0.0")));

        assertTrue(cmd.validateI440fxRaise(),
                "a dead pin with a common machine type must not fail the raise");
        cmd.pinI440fxEntitiesOnRaise();
        cmd.auditI440fxRaiseMigration();

        assertEquals(PC_I440FX_RHEL10, downVm.getCustomEmulatedMachine(),
                "dead machine type must be retargeted to the newest common type");
        verifyNoPinningWrites();
        verify(auditLogDirector).log(any(AuditLogable.class),
                eq(AuditLogType.CLUSTER_UPDATE_I440FX_VM_RETARGETED));
        verify(auditLogDirector, never()).log(any(AuditLogable.class),
                eq(AuditLogType.CLUSTER_UPDATE_I440FX_VM_PINNED));
    }

    @Test
    public void raisePreservesPinRunnableOnSomeHostsInMixedCluster() {
        // Mixed EL9/EL10 cluster: the VM's machine type runs on the EL9 hosts, so the
        // raise proceeds and keeps it — even though no i440fx type is common to all
        // hosts (there is nothing to fix; the VM keeps working where it does).
        VmStatic vm = createI440fxVm("mixed-vm");
        createRaiseCommand(
                createCluster(VERSION_4_7, DUAL_DEFAULT),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(vm),
                Collections.emptyList());
        when(vdsDao.getClusterI440fxCommonMachines(any())).thenReturn(Collections.emptyList());
        when(vdsDao.getAllForCluster(any())).thenReturn(Arrays.asList(
                hostWithMachines(PC_I440FX_RHEL + ",pc-q35-rhel9.6.0"),
                hostWithMachines(PC_I440FX_RHEL10 + ",pc-q35-rhel10.0.0")));

        assertTrue(cmd.validateI440fxRaise(),
                "a machine type runnable on some host must not fail the raise");
        cmd.pinI440fxEntitiesOnRaise();

        assertEquals(PC_I440FX_RHEL, vm.getCustomEmulatedMachine(),
                "a machine type some host can run must be preserved, not retargeted");
        verifyNoPinningWrites();
    }

    @Test
    public void raiseRetargetsUnderivablePinWhenCommonExists() {
        // Single-Q35 cluster default + down VM: nothing can be derived, but the hosts
        // share a common i440fx type — the VM is revived with it.
        VmStatic downVm = createI440fxVm("underivable-vm");
        createRaiseCommand(
                createCluster(VERSION_4_7, PC_Q35_RHEL),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(downVm),
                Collections.emptyList());
        when(vdsDao.getClusterI440fxCommonMachines(any()))
                .thenReturn(Collections.singletonList(PC_I440FX_RHEL10));
        when(vdsDao.getAllForCluster(any()))
                .thenReturn(Collections.singletonList(hostWithMachines(PC_I440FX_RHEL10)));

        assertTrue(cmd.validateI440fxRaise());
        cmd.pinI440fxEntitiesOnRaise();

        assertEquals(PC_I440FX_RHEL10, downVm.getCustomEmulatedMachine(),
                "an underivable pin must be revived from the common machine types");
        verifyNoPinningWrites();
    }

    @Test
    public void raiseFailsWhenEntityCannotBeFixed() {
        // Dead/un-derivable machine type AND no common i440fx type (mixed-host cluster):
        // the only state the raise refuses — the remedy is Q35 conversion.
        VmStatic downVm = createI440fxVm("unfixable-vm");
        VmTemplate template = createI440fxTemplate("unfixable-tpl");
        createRaiseCommand(
                createCluster(VERSION_4_7, PC_Q35_RHEL),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(downVm),
                Collections.singletonList(template));
        when(vdsDao.getClusterI440fxCommonMachines(any())).thenReturn(Collections.emptyList());
        when(vdsDao.getAllForCluster(any())).thenReturn(Arrays.asList(
                hostWithMachines(PC_I440FX_RHEL + ",pc-q35-rhel9.6.0"),
                hostWithMachines(PC_I440FX_RHEL10 + ",pc-q35-rhel10.0.0")));

        assertFalse(cmd.validateI440fxRaise(),
                "raise must fail when an entity can neither keep nor get a machine type");
        ValidateTestUtils.assertValidationMessages("Wrong validation message", cmd,
                EngineMessage.CLUSTER_CANNOT_UPDATE_COMPATIBILITY_VERSION_NO_COMMON_I440FX_MACHINE);
        assertNull(downVm.getCustomEmulatedMachine(),
                "a failed validation must not have pinned anything");
    }

    @Test
    public void raiseLeavesPinnedEntitiesUntouched() {
        VmStatic pinnedVm = createI440fxVm("pinned-vm");
        pinnedVm.setCustomEmulatedMachine(PC_I440FX_RHEL);
        createRaiseCommand(
                createCluster(VERSION_4_7, DUAL_DEFAULT),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(pinnedVm),
                Collections.emptyList());

        assertTrue(cmd.validateI440fxRaise());
        cmd.pinI440fxEntitiesOnRaise();
        cmd.auditI440fxRaiseMigration();

        assertEquals(PC_I440FX_RHEL, pinnedVm.getCustomEmulatedMachine());
        verifyNoPinningWrites();
        verify(auditLogDirector).log(any(AuditLogable.class),
                eq(AuditLogType.CLUSTER_UPDATE_I440FX_DEFAULT_MIGRATED));
        verify(auditLogDirector, never()).log(any(AuditLogable.class),
                eq(AuditLogType.CLUSTER_UPDATE_I440FX_VM_PINNED));
    }

    @Test
    public void raisePinsHostedEngineVm() {
        // Pinning preserves hardware — the Hosted Engine VM must be pinned like any
        // other, leaving it unpinned is what would break it after the raise.
        VmStatic heVm = createI440fxVm("he-vm");
        heVm.setOrigin(OriginType.HOSTED_ENGINE);
        createRaiseCommand(
                createCluster(VERSION_4_7, DUAL_DEFAULT),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(heVm),
                Collections.emptyList());
        when(vdsDao.getClusterI440fxCommonMachines(any()))
                .thenReturn(Collections.singletonList(PC_I440FX_RHEL));
        when(vdsDao.getAllForCluster(any()))
                .thenReturn(Collections.singletonList(hostWithMachines(PC_I440FX_RHEL)));

        assertTrue(cmd.validateI440fxRaise(), "the HE VM must be included in the validate list");
        cmd.pinI440fxEntitiesOnRaise();

        assertEquals(PC_I440FX_RHEL, heVm.getCustomEmulatedMachine(),
                "Hosted Engine VM must be pinned at raise time");
        verifyNoPinningWrites();
    }

    @Test
    public void raiseBelow48DoesNotPin() {
        VmStatic unpinnedVm = createI440fxVm("old-vm");
        createRaiseCommand(
                createCluster(VERSION_4_5, DUAL_DEFAULT),
                createCluster(VERSION_4_7, null),
                Collections.singletonList(unpinnedVm),
                Collections.emptyList());

        assertTrue(cmd.validateI440fxRaise());
        cmd.pinI440fxEntitiesOnRaise();
        cmd.auditI440fxRaiseMigration();

        assertNull(unpinnedVm.getCustomEmulatedMachine(),
                "a raise that stays below 4.8 must not pin anything");
        verifyNoPinningWrites();
        verify(vdsDao, never()).getClusterI440fxCommonMachines(any());
        verifyNoMoreInteractions(auditLogDirector);
    }

    @Test
    public void downgradeIsNoOp() {
        VmStatic unpinnedVm = createI440fxVm("vm");
        createRaiseCommand(
                createCluster(VERSION_4_8, PC_Q35_RHEL),
                createCluster(VERSION_4_7, null),
                Collections.singletonList(unpinnedVm),
                Collections.emptyList());

        assertTrue(cmd.validateI440fxRaise());
        cmd.pinI440fxEntitiesOnRaise();
        cmd.auditI440fxRaiseMigration();

        assertNull(unpinnedVm.getCustomEmulatedMachine(),
                "a downgrade must not touch anything");
        verifyNoPinningWrites();
        verifyNoMoreInteractions(auditLogDirector);
    }

    @Test
    public void raiseWithoutUnpinnedEntitiesSkipsCommonMachinesCheck() {
        VmStatic pinnedVm = createI440fxVm("pinned-vm");
        pinnedVm.setCustomEmulatedMachine(PC_I440FX_RHEL);
        createRaiseCommand(
                createCluster(VERSION_4_7, DUAL_DEFAULT),
                createCluster(VERSION_4_8, null),
                Collections.singletonList(pinnedVm),
                Collections.emptyList());

        assertTrue(cmd.validateI440fxRaise());
        verify(vdsDao, never()).getClusterI440fxCommonMachines(any());
    }
}
