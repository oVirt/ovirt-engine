package org.ovirt.engine.core.bll;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.cluster.DefaultManagementNetworkFinder;
import org.ovirt.engine.core.bll.network.cluster.UpdateClusterNetworkClusterValidator;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class UpdateClusterCommandTest {

    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
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

    @Rule
    public MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.SupportedClusterLevels, versions));

    @Mock
    DbFacade dbFacadeMock;

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
    private UpdateClusterNetworkClusterValidator networkClusterValidator;
    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Mock
    private SchedulingManager schedulingManager;
    @Mock
    private InClusterUpgradeValidator inClusterUpgradeValidator;
    @Mock
    private VmNumaNodeDao vmNumaNodeDao;

    @Mock
    private Network mockManagementNetwork = createManagementNetwork();
    private Guid managementNetworkId;

    private Network createManagementNetwork() {
        final Network network = new Network();
        network.setId(TEST_MANAGEMENT_NETWORK_ID);
        return network;
    }

    @Spy
    @InjectMocks
    private UpdateClusterCommand<ManagementNetworkOnClusterOperationParameters> cmd =
            new UpdateClusterCommand<>(new ManagementNetworkOnClusterOperationParameters(), null);

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
        cpuIsNotUpdatable();
        cpuManufacturersMatch();
        cpuExists();
        clusterHasVMs();
        clusterHasVds();
        architectureIsUpdatable();
        validateFailedWithReason(EngineMessage.CLUSTER_CPU_IS_NOT_UPDATABLE);
    }

    @Test
    public void legalCpuUpdate() {
        createCommandWithDifferentCpuName();
        cpuExists();
        architectureIsUpdatable();
        initAndAssertValidation(true);
    }

    private void cpuIsNotUpdatable() {
        doReturn(false).when(cmd).isCpuUpdatable(any());
    }

    @Test
    public void invalidCpuSelection() {
        createCommandWithDefaultCluster();
        validateFailedWithReason(EngineMessage.ACTION_TYPE_FAILED_CPU_NOT_FOUND);
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
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS);
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
    public void detachedClusterMovesToDcWithExistingManagementNetwork() {

        newDefaultManagementNetworkFound();

        mcr.mockConfigValue(ConfigValues.AutoRegistrationDefaultClusterID, Guid.Empty);
        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        setupCpu();
        storagePoolIsLocalFS();

        initAndAssertValidation(true);
    }

    private void managementNetworkNotFoundById() {
        managementNetworkId = TEST_MANAGEMENT_NETWORK_ID;
        when(networkDao.get(TEST_MANAGEMENT_NETWORK_ID)).thenReturn(null);
    }

    @Test
    public void invalidDefaultManagementNetworkAttachement() {
        newDefaultManagementNetworkFound();
        final EngineMessage expected = EngineMessage.Unassigned;
        when(networkClusterValidator.managementNetworkChange()).thenReturn(new ValidationResult(expected));

        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        setupCpu();
        validateFailedWithReason(expected);
    }

    private void setupCpu() {
        cpuExists();
        cpuManufacturersMatch();
    }

    @Test
    public void defaultClusterInLocalFs() {
        newDefaultManagementNetworkFound();

        mcr.mockConfigValue(ConfigValues.AutoRegistrationDefaultClusterID, DEFAULT_CLUSTER_ID);
        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        setupCpu();
        architectureIsUpdatable();
        validateFailedWithReason(EngineMessage.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
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
    public void clusterWithVirtGlusterServicesNotAllowed() {
        createCommandWithVirtGlusterEnabled();
        when(clusterDao.get(any())).thenReturn(createClusterWithNoCpuName());
        mcr.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, Boolean.FALSE);
        cpuExists();
        validateFailedWithReason(EngineMessage.CLUSTER_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED);
    }

    @Test
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
    public void shouldCheckIfClusterCanBeUpgraded() {
        createCommandWithDefaultCluster();
        cpuExists();
        architectureIsUpdatable();
        cmd.getCluster().setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        initAndAssertValidation(true);
        verify(inClusterUpgradeValidator, times(1)).isUpgradePossible(anyList(), anyList());
        verify(inClusterUpgradeValidator, times(0)).isUpgradeDone(anyList());
    }

    @Test
    public void shouldCheckIfClusterUpgradeIsDone() {
        createCommandWithDefaultCluster();
        Cluster oldCluster = oldGroupFromDb();
        cpuExists();
        architectureIsUpdatable();
        oldCluster.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        cmd.getCluster().setClusterPolicyId(NOT_UPGRADE_POLICY_GUID);
        initAndAssertValidation(true);
        verify(inClusterUpgradeValidator, times(0)).isUpgradePossible(anyList(), anyList());
        verify(inClusterUpgradeValidator, times(1)).isUpgradeDone(anyList());
    }

    @Test
    public void shouldStayInUpgradeMode() {
        createCommandWithDefaultCluster();
        Cluster oldCluster = oldGroupFromDb();
        cpuExists();
        architectureIsUpdatable();
        oldCluster.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        cmd.getCluster().setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        initAndAssertValidation(true);
        verify(inClusterUpgradeValidator, times(0)).isUpgradePossible(anyList(), anyList());
        verify(inClusterUpgradeValidator, times(0)).isUpgradeDone(anyList());
    }

    @Test
    public void vmsAreUpdatedByTheOrderOfTheirIds() {
        final Cluster newerCluster = createDefaultCluster();
        newerCluster.setCompatibilityVersion(new Version(1, 2));
        createCommand(newerCluster);
        cmd.init();
        VmStatic vm1 = new VmStatic();
        vm1.setId(VM_ID1);
        VmStatic vm2 = new VmStatic();
        vm2.setId(VM_ID2);
        VmStatic vm3 = new VmStatic();
        vm3.setId(VM_ID3);
        when(vmStaticDao.getAllByCluster(any())).thenReturn(Arrays.asList(vm1, vm2, vm3));
        // the VMs ordered by Guids: v2, v3, v1
        assertEquals(Arrays.asList(vm2, vm3, vm1), cmd.filterVmsInClusterNeedUpdate());
    }

    @Test
    public void hwrngAdditionalRngDeviceUsed() {
        final Cluster cluster = createDefaultCluster();
        cluster.setAdditionalRngSources(Collections.singleton(VmRngDevice.Source.HWRNG));
        createCommand(cluster);
        cpuExists();
        initAndAssertValidation(true);
    }

    @Test
    public void randomAdditionalRngDeviceUsed() {
        final Cluster cluster = createDefaultCluster();
        cluster.setAdditionalRngSources(Collections.singleton(VmRngDevice.Source.RANDOM));
        createCommand(cluster);
        cpuExists();
        validateFailedWithReason(EngineMessage.ACTION_TYPE_FAILED_RANDOM_RNG_SOURCE_CANT_BE_ADDED_TO_CLUSTER_ADDITIONAL_RNG_SOURCES);
    }

    @Test
    public void memoryOptimizationWithoutKsmOrBallooning(){
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(150);
        cluster.setEnableKsm(false);
        cluster.setEnableBallooning(false);
        createCommand(cluster);
        cpuExists();
        validateFailedWithReason(EngineMessage.CLUSTER_TO_ALLOW_MEMORY_OPTIMIZATION_YOU_MUST_ALLOW_KSM_OR_BALLOONING);
    }

    @Test
    public void memoryOptimizationLowerThenZeroWithoutKsmOrBallooning(){
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(-52);
        cluster.setEnableKsm(false);
        cluster.setEnableBallooning(false);
        createCommand(cluster);
        cpuExists();
        validateFailedWithReason(EngineMessage.CLUSTER_TO_ALLOW_MEMORY_OPTIMIZATION_YOU_MUST_ALLOW_KSM_OR_BALLOONING);
    }

    @Test
    public void memoryOptimizationWithoutBallooning(){
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(0);
        cluster.setEnableKsm(true);
        cluster.setEnableBallooning(false);
        createCommand(cluster);
        cpuExists();
        initAndAssertValidation(true);
    }

    @Test
    public void memoryOptimizationWithoutKsm(){
        final Cluster cluster = createDefaultCluster();
        cluster.setMaxVdsMemoryOverCommit(200);
        cluster.setEnableKsm(false);
        cluster.setEnableBallooning(true);
        createCommand(cluster);
        cpuExists();
        initAndAssertValidation(true);
    }

    @Test
    public void memoryOptimizationWithKsmAndBallooning(){
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

        doReturn(dbFacadeMock).when(cmd).getDbFacade();
        doReturn(clusterDao).when(dbFacadeMock).getClusterDao();
        doReturn(storagePoolDao).when(dbFacadeMock).getStoragePoolDao();
        doReturn(networkClusterValidator).when(cmd).createManagementNetworkClusterValidator();
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
        doReturn(null).when(cmd).missingServerCpuFlags(any());
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
}
