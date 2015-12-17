package org.ovirt.engine.core.bll;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
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
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.MockConfigRule;
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
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

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

    private static final Map<String, String> migrationMap = Collections.unmodifiableMap(
            new HashMap<String, String>() {{
                put("undefined", "true");
                put("x86_64", "true");
                put("ppc64", "false");
            }});

    private static final Set<Version> versions = Collections.unmodifiableSet(
            new HashSet<Version>() {{
                add(VERSION_1_0);
                add(VERSION_1_1);
                add(VERSION_1_2);
            }});

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.IsMigrationSupported, VERSION_1_0.getValue(), migrationMap),
            mockConfig(ConfigValues.IsMigrationSupported, VERSION_1_1.getValue(), migrationMap),
            mockConfig(ConfigValues.IsMigrationSupported, VERSION_1_2.getValue(), migrationMap)
    );

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    DbFacade dbFacadeMock;

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

    private UpdateClusterCommand<ManagementNetworkOnClusterOperationParameters> cmd;

    @Test
    public void nameInUse() {
        createSimpleCommand();
        createCommandWithDifferentName();
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_DO_ACTION_NAME_IN_USE);
    }

    @Test
    public void invalidCluster() {
        createSimpleCommand();
        when(clusterDao.get(any(Guid.class))).thenReturn(null);
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
        doReturn(false).when(cmd).isCpuUpdatable(any(Cluster.class));
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
        createCommandWithOlderVersion(true, false);
        setupCpu();
        vdsExist();
        validateFailedWithReason(EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void versionDecreaseNoHostsOrNetwork() {
        createCommandWithOlderVersion(true, false);
        setupCpu();
        StoragePoolDao storagePoolDao2 = Mockito.mock(StoragePoolDao.class);
        when(storagePoolDao2.get(any(Guid.class))).thenReturn(createStoragePoolLocalFS());
        doReturn(storagePoolDao2).when(cmd).getStoragePoolDao();
        initAndAssertValidation(true);
    }

    @Test
    public void versionDecreaseLowerVersionThanDC() {
        createCommandWithOlderVersion(true, false);
        StoragePoolDao storagePoolDao2 = Mockito.mock(StoragePoolDao.class);
        when(storagePoolDao2.get(any(Guid.class))).thenReturn(createStoragePoolLocalFSOldVersion());
        doReturn(storagePoolDao2).when(cmd).getStoragePoolDao();
        doReturn(storagePoolDao2).when(dbFacadeMock).getStoragePoolDao();
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
        prepareManagementNetworkMocks();

        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        setupCpu();
        allQueriesForVms();
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

        prepareManagementNetworkMocks();

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
        prepareManagementNetworkMocks();

        mcr.mockConfigValue(ConfigValues.AutoRegistrationDefaultClusterID, DEFAULT_CLUSTER_ID);
        createCommandWithDefaultCluster();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        setupCpu();
        allQueriesForVms();
        architectureIsUpdatable();
        validateFailedWithReason(EngineMessage.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
    }

    private void prepareManagementNetworkMocks() {
        newDefaultManagementNetworkFound();
        when(networkClusterValidator.managementNetworkChange()).thenReturn(ValidationResult.VALID);
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
        when(clusterDao.get(any(Guid.class))).thenReturn(createClusterWithNoCpuName());
        when(clusterDao.getByName(anyString())).thenReturn(createClusterWithNoCpuName());
        when(glusterVolumeDao.getByClusterId(any(Guid.class))).thenReturn(new ArrayList<>());
        allQueriesForVms();
        initAndAssertValidation(true);
    }

    @Test
    public void clusterWithNoServiceEnabled() {
        createCommandWithNoService();
        when(clusterDao.get(any(Guid.class))).thenReturn(createClusterWithNoCpuName());
        when(clusterDao.getByName(anyString())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        allQueriesForVms();
        validateFailedWithReason(EngineMessage.CLUSTER_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED);
    }

    @Test
    public void clusterWithVirtGlusterServicesNotAllowed() {
        createCommandWithVirtGlusterEnabled();
        when(clusterDao.get(any(Guid.class))).thenReturn(createClusterWithNoCpuName());
        when(clusterDao.getByName(anyString())).thenReturn(createClusterWithNoCpuName());
        mcr.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, Boolean.FALSE);
        cpuExists();
        allQueriesForVms();
        validateFailedWithReason(EngineMessage.CLUSTER_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED);
    }

    @Test
    public void disableVirtWhenVmsExist() {
        createCommandWithGlusterEnabled();
        when(clusterDao.get(any(Guid.class))).thenReturn(createDefaultCluster());
        when(clusterDao.getByName(anyString())).thenReturn(createDefaultCluster());
        cpuExists();
        cpuFlagsNotMissing();
        clusterHasVds();
        clusterHasVMs();

        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_DISABLE_VIRT_WHEN_CLUSTER_CONTAINS_VMS);
    }

    @Test
    public void disableGlusterWhenVolumesExist() {
        createCommandWithVirtEnabled();
        when(clusterDao.get(any(Guid.class))).thenReturn(createClusterWithNoCpuName());
        when(clusterDao.getByName(anyString())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        cpuFlagsNotMissing();
        allQueriesForVms();
        clusterHasGlusterVolumes();

        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_DISABLE_GLUSTER_WHEN_CLUSTER_CONTAINS_VOLUMES);
    }

    @Test
    public void enableNewAddtionalFeatureWhenHostDoesnotSupport() {
        createCommandWithAddtionalFeature();
        when(clusterDao.get(any(Guid.class))).thenReturn(createClusterWithNoCpuName());
        when(clusterDao.getByName(anyString())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        cpuFlagsNotMissing();
        allQueriesForVms();
        clusterHasVds();
        when(clusterFeatureDao.getSupportedFeaturesByClusterId(any(Guid.class))).thenReturn(Collections.emptySet());
        when(hostFeatureDao.getSupportedHostFeaturesByHostId(any(Guid.class))).thenReturn(Collections.emptySet());
        validateFailedWithReason(EngineMessage.CLUSTER_CANNOT_UPDATE_SUPPORTED_FEATURES_WITH_LOWER_HOSTS);
    }

    @Test
    public void enableNewAddtionalFeatureWhenHostSupports() {
        createCommandWithAddtionalFeature();
        when(clusterDao.get(any(Guid.class))).thenReturn(createClusterWithNoCpuName());
        when(clusterDao.getByName(anyString())).thenReturn(createClusterWithNoCpuName());
        cpuExists();
        cpuFlagsNotMissing();
        allQueriesForVms();
        clusterHasVds();
        when(clusterFeatureDao.getSupportedFeaturesByClusterId(any(Guid.class))).thenReturn(Collections.emptySet());
        when(hostFeatureDao.getSupportedHostFeaturesByHostId(any(Guid.class))).thenReturn(Collections.singleton("TEST_FEATURE"));
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

    private void createSimpleCommand() {
        createCommand(createNewCluster());
    }

    private void createCommandWithOlderVersion(boolean supportsVirtService, boolean supportsGlusterService) {
        createCommand(createClusterWithOlderVersion(true, false));

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
        setValidCpuVersionMap();
        final ManagementNetworkOnClusterOperationParameters param;
        if (managementNetworkId == null) {
            param = new ManagementNetworkOnClusterOperationParameters(group);
        } else {
            param = new ManagementNetworkOnClusterOperationParameters(group, managementNetworkId);
        }
        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);
        cmd = spy(new UpdateClusterCommand<ManagementNetworkOnClusterOperationParameters>(param, null) {
            @Override
            protected void initUser() {
                // Stub for testing
            }
        });

        doReturn(0).when(cmd).compareCpuLevels(any(Cluster.class));

        doReturn(cpuFlagsManagerHandler).when(cmd).getCpuFlagsManagerHandler();
        doReturn(dbFacadeMock).when(cmd).getDbFacade();
        doReturn(clusterDao).when(cmd).getClusterDao();
        doReturn(clusterDao).when(dbFacadeMock).getClusterDao();
        doReturn(vdsDao).when(cmd).getVdsDao();
        doReturn(storagePoolDao).when(cmd).getStoragePoolDao();
        doReturn(storagePoolDao).when(dbFacadeMock).getStoragePoolDao();
        doReturn(glusterVolumeDao).when(cmd).getGlusterVolumeDao();
        doReturn(vmDao).when(cmd).getVmDao();
        doReturn(networkDao).when(cmd).getNetworkDao();
        doReturn(defaultManagementNetworkFinder).when(cmd).getDefaultManagementNetworkFinder();
        doReturn(clusterFeatureDao).when(cmd).getClusterFeatureDao();
        doReturn(hostFeatureDao).when(cmd).getHostFeatureDao();
        doReturn(networkClusterValidator).when(cmd).createManagementNetworkClusterValidator();
        doReturn(true).when(cmd).isSupportedEmulatedMachinesMatchClusterLevel(any(VDS.class));

        // cluster upgrade
        doReturn(schedulingManager).when(cmd).getSchedulingManager();
        doReturn(vmNumaNodeDao).when(cmd).getVmNumaNodeDao();
        doReturn(inClusterUpgradeValidator).when(cmd).getUpgradeValidator();
        doReturn(new ClusterPolicy()).when(schedulingManager).getClusterPolicy(any(Guid.class));
        final ClusterPolicy clusterPolicy = new ClusterPolicy();
        clusterPolicy.setId(ClusterPolicy.UPGRADE_POLICY_GUID);
        doReturn(clusterPolicy).when(schedulingManager).getClusterPolicy(eq(ClusterPolicy.UPGRADE_POLICY_GUID));
        doReturn(ValidationResult.VALID).when(inClusterUpgradeValidator).isUpgradeDone(anyList());
        doReturn(ValidationResult.VALID).when(inClusterUpgradeValidator).isUpgradePossible(anyList(), anyList());
        doReturn(new HashMap<Guid, List<VmNumaNode>>()).when(vmNumaNodeDao)
                .getVmNumaNodeInfoByClusterIdAsMap(any(Guid.class));

        if (StringUtils.isEmpty(group.getCpuName())) {
            doReturn(ArchitectureType.undefined).when(cmd).getArchitecture();
        } else {
            doReturn(ArchitectureType.x86_64).when(cmd).getArchitecture();
        }

        when(clusterDao.get(any(Guid.class))).thenReturn(createDefaultCluster());
        when(clusterDao.getByName(anyString())).thenReturn(createDefaultCluster());
        List<Cluster> clusterList = new ArrayList<>();
        clusterList.add(createDefaultCluster());
        when(clusterDao.getByName(anyString(), anyBoolean())).thenReturn(clusterList);
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
        return group;
    }

    private static Cluster createDetachedDefaultCluster() {
        Cluster group = createDefaultCluster();
        group.setStoragePoolId(null);
        return group;
    }

    private static Cluster createClusterWithOlderVersion(boolean supportsVirtService, boolean supportsGlusterService) {
        Cluster group = createNewCluster();
        group.setCompatibilityVersion(VERSION_1_0);
        group.setStoragePoolId(DC_ID1);
        group.setVirtService(supportsVirtService);
        group.setGlusterService(supportsGlusterService);
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
        when(clusterDao.getAllForStoragePool(any(Guid.class))).thenReturn(groupList);
    }

    private void vdsExist() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        List<VDS> vdsList = new ArrayList<>();
        vdsList.add(vds);
        when(vdsDao.getAllForCluster(any(Guid.class))).thenReturn(vdsList);
    }

    private void vdsExistWithHigherVersion() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        vds.setClusterCompatibilityVersion(VERSION_1_2);
        List<VDS> vdsList = new ArrayList<>();
        vdsList.add(vds);
        when(vdsDao.getAllForCluster(any(Guid.class))).thenReturn(vdsList);
    }

    private void allQueriesForVms() {
        when(vmDao.getAllForCluster(any(Guid.class))).thenReturn(Collections.<VM>emptyList());
    }

    private void clusterHasVds() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        vds.setSupportedClusterLevels(VERSION_1_1.toString());
        List<VDS> vdsList = new ArrayList<>();
        vdsList.add(vds);
        when(vdsDao.getAllForCluster(any(Guid.class))).thenReturn(vdsList);
    }

    private void clusterHasGlusterVolumes() {
        List<GlusterVolumeEntity> volumes = new ArrayList<>();
        volumes.add(new GlusterVolumeEntity());
        when(glusterVolumeDao.getByClusterId(any(Guid.class))).thenReturn(volumes);
    }

    private void clusterHasVMs() {
        VM vm = new VM();
        vm.setClusterId(DEFAULT_CLUSTER_ID);
        List<VM> vmList = new ArrayList<>();
        vmList.add(vm);

        when(vmDao.getAllForCluster(any(Guid.class))).thenReturn(vmList);
    }

    private void cpuFlagsMissing() {
        List<String> strings = new ArrayList<>();
        strings.add("foo");
        doReturn(strings).when(cmd).missingServerCpuFlags(any(VDS.class));
    }

    private void cpuFlagsNotMissing() {
        doReturn(null).when(cmd).missingServerCpuFlags(any(VDS.class));
    }

    private void cpuManufacturersDontMatch() {
        doReturn(false).when(cmd).checkIfCpusSameManufacture(any(Cluster.class));
    }

    private void cpuManufacturersMatch() {
        doReturn(true).when(cmd).checkIfCpusSameManufacture(any(Cluster.class));
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

    private void setValidCpuVersionMap() {
        mcr.mockConfigValue(ConfigValues.SupportedClusterLevels, versions);
    }

    private void validateFailedWithReason(final EngineMessage message) {
        initAndAssertValidation(false);
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(message.toString()));
    }
}
