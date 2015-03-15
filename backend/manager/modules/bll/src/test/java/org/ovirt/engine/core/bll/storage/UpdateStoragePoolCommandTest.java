package org.ovirt.engine.core.bll.storage;

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
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    @Mock
    private ManagementNetworkUtil managementNetworkUtil;

    private final List<Network> allDcNetworks = new ArrayList<>();

    @Before
    public void setUp() {
        when(spDao.get(any(Guid.class))).thenReturn(createDefaultStoragePool());
        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(sdList);
        when(vdsGroupDao.getAllForStoragePool(any(Guid.class))).thenReturn(createClusterList());

        spyCommand(new StoragePoolManagementParameter(createNewStoragePool()));
    }

    protected void spyCommand(StoragePoolManagementParameter params) {
        UpdateStoragePoolCommand<StoragePoolManagementParameter> realCommand =
                new UpdateStoragePoolCommand<>(params);

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
        doReturn(managementNetworkUtil).when(cmd).getManagementNetworkUtil();
        doReturn(poolValidator).when(cmd).createStoragePoolValidator();
        doReturn(true).when(sdList).isEmpty();

        mcr.mockConfigValue(ConfigValues.AutoRegistrationDefaultVdsGroupID, DEFAULT_VDS_GROUP_ID);
        mcr.mockConfigValue(ConfigValues.NonVmNetworkSupported, false);
        mcr.mockConfigValue(ConfigValues.MTUOverrideSupported, false);
        mcr.mockConfigValue(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_0, false);
        mcr.mockConfigValue(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_1, false);
        mcr.mockConfigValue(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_2, false);
        mcr.mockConfigValue(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_3, false);
        mcr.mockConfigValue(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_4, true);
        mcr.mockConfigValue(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_5, true);
        mcr.mockConfigValue(ConfigValues.PosixStorageEnabled, Version.v3_1, false);
        mcr.mockConfigValue(ConfigValues.GlusterFsStorageEnabled, Version.v3_1, false);
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
        addNonManagementNetworkToPool();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void lowerVersionMgmtNetworkAndRegularNetworks() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addNonManagementNetworksToPool(2);
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void lowerVersionHostsAndNetwork() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addHostsToCluster();
        addNonManagementNetworkToPool();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void lowerVersionMgmtNetworkSupportedFeatures() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addManagementNetworksToPool(2);
        setupNetworkValidator(true);
        assertTrue(cmd.canDoAction());
    }

    // TODO:
    @Test
    public void lowerVersionMgmtNetworkNonSupportedFeatures() {
        storagePoolWithLowerVersion();
        addNonDefaultClusterToPool();
        addManagementNetworksToPool(2);
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
        secondCluster.setCompatibilityVersion(VERSION_1_2);
        secondCluster.setName("secondCluster");
        clusterList.add(secondCluster);

        // Create new unsupported cluster.
        VDSGroup thirdCluster = new VDSGroup();
        thirdCluster.setCompatibilityVersion(VERSION_1_1);
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

    @Test
    public void cantDowngradeIfImpliesFormatDowngrading() {
        storagePoolVersion35();

        // Set the current compatibility to be 3.5, and the new to be 3.0. downgrading to 3.0 will cause format downgrading.
        cmd.getStoragePool().setCompatibilityVersion(Version.v3_0);

        // Add domains to the storage domains list. (cancel the mock)
        StorageDomain sd = createStorageDomain(StorageFormatType.V3, StorageType.UNKNOWN);
        setAttachedDomains(sd);

        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_DECREASING_COMPATIBILITY_VERSION_CAUSES_STORAGE_FORMAT_DOWNGRADING);
    }

    @Test
    public void cantDowngradeIfGlusterNotSupported() {
        failOnDowngradingWithStorageType(StorageType.GLUSTERFS);
    }

    @Test
    public void cantDowngradeIfPosixNotSupported() {
        failOnDowngradingWithStorageType(StorageType.POSIXFS);
    }

    private void failOnDowngradingWithStorageType(StorageType storageType) {
        storagePoolVersion35();
        cmd.getStoragePool().setCompatibilityVersion(Version.v3_1);

        // Add domains to the storage domains list. (cancel the mock)
        StorageDomain sd = createStorageDomain(StorageFormatType.V3, storageType);
        setAttachedDomains(sd);

        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAINS_ARE_NOT_SUPPORTED_IN_DOWNGRADED_VERSION);
    }

    @Test
    public void cantDowngradeIfMixedTypesNotSupported() {
        storagePoolVersion35();
        cmd.getStoragePool().setCompatibilityVersion(Version.v3_3);

        // Set mixed storage domains (File, Block).
        StorageDomain sdISCI = createStorageDomain(StorageFormatType.V3, StorageType.ISCSI);
        StorageDomain sdNFS = createStorageDomain(StorageFormatType.V3, StorageType.NFS);
        setAttachedDomains(sdISCI, sdNFS);

        List<StorageType> storageTypes = new ArrayList<>();
        storageTypes.add(sdISCI.getStorageType());
        storageTypes.add(sdNFS.getStorageType());

        doReturn(storageTypes).when(spDao).getStorageTypesInPool(any(Guid.class));
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_MIXED_STORAGE_TYPES_NOT_ALLOWED);
    }

    private StorageDomain createStorageDomain(StorageFormatType formatType, StorageType storageType) {
        StorageDomain sd = new StorageDomain();
        sd.setStorageFormat(formatType);
        sd.setStorageType(storageType);

        return sd;
    }

    private void setAttachedDomains(StorageDomain ... sDomains) {
        List<StorageDomainStatic> sdListWithDomains = new ArrayList<>();
        for (StorageDomain sd:sDomains) {
            sdListWithDomains.add(sd.getStorageStaticData());

            // Set the specific validator for this domain.
            AttachDomainValidatorForTesting attachDomainValidator = spy(new AttachDomainValidatorForTesting(sd.getStorageStaticData(), cmd.getStoragePool()));
            doReturn(attachDomainValidator).when(cmd).getAttachDomainValidator(sd.getStorageStaticData());
            doReturn(spDao).when(attachDomainValidator).getStoragePoolDao();
        }

        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(sdListWithDomains);
    }

    private void storagePoolVersion35() {
        StoragePool pool = createBasicPool();
        pool.setCompatibilityVersion(Version.v3_5);
        when(spDao.get(any(Guid.class))).thenReturn(pool);
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

    private void domainListNotEmpty() {
        when(sdList.isEmpty()).thenReturn(false);
        when(sdList.size()).thenReturn(1);
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
        pool.setCompatibilityVersion(Version.v3_5);
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
        List<VDSGroup> clusters = new ArrayList<>();
        VDSGroup cluster = new VDSGroup();
        cluster.setCompatibilityVersion(VERSION_1_0);
        cluster.setName("firstCluster");
        clusters.add(cluster);
        return clusters;
    }

    private void addDefaultClusterToPool() {
        VDSGroup defaultCluster = new VDSGroup();
        defaultCluster.setCompatibilityVersion(VERSION_1_1);
        defaultCluster.setId(DEFAULT_VDS_GROUP_ID);
        List<VDSGroup> clusters = new ArrayList<>();
        clusters.add(defaultCluster);
        when(vdsGroupDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusters);
    }

    private void addNonDefaultClusterToPool() {
        VDSGroup defaultCluster = new VDSGroup();
        defaultCluster.setCompatibilityVersion(VERSION_1_1);
        defaultCluster.setId(NON_DEFAULT_VDS_GROUP_ID);
        List<VDSGroup> clusters = new ArrayList<>();
        clusters.add(defaultCluster);
        when(vdsGroupDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusters);
    }

    private void addHostsToCluster() {
        VDS host = new VDS();
        List<VDS> hosts = new ArrayList<>();
        hosts.add(host);
        when(vdsDao.getAllForStoragePool(any(Guid.class))).thenReturn(hosts);
    }

    private void addManagementNetworksToPool(int numberOfNetworks) {
        for (int i=0; i<numberOfNetworks; i++) {
            final Guid managementNetworkId = Guid.newGuid();
            allDcNetworks.add(createNetwork(managementNetworkId));
            when(managementNetworkUtil.isManagementNetwork(managementNetworkId)).thenReturn(true);
        }
        when(networkDao.getAllForDataCenter(any(Guid.class))).thenReturn(allDcNetworks);
    }

    private Network createNetwork(Guid networkId) {
        Network network = new Network();
        network.setId(networkId);
        return network;
    }

    private void setupNetworkValidator(boolean valid) {
        NetworkValidator validator = Mockito.mock(NetworkValidator.class);
        when(validator.canNetworkCompatabilityBeDecreased()).thenReturn(valid);
        when(cmd.getNetworkValidator(any(Network.class))).thenReturn(validator);
    }

    private void addNonManagementNetworkToPool() {
        addNonManagementNetworksToPool(1);
    }

    private void addNonManagementNetworksToPool(int numberOfNetwworks) {
        for (int i = 0; i< numberOfNetwworks; i++) {
            final Guid networkId = Guid.newGuid();
            Network network = createNetwork(networkId);
            network.setId(networkId);
            allDcNetworks.add(network);
            when(managementNetworkUtil.isManagementNetwork(networkId)).thenReturn(false);
        }
        when(networkDao.getAllForDataCenter(any(Guid.class))).thenReturn(allDcNetworks);
    }

    private void canDoActionFailed(final VdcBllMessages reason) {
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(reason.toString()));
    }

    protected class StoragePoolValidatorForTesting extends StoragePoolValidator {
        public StoragePoolValidatorForTesting(StoragePool storagePool) {
            super(storagePool);
        }

        // This function overrides a protected function in StoragePoolValidator (which is not accessible in this package) for mocking ability.
        public VdsGroupDAO getVdsGroupDao() {
            return super.getVdsGroupDao();
        }
    }

    protected class AttachDomainValidatorForTesting extends StorageDomainToPoolRelationValidator {
        public AttachDomainValidatorForTesting(StorageDomainStatic domainStatic, StoragePool pool) {
            super(domainStatic, pool);
        }

        // This function overrides a protected function in StorageDomainToPoolRelationValidator (which is not accessible in this package) for mocking ability.
        public StoragePoolDAO getStoragePoolDao() {
            return super.getStoragePoolDao();
        }
    }
}
