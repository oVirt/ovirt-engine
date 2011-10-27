package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({DbFacade.class, Config.class})
@RunWith(PowerMockRunner.class)
public class UpdateStoragePoolCommandTest {

    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
    private static final Version VERSION_2_0 = new Version(2, 0);


    private UpdateStoragePoolCommand cmd;

    @Mock private DbFacade facade;
    @Mock private StoragePoolDAO spDao;
    @Mock private StorageDomainStaticDAO sdDao;
    @Mock private List<storage_domain_static> sdList;
    @Mock private VdsGroupDAO vdsDao;

    @Before
    public void setUp() {
        initMocks(this);

        mockStatic(DbFacade.class);
        mockStatic(Config.class);

        when(DbFacade.getInstance()).thenReturn(facade);

        when(facade.getStoragePoolDAO()).thenReturn(spDao);
        when(facade.getStorageDomainStaticDAO()).thenReturn(sdDao);
        when(facade.getVdsGroupDAO()).thenReturn(vdsDao);

        when(Config.GetValue(ConfigValues.StoragePoolNameSizeLimit)).thenReturn(10);
        when(Config.GetValue(ConfigValues.SupportedClusterLevels)).thenReturn(createVersionSet());

        when(spDao.get(any(Guid.class))).thenReturn(createDefaultStoragePool());

        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(sdList);

        when(vdsDao.getAllForStoragePool(any(Guid.class))).thenReturn(createClusterList());

        cmd = new UpdateStoragePoolCommand<StoragePoolManagementParameter>(
                new StoragePoolManagementParameter(createNewStoragePool()));
    }

    @Test
    public void happyPath() {
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void nameExists() {
        newPoolNameIsAlreadyTaken();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST.toString());
    }

    @Test
    public void hasDomains() {
        domainListNotEmpty();
        canDoActionFailed(VdcBllMessages.ERROR_CANNOT_CHANGE_STORAGE_POOL_TYPE_WITH_DOMAINS.toString());
    }

    @Test
    public void unsupportedVersion() {
        storagePoolWithInvalidVersion();
        canDoActionFailed(VersionSupport.getUnsupportedVersionMessage());
    }

    @Test
    public void lowerVersion() {
        storagePoolWithLowerVersion();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION.toString());
    }

    @Test
    public void versionHigherThanCluster() {
        storagePoolWithVersionHigherThanCluster();
        canDoActionFailed(
                VdcBllMessages.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS.toString());
    }

    @Test
    public void poolHasDefaultCluster() {
        addDefaultClusterToPool();
        storagePoolWithLocalFS();
        canDoActionFailed(
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS.toString());
    }

    private void newPoolNameIsAlreadyTaken() {
        when(spDao.get(any(Guid.class))).thenReturn(new storage_pool("foo", Guid.Empty, "foo",
                StorageType.NFS.getValue(), StoragePoolStatus.Up.getValue()));
        when(spDao.getByName(anyString())).thenReturn(createDefaultStoragePool());
    }

    private void storagePoolWithVersionHigherThanCluster() {
        cmd = new UpdateStoragePoolCommand<StoragePoolManagementParameter>(
                new StoragePoolManagementParameter(createHigherVersionStoragePool()));
    }

    private void storagePoolWithLowerVersion() {
        cmd = new UpdateStoragePoolCommand<StoragePoolManagementParameter>(
                new StoragePoolManagementParameter(createLowerVersionStoragePool()));
    }

    private void storagePoolWithInvalidVersion() {
        cmd = new UpdateStoragePoolCommand<StoragePoolManagementParameter>(
                new StoragePoolManagementParameter(createInvalidVersionStoragePool()));
    }

    private void storagePoolWithLocalFS() {
        cmd = new UpdateStoragePoolCommand<StoragePoolManagementParameter>(
                new StoragePoolManagementParameter(createDefaultStoragePool()));
    }

    private void domainListNotEmpty() {
        when(sdList.size()).thenReturn(1);
    }

    private Set<Version> createVersionSet() {
        Set<Version> versions = new HashSet<Version>();
        versions.add(VERSION_1_0);
        versions.add(VERSION_1_1);
        versions.add(VERSION_1_2);
        return versions;
    }

    private storage_pool createNewStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.NFS);
        pool.setcompatibility_version(VERSION_1_1);
        return pool;
    }

    private storage_pool createDefaultStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        pool.setcompatibility_version(VERSION_1_1);
        return pool;
    }

    private storage_pool createLowerVersionStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        pool.setcompatibility_version(VERSION_1_0);
        return pool;
    }

    private storage_pool createBasicPool() {
        storage_pool pool = new storage_pool();
        pool.setId(Guid.NewGuid());
        pool.setname("Default");
        return pool;
    }

    private storage_pool createHigherVersionStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        pool.setcompatibility_version(VERSION_1_2);
        return pool;
    }

    private storage_pool createInvalidVersionStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        pool.setcompatibility_version(VERSION_2_0);
        return pool;
    }

    private List<VDSGroup> createClusterList() {
        List<VDSGroup> clusters = new ArrayList<VDSGroup>();
        VDSGroup cluster = new VDSGroup();
        cluster.setcompatibility_version(VERSION_1_0);
        clusters.add(cluster);
        return clusters;
    }

    private void addDefaultClusterToPool() {
        VDSGroup defaultCluster = new VDSGroup();
        defaultCluster.setcompatibility_version(VERSION_1_1);
        defaultCluster.setID(VDSGroup.DEFAULT_VDS_GROUP_ID);
        List<VDSGroup> clusters = new ArrayList<VDSGroup>();
        clusters.add(defaultCluster);
        when(vdsDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusters);
    }

    private void canDoActionFailed(final String reason) {
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(reason));
    }
}
