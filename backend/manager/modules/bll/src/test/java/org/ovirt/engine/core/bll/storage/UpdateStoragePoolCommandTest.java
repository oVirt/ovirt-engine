package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStoragePoolCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
    private static final Version VERSION_2_0 = new Version(2, 0);
    private static final Guid DEFAULT_VDS_GROUP_ID = new Guid("99408929-82CF-4DC7-A532-9D998063FA95");
    private static final Guid NON_DEFAULT_VDS_GROUP_ID = new Guid("99408929-82CF-4DC7-A532-9D998063FA96");

    private UpdateStoragePoolCommand<StoragePoolManagementParameter> cmd;

    @Mock
    private StoragePoolDAO spDao;
    @Mock
    private StorageDomainStaticDAO sdDao;
    @Mock
    private List<StorageDomainStatic> sdList;
    @Mock
    private VdsGroupDAO vdsGroupDao;
    @Mock
    private VdsDAO vdsDao;
    @Mock
    private NetworkDao networkDao;

    @Before
    public void setUp() {
        when(spDao.get(any(Guid.class))).thenReturn(createDefaultStoragePool());
        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(sdList);
        when(vdsGroupDao.getAllForStoragePool(any(Guid.class))).thenReturn(createClusterList());

        spyCommand(new StoragePoolManagementParameter(createNewStoragePool()));
    }

    protected void spyCommand(StoragePoolManagementParameter params) {
        UpdateStoragePoolCommand<StoragePoolManagementParameter> realCommand =
                new UpdateStoragePoolCommand<StoragePoolManagementParameter>(params);

        StoragePoolValidatorForTesting poolValidator = spy(new StoragePoolValidatorForTesting(params.getStoragePool()));
        doReturn(vdsGroupDao).when(poolValidator).getVdsGroupDao();

        cmd = spy(realCommand);
        doReturn(10).when(cmd).getStoragePoolNameSizeLimit();
        doReturn(createVersionSet().contains(cmd.getStoragePool().getCompatibilityVersion())).when(cmd)
                .isStoragePoolVersionSupported();
        doReturn(spDao).when(cmd).getStoragePoolDAO();
        doReturn(sdDao).when(cmd).getStorageDomainStaticDAO();
        doReturn(vdsGroupDao).when(cmd).getVdsGroupDAO();
        doReturn(vdsDao).when(cmd).getVdsDAO();
        doReturn(networkDao).when(cmd).getNetworkDAO();
        doReturn(poolValidator).when(cmd).createStoragePoolValidator();
        doReturn(true).when(sdList).isEmpty();

        mcr.mockConfigValue(ConfigValues.AutoRegistrationDefaultVdsGroupID, DEFAULT_VDS_GROUP_ID);
        mcr.mockConfigValue(ConfigValues.ManagementNetwork, "test_mgmt");
        mcr.mockConfigValue(ConfigValues.NonVmNetworkSupported, false);
        mcr.mockConfigValue(ConfigValues.MTUOverrideSupported, false);
    }

    @Test
    public void happyPath() {
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void nameExists() {
        newPoolNameIsAlreadyTaken();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
    }

    @Test
    public void hasDomains() {
        domainListNotEmpty();
        canDoActionFailed(VdcBllMessages.ERROR_CANNOT_CHANGE_STORAGE_POOL_TYPE_WITH_DOMAINS);
    }

    @Test
    public void unsupportedVersion() {
        storagePoolWithInvalidVersion();
        canDoActionFailed(VersionSupport.getUnsupportedVersionMessage());
    }

    @Test
    public void lowerVersionNoHostsNoNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void lowerVersionHostsNoNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addHostsToCluster();
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void lowerVersionNoHostsWithNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addNetworkToPool();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void lowerVersionHostsAndNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addHostsToCluster();
        addNetworkToPool();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void lowerVersionMgmtNetworkSupportedFeatures() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addManagementNetworkToPool();
        setupNetworkValidator(true);
        assertTrue(cmd.canDoAction());
    }

    // TODO:
    @Test
    public void lowerVersionMgmtNetworkNonSupportedFeatures() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addManagementNetworkToPool();
        setupNetworkValidator(false);
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void versionHigherThanCluster() {
        storagePoolWithVersionHigherThanCluster();
        canDoActionFailed(VdcBllMessages.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS);
    }

    @Test
    public void testValidateAllClustersLevel() {
        storagePoolWithVersionHigherThanCluster();
        List<VDSGroup> clusterList = createClusterList();
        // Create new supported cluster.
        VDSGroup secondCluster = new VDSGroup();
        secondCluster.setcompatibility_version(VERSION_1_2);
        secondCluster.setName("secondCluster");
        clusterList.add(secondCluster);

        // Create new unsupported cluster.
        VDSGroup thirdCluster = new VDSGroup();
        thirdCluster.setcompatibility_version(VERSION_1_1);
        thirdCluster.setName("thirdCluster");
        clusterList.add(thirdCluster);

        // Test upgrade
        when(vdsGroupDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusterList);
        assertFalse(cmd.checkAllClustersLevel());
        List<String> messages = cmd.getReturnValue().getCanDoActionMessages();
        assertTrue(messages.contains(VdcBllMessages.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS.toString()));
        assertTrue(messages.get(0).contains("firstCluster"));
        assertFalse(messages.get(0).contains("secondCluster"));
        assertTrue(messages.get(0).contains("thirdCluster"));
    }

    @Test
    public void poolHasDefaultCluster() {
        mcr.mockConfigValue(ConfigValues.AutoRegistrationDefaultVdsGroupID, DEFAULT_VDS_GROUP_ID);
        addDefaultClusterToPool();
        storagePoolWithLocalFS();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS);
    }

    private void newPoolNameIsAlreadyTaken() {
        when(spDao.get(any(Guid.class))).thenReturn(new StoragePool());
        List<StoragePool> storagePoolList = new ArrayList<StoragePool>();
        storagePoolList.add(createDefaultStoragePool());
        when(spDao.getByName(anyString(), anyBoolean())).thenReturn(new ArrayList<StoragePool>(storagePoolList));
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

    private void domainListNotEmpty() {
        when(sdList.isEmpty()).thenReturn(false);
        when(sdList.size()).thenReturn(1);
    }

    private static Set<Version> createVersionSet() {
        Set<Version> versions = new HashSet<Version>();
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

    private static List<VDSGroup> createClusterList() {
        List<VDSGroup> clusters = new ArrayList<VDSGroup>();
        VDSGroup cluster = new VDSGroup();
        cluster.setcompatibility_version(VERSION_1_0);
        cluster.setName("firstCluster");
        clusters.add(cluster);
        return clusters;
    }

    private void addDefaultClusterToPool() {
        VDSGroup defaultCluster = new VDSGroup();
        defaultCluster.setcompatibility_version(VERSION_1_1);
        defaultCluster.setId(DEFAULT_VDS_GROUP_ID);
        List<VDSGroup> clusters = new ArrayList<VDSGroup>();
        clusters.add(defaultCluster);
        when(vdsGroupDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusters);
    }

    private void addNonDefaultClusterToPool() {
        VDSGroup defaultCluster = new VDSGroup();
        defaultCluster.setcompatibility_version(VERSION_1_1);
        defaultCluster.setId(NON_DEFAULT_VDS_GROUP_ID);
        List<VDSGroup> clusters = new ArrayList<VDSGroup>();
        clusters.add(defaultCluster);
        when(vdsGroupDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusters);
    }

    private void addHostsToCluster() {
        VDS host = new VDS();
        List<VDS> hosts = new ArrayList<>();
        hosts.add(host);
        when(vdsDao.getAllForStoragePool(any(Guid.class))).thenReturn(hosts);
    }

    private void addManagementNetworkToPool() {
        Network network = new Network();
        network.setName(Config.<String> getValue(ConfigValues.ManagementNetwork));
        List<Network> networks = new ArrayList<>();
        networks.add(network);
        when(networkDao.getAllForDataCenter(any(Guid.class))).thenReturn(networks);
    }

    private void setupNetworkValidator(boolean valid) {
        NetworkValidator validator = Mockito.mock(NetworkValidator.class);
        when(validator.canNetworkCompatabilityBeDecreased()).thenReturn(valid);
        when(cmd.getNetworkValidator(any(Network.class))).thenReturn(validator);
    }

    private void addNetworkToPool() {
        Network network = new Network();
        network.setName(Config.<String> getValue(ConfigValues.ManagementNetwork) + "2");
        List<Network> networks = new ArrayList<>();
        networks.add(network);
        when(networkDao.getAllForDataCenter(any(Guid.class))).thenReturn(networks);
    }

    private void canDoActionFailed(final VdcBllMessages reason) {
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(reason.toString()));
    }

    protected class StoragePoolValidatorForTesting extends StoragePoolValidator {
        public StoragePoolValidatorForTesting(StoragePool storagePool) {
            super(storagePool);
        }

        public VdsGroupDAO getVdsGroupDao() {
            return DbFacade.getInstance().getVdsGroupDao();
        }
    }
}
