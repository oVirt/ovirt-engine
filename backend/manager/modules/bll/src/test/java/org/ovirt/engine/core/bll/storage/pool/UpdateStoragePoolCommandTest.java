package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class UpdateStoragePoolCommandTest extends BaseCommandTest {

    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
    private static final Version VERSION_2_0 = new Version(2, 0);
    private static final Guid DEFAULT_CLUSTER_ID = Guid.newGuid();
    private static final Guid NON_DEFAULT_CLUSTER_ID = Guid.newGuid();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.AutoRegistrationDefaultClusterID, DEFAULT_CLUSTER_ID),
            mockConfig(ConfigValues.StoragePoolNameSizeLimit, 10)
    );


    private UpdateStoragePoolCommand<StoragePoolManagementParameter> cmd;

    @Mock
    private StoragePoolDao spDao;
    @Mock
    private StorageDomainStaticDao sdDao;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private NetworkDao networkDao;
    @Mock
    private ManagementNetworkUtil managementNetworkUtil;
    private StoragePoolValidator poolValidator;

    @Before
    public void setUp() {
        when(spDao.get(any(Guid.class))).thenReturn(createDefaultStoragePool());
        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.<StorageDomainStatic>emptyList());
        when(clusterDao.getAllForStoragePool(any(Guid.class))).thenReturn(createClusterList());

        spyCommand(new StoragePoolManagementParameter(createNewStoragePool()));
    }

    protected void spyCommand(StoragePoolManagementParameter params) {
        UpdateStoragePoolCommand<StoragePoolManagementParameter> realCommand =
                new UpdateStoragePoolCommand<>(params, null);

        cmd = spy(realCommand);
        doReturn(createVersionSet().contains(cmd.getStoragePool().getCompatibilityVersion())).when(cmd)
                .isStoragePoolVersionSupported();
        doReturn(spDao).when(cmd).getStoragePoolDao();
        doReturn(sdDao).when(cmd).getStorageDomainStaticDao();
        doReturn(clusterDao).when(cmd).getClusterDao();
        doReturn(vdsDao).when(cmd).getVdsDao();
        doReturn(networkDao).when(cmd).getNetworkDao();
        doReturn(managementNetworkUtil).when(cmd).getManagementNetworkUtil();

        // Spy the StoragePoolValidator:
        poolValidator = spy(new StoragePoolValidator(params.getStoragePool()));
        doReturn(ValidationResult.VALID).when(poolValidator).isNotLocalfsWithDefaultCluster();
        doReturn(poolValidator).when(cmd).createStoragePoolValidator();
    }

    @Test
    public void happyPath() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void nameExists() {
        newPoolNameIsAlreadyTaken();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
    }

    @Test
    public void hasDomains() {
        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn
                (Collections.singletonList(new StorageDomainStatic()));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ERROR_CANNOT_CHANGE_STORAGE_POOL_TYPE_WITH_DOMAINS);
    }

    @Test
    public void unsupportedVersion() {
        storagePoolWithInvalidVersion();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, VersionSupport.getUnsupportedVersionMessage());
    }

    @Test
    public void lowerVersionNoHostsNoNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void lowerVersionHostsNoNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addHostsToCluster();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void lowerVersionNoHostsWithNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addNonManagementNetworkToPool();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void lowerVersionMgmtNetworkAndRegularNetworks() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addManagementNetworkToPool();
        addNonManagementNetworksToPool(2);
        setupNetworkValidator(true);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void lowerVersionHostsAndNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addHostsToCluster();
        addNonManagementNetworkToPool();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void lowerVersionMgmtNetworkSupportedFeatures() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addManagementNetworksToPool(2);
        setupNetworkValidator(true);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void lowerVersionMgmtNetworkNonSupportedFeatures() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addManagementNetworksToPool(2);
        setupNetworkValidator(false);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void versionHigherThanCluster() {
        storagePoolWithVersionHigherThanCluster();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS);
    }

    @Test
    public void testValidateAllClustersLevel() {
        storagePoolWithVersionHigherThanCluster();
        List<Cluster> clusterList = createClusterList();
        // Create new supported cluster.
        Cluster secondCluster = new Cluster();
        secondCluster.setCompatibilityVersion(VERSION_1_2);
        secondCluster.setName("secondCluster");
        clusterList.add(secondCluster);

        // Create new unsupported cluster.
        Cluster thirdCluster = new Cluster();
        thirdCluster.setCompatibilityVersion(VERSION_1_1);
        thirdCluster.setName("thirdCluster");
        clusterList.add(thirdCluster);

        // Test upgrade
        when(clusterDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusterList);
        assertFalse(cmd.checkAllClustersLevel());
        List<String> messages = cmd.getReturnValue().getValidationMessages();
        assertTrue(messages.contains(EngineMessage.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS.toString()));
        assertTrue(messages.get(0).contains("firstCluster"));
        assertFalse(messages.get(0).contains("secondCluster"));
        assertTrue(messages.get(0).contains("thirdCluster"));
    }

    @Test
    public void poolHasDefaultCluster() {
        mcr.mockConfigValue(ConfigValues.AutoRegistrationDefaultClusterID, DEFAULT_CLUSTER_ID);
        addDefaultClusterToPool();
        storagePoolWithLocalFS();
        doReturn(new ValidationResult
                (EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_CLUSTER_CANNOT_BE_LOCALFS))
                .when(poolValidator).isNotLocalfsWithDefaultCluster();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_CLUSTER_CANNOT_BE_LOCALFS);
    }

    private void newPoolNameIsAlreadyTaken() {
        when(spDao.get(any(Guid.class))).thenReturn(new StoragePool());
        List<StoragePool> storagePoolList = new ArrayList<>();
        storagePoolList.add(createDefaultStoragePool());
        when(spDao.getByName(anyString(), anyBoolean())).thenReturn(new ArrayList<>(storagePoolList));
    }

    private void storagePoolWithVersionHigherThanCluster() {
        spyCommand(new StoragePoolManagementParameter(createHigherVersionStoragePool()));
    }

    private void storagePoolWithLowerVersion() {
        spyCommand(new StoragePoolManagementParameter(createLowerVersionStoragePool()));
    }

    private void storagePoolWithInvalidVersion() {
        spyCommand(new StoragePoolManagementParameter(createInvalidVersionStoragePool()));
    }

    private void storagePoolWithLocalFS() {
        spyCommand(new StoragePoolManagementParameter(createDefaultStoragePool()));
    }

    private static Set<Version> createVersionSet() {
        Set<Version> versions = new HashSet<>();
        versions.add(VERSION_1_0);
        versions.add(VERSION_1_1);
        versions.add(VERSION_1_2);
        return versions;
    }

    private static StoragePool createNewStoragePool() {
        StoragePool pool = createBasicPool();
        pool.setIsLocal(false);
        pool.setCompatibilityVersion(VERSION_1_1);
        return pool;
    }

    private static StoragePool createDefaultStoragePool() {
        StoragePool pool = createBasicPool();
        pool.setIsLocal(true);
        pool.setCompatibilityVersion(VERSION_1_1);
        return pool;
    }

    private static StoragePool createLowerVersionStoragePool() {
        StoragePool pool = createBasicPool();
        pool.setIsLocal(true);
        pool.setCompatibilityVersion(VERSION_1_0);
        return pool;
    }

    private static StoragePool createBasicPool() {
        StoragePool pool = new StoragePool();
        pool.setId(Guid.newGuid());
        pool.setName("Default");
        return pool;
    }

    private static StoragePool createHigherVersionStoragePool() {
        StoragePool pool = createBasicPool();
        pool.setIsLocal(true);
        pool.setCompatibilityVersion(VERSION_1_2);
        return pool;
    }

    private static StoragePool createInvalidVersionStoragePool() {
        StoragePool pool = createBasicPool();
        pool.setIsLocal(true);
        pool.setCompatibilityVersion(VERSION_2_0);
        return pool;
    }

    private static List<Cluster> createClusterList() {
        List<Cluster> clusters = new ArrayList<>();
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(VERSION_1_0);
        cluster.setName("firstCluster");
        clusters.add(cluster);
        return clusters;
    }

    private void addDefaultClusterToPool() {
        Cluster defaultCluster = new Cluster();
        defaultCluster.setCompatibilityVersion(VERSION_1_1);
        defaultCluster.setId(DEFAULT_CLUSTER_ID);
        List<Cluster> clusters = new ArrayList<>();
        clusters.add(defaultCluster);
        when(clusterDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusters);
    }

    private void addNonDefaultClusterToPool() {
        Cluster defaultCluster = new Cluster();
        defaultCluster.setCompatibilityVersion(VERSION_1_1);
        defaultCluster.setId(NON_DEFAULT_CLUSTER_ID);
        List<Cluster> clusters = new ArrayList<>();
        clusters.add(defaultCluster);
        when(clusterDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusters);
    }

    private void addHostsToCluster() {
        VDS host = new VDS();
        List<VDS> hosts = new ArrayList<>();
        hosts.add(host);
        when(vdsDao.getAllForStoragePool(any(Guid.class))).thenReturn(hosts);
    }

    private void addManagementNetworkToPool() {
        addManagementNetworksToPool(1);
    }

    private void addManagementNetworksToPool(int numberOfNetworks) {
        addNetworksToPool(numberOfNetworks, true);
    }

    private Network createNetwork(Guid networkId) {
        Network network = new Network();
        network.setId(networkId);
        return network;
    }

    private void setupNetworkValidator(boolean valid) {
        NetworkValidator validator = mock(NetworkValidator.class);
        when(validator.canNetworkCompatabilityBeDecreased()).thenReturn(valid);
        when(cmd.getNetworkValidator(any(Network.class))).thenReturn(validator);
    }

    private void addNonManagementNetworkToPool() {
        addNonManagementNetworksToPool(1);
    }

    private void addNonManagementNetworksToPool(int numberOfNetworks) {
        addNetworksToPool(numberOfNetworks, false);
    }

    private void addNetworksToPool(int numberOfNetworks, boolean isManagement) {
        List<Network> allDcNetworks = new ArrayList<>();
        for (int i = 0; i < numberOfNetworks; i++) {
            final Guid networkId = Guid.newGuid();
            Network network = createNetwork(networkId);
            network.setId(networkId);
            allDcNetworks.add(network);
            when(managementNetworkUtil.isManagementNetwork(networkId)).thenReturn(isManagement);
        }
        when(networkDao.getAllForDataCenter(any(Guid.class))).thenReturn(allDcNetworks);
    }
}
