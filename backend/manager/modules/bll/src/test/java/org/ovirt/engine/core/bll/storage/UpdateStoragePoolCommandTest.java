package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStoragePoolCommandTest {

    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
    private static final Version VERSION_2_0 = new Version(2, 0);

    private UpdateStoragePoolCommand<StoragePoolManagementParameter> cmd;

    @Mock
    private StoragePoolDAO spDao;
    @Mock
    private StorageDomainStaticDAO sdDao;
    @Mock
    private List<StorageDomainStatic> sdList;
    @Mock
    private VdsGroupDAO vdsDao;

    @Before
    public void setUp() {
        when(spDao.get(any(Guid.class))).thenReturn(createDefaultStoragePool());
        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(sdList);
        when(vdsDao.getAllForStoragePool(any(Guid.class))).thenReturn(createClusterList());

        spyCommand(new StoragePoolManagementParameter(createNewStoragePool()));
    }

    protected void spyCommand(StoragePoolManagementParameter params) {
        UpdateStoragePoolCommand<StoragePoolManagementParameter> realCommand =
                new UpdateStoragePoolCommand<StoragePoolManagementParameter>(params);

        StoragePoolValidator validator = spy(realCommand.createStoragePoolValidator());
        doReturn(vdsDao).when(validator).getVdsGroupDao();

        cmd = spy(realCommand);
        doReturn(10).when(cmd).getStoragePoolNameSizeLimit();
        doReturn(createVersionSet().contains(cmd.getStoragePool().getcompatibility_version())).when(cmd)
                .isStoragePoolVersionSupported();
        doReturn(spDao).when(cmd).getStoragePoolDAO();
        doReturn(sdDao).when(cmd).getStorageDomainStaticDAO();
        doReturn(vdsDao).when(cmd).getVdsGroupDAO();
        doReturn(validator).when(cmd).createStoragePoolValidator();
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
        canDoActionFailed(VdcBllMessages.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS.toString());
    }

    @Test
    public void testValidateAllClustersLevel() {
        storagePoolWithVersionHigherThanCluster();
        List<VDSGroup> clusterList = createClusterList();
        // Create new supported cluster.
        VDSGroup secondCluster = new VDSGroup();
        secondCluster.setcompatibility_version(VERSION_1_2);
        secondCluster.setname("secondCluster");
        clusterList.add(secondCluster);

        // Create new unsupported cluster.
        VDSGroup thirdCluster = new VDSGroup();
        thirdCluster.setcompatibility_version(VERSION_1_1);
        thirdCluster.setname("thirdCluster");
        clusterList.add(thirdCluster);

        // Test upgrade
        when(vdsDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusterList);
        assertFalse(cmd.checkAllClustersLevel());
        List<String> messages = cmd.getReturnValue().getCanDoActionMessages();
        assertTrue(messages.contains(VdcBllMessages.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS.toString()));
        assertTrue(messages.get(0).contains("firstCluster"));
        assertFalse(messages.get(0).contains("secondCluster"));
        assertTrue(messages.get(0).contains("thirdCluster"));
    }

    @Test
    public void poolHasDefaultCluster() {
        addDefaultClusterToPool();
        storagePoolWithLocalFS();
        canDoActionFailed(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS.toString());
    }

    private void newPoolNameIsAlreadyTaken() {
        when(spDao.get(any(Guid.class))).thenReturn(new storage_pool("foo", Guid.Empty, "foo",
                StorageType.NFS.getValue(), StoragePoolStatus.Up.getValue()));
        when(spDao.getByName(anyString())).thenReturn(createDefaultStoragePool());
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
        when(sdList.size()).thenReturn(1);
    }

    private static Set<Version> createVersionSet() {
        Set<Version> versions = new HashSet<Version>();
        versions.add(VERSION_1_0);
        versions.add(VERSION_1_1);
        versions.add(VERSION_1_2);
        return versions;
    }

    private static storage_pool createNewStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.NFS);
        pool.setcompatibility_version(VERSION_1_1);
        return pool;
    }

    private static storage_pool createDefaultStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        pool.setcompatibility_version(VERSION_1_1);
        return pool;
    }

    private static storage_pool createLowerVersionStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        pool.setcompatibility_version(VERSION_1_0);
        return pool;
    }

    private static storage_pool createBasicPool() {
        storage_pool pool = new storage_pool();
        pool.setId(Guid.NewGuid());
        pool.setname("Default");
        return pool;
    }

    private static storage_pool createHigherVersionStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        pool.setcompatibility_version(VERSION_1_2);
        return pool;
    }

    private static storage_pool createInvalidVersionStoragePool() {
        storage_pool pool = createBasicPool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        pool.setcompatibility_version(VERSION_2_0);
        return pool;
    }

    private static List<VDSGroup> createClusterList() {
        List<VDSGroup> clusters = new ArrayList<VDSGroup>();
        VDSGroup cluster = new VDSGroup();
        cluster.setcompatibility_version(VERSION_1_0);
        cluster.setname("firstCluster");
        clusters.add(cluster);
        return clusters;
    }

    private void addDefaultClusterToPool() {
        VDSGroup defaultCluster = new VDSGroup();
        defaultCluster.setcompatibility_version(VERSION_1_1);
        defaultCluster.setId(VDSGroup.DEFAULT_VDS_GROUP_ID);
        List<VDSGroup> clusters = new ArrayList<VDSGroup>();
        clusters.add(defaultCluster);
        when(vdsDao.getAllForStoragePool(any(Guid.class))).thenReturn(clusters);
    }

    private void canDoActionFailed(final String reason) {
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(reason));
    }
}
