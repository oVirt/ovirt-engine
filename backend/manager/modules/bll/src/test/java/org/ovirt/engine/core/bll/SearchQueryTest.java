package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.searchbackend.SearchObjectAutoCompleter;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Config.class })
public class SearchQueryTest {

    List<DiskImage> diskImageResultList = new ArrayList<DiskImage>();
    List<Quota> quotaResultList = new ArrayList<Quota>();
    List<VM> vmResultList = new ArrayList<VM>();
    List<VDS> vdsResultList = new ArrayList<VDS>();
    List<VDSGroup> vdsGroupResultList = new ArrayList<VDSGroup>();
    List<storage_pool> storagePoolResultList = new ArrayList<storage_pool>();
    List<GlusterVolumeEntity> glusterVolumeList = new ArrayList<GlusterVolumeEntity>();

    public SearchQueryTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Config.class);
        mockStatic(DbFacade.class);

    }

    @Before
    public void setup() throws Exception {
        mockDAO();
        mockConfig();
        MockitoAnnotations.initMocks(this);
    }

    private void mockDAO() {
        final DiskImageDAO diskImageDAO = Mockito.mock(DiskImageDAO.class);
        final QuotaDAO quotaDAO = Mockito.mock(QuotaDAO.class);
        final VmDAO vmDAO = Mockito.mock(VmDAO.class);
        final VdsDAO vdsDAO = Mockito.mock(VdsDAO.class);
        final VdsGroupDAO vdsGroupDAO = Mockito.mock(VdsGroupDAO.class);
        final StoragePoolDAO storagePoolDAO = Mockito.mock(StoragePoolDAO.class);
        final GlusterVolumeDao glusterVolumeDao = Mockito.mock(GlusterVolumeDao.class);
        final DbEngineDialect dbEngineDialect = Mockito.mock(DbEngineDialect.class);
        final DbFacade facadeMock = new DbFacade() {
            @Override
            public DiskImageDAO getDiskImageDAO() {
                return diskImageDAO;
            }

            @Override
            public VmDAO getVmDAO() {
                return vmDAO;
            }

            @Override
            public VdsDAO getVdsDAO() {
                return vdsDAO;
            }

            @Override
            public VdsGroupDAO getVdsGroupDAO() {
                return vdsGroupDAO;
            }

            @Override
            public StoragePoolDAO getStoragePoolDAO() {
                return storagePoolDAO;
            }

            @Override
            public DbEngineDialect getDbEngineDialect() {
                return dbEngineDialect;
            }

            @Override
            public GlusterVolumeDao getGlusterVolumeDao() {
                return glusterVolumeDao;
            }

            @Override
            public QuotaDAO getQuotaDAO() {
                return quotaDAO;
            }
        };

        Mockito.when(DbFacade.getInstance()).thenReturn(facadeMock);
        Mockito.when(dbEngineDialect.getPreSearchQueryCommand()).thenReturn("");

        // mock DAOs
        mockDiskImageDAO(diskImageDAO);
        mockQuotaDAO(quotaDAO);
        mockVMDAO(vmDAO);
        mockVdsDAO(vdsDAO);
        mockVdsGroupDAO(vdsGroupDAO);
        mockStoragePoolDAO(storagePoolDAO);
        mockGlusterVolumeDao(glusterVolumeDao);
    }

    /**
     * Mock disk image DAO so that when getAllWithQuery will be called with the appropriate query string, a unique list will
     * be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     *
     * @param diskImageDao
     *            - The dao to be used
     */
    private void mockDiskImageDAO(final DiskImageDAO diskImageDAO) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter(false);
        Mockito.when(diskImageDAO.getAllWithQuery(Matchers.matches(getDiskImageRegexString(search))))
                .thenReturn(diskImageResultList);
    }

    private void mockQuotaDAO(final QuotaDAO quotaDAO) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter(false);
        Mockito.when(quotaDAO.getAllWithQuery(Matchers.matches(getQuotaRegexString(search))))
                .thenReturn(quotaResultList);
    }

    private String getQuotaRegexString(SearchObjectAutoCompleter search) {
        return ".*" + search.getDefaultSort(SearchObjects.QUOTA_OBJ_NAME) + ".*"
                + search.getRelatedTableNameWithOutTags(SearchObjects.QUOTA_OBJ_NAME) + ".* "
                + search.getPrimeryKeyName(SearchObjects.QUOTA_OBJ_NAME) + ".*";
    }

    /**
     * Mock vds group DAO so that when getAllWithQuery will be called with the appropriate query string, a unique list will
     * be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     *
     * @param vdsGroupDAO
     *            - The dao to be used
     */
    private void mockVdsGroupDAO(final VdsGroupDAO vdsGroupDAO) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter(false);
        Mockito.when(vdsGroupDAO.getAllWithQuery(Matchers.matches(getVdsGroupRegexString(search))))
                .thenReturn(vdsGroupResultList);
    }

    /**
     * Mock storage pool DAO so that when getAllWithQuery will be called with the appropriate query string, a unique list will
     * be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     *
     * @param storagePoolDAO
     *            - The dao to be used
     */
    private void mockStoragePoolDAO(final StoragePoolDAO storagePoolDAO) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter(false);
        Mockito.when(storagePoolDAO.getAllWithQuery(Matchers.matches(getStoragePoolRegexString(search))))
                .thenReturn(storagePoolResultList);
    }

    /**
     * Mock Gluster Volume DAO so that when getAllWithQuery will be called with the appropriate query string, a unique
     * list will be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     *
     * @param glusterVolumeDao
     *            - The dao to be used
     */
    private void mockGlusterVolumeDao(final GlusterVolumeDao glusterVolumeDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter(false);
        Mockito.when(glusterVolumeDao.getAllWithQuery(Matchers.matches(getGlusterVolumeRegexString(search))))
                .thenReturn(glusterVolumeList);
    }

    /**
     * Regex string which contains all of the disk image properties.
     *
     * @param search
     */
    private String getDiskImageRegexString(SearchObjectAutoCompleter search) {
        return ".*" + search.getDefaultSort(SearchObjects.DISK_OBJ_NAME) + ".*"
                + search.getRelatedTableNameWithOutTags(SearchObjects.DISK_OBJ_NAME) + ".* "
                + search.getPrimeryKeyName(SearchObjects.DISK_OBJ_NAME) + ".*";
    }

    /**
     * Mock Vds DAO so that when getAllWithQuery will be called with the appropriate query string, a unique list will
     * be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     *
     * @param diskImageDao
     *            - The dao to be used
     */
    private void mockVdsDAO(final VdsDAO vdsDAO) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter(false);
        Mockito.when(vdsDAO.getAllWithQuery(Matchers.matches(getVdsRegexString(search))))
                .thenReturn(vdsResultList);
    }

    /**
     * Mock VM DAO so that when getAllWithQuery will be called with the appropriate query string, a unique list will
     * be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     *
     * @param vmDAO
     *            - The dao to be used
     */
    private void mockVMDAO(final VmDAO vmDAO) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter(false);
        Mockito.when(vmDAO.getAllUsingQuery(Matchers.matches(getVMRegexString(search))))
                .thenReturn(vmResultList);
    }

    /**
     * Regex string which contains all of the VM properties.
     *
     * @param search
     */
    private String getVMRegexString(SearchObjectAutoCompleter search) {
        return ".*" + search.getDefaultSort(SearchObjects.VM_OBJ_NAME) + ".*"
                + search.getRelatedTableNameWithOutTags(SearchObjects.VM_OBJ_NAME) + ".* "
                + search.getPrimeryKeyName(SearchObjects.VM_OBJ_NAME) + ".*";
    }

    /**
     * Regex string which contains all of the VDS properties.
     *
     * @param search
     */
    private String getVdsRegexString(SearchObjectAutoCompleter search) {
        return ".*" + search.getDefaultSort(SearchObjects.VDS_OBJ_NAME) + ".*"
                + search.getRelatedTableNameWithOutTags(SearchObjects.VDS_OBJ_NAME) + ".* "
                + search.getPrimeryKeyName(SearchObjects.VDS_OBJ_NAME) + ".*";
    }

    /**
     * Regex string which contains all of the Vds group properties.
     *
     * @param search
     */
    private String getVdsGroupRegexString(SearchObjectAutoCompleter search) {
        return ".*" + search.getDefaultSort(SearchObjects.VDC_CLUSTER_OBJ_NAME) + ".*"
                + search.getRelatedTableNameWithOutTags(SearchObjects.VDC_CLUSTER_OBJ_NAME) + ".* "
                + search.getPrimeryKeyName(SearchObjects.VDC_CLUSTER_OBJ_NAME) + ".*";
    }

    /**
     * Regex string which contains all of the storage pool properties.
     *
     * @param search
     */
    private String getStoragePoolRegexString(SearchObjectAutoCompleter search) {
        return ".*" + search.getDefaultSort(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME) + ".*"
                + search.getRelatedTableNameWithOutTags(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME) + ".* "
                + search.getPrimeryKeyName(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME) + ".*";
    }

    /**
     * Regex string which contains all of the Gluster Volume properties.
     *
     * @param search
     */
    private String getGlusterVolumeRegexString(SearchObjectAutoCompleter search) {
        return ".*" + search.getDefaultSort(SearchObjects.GLUSTER_VOLUME_OBJ_NAME) + ".*"
                + search.getRelatedTableNameWithOutTags(SearchObjects.GLUSTER_VOLUME_OBJ_NAME) + ".* "
                + search.getPrimeryKeyName(SearchObjects.GLUSTER_VOLUME_OBJ_NAME) + ".*";
    }

    /**
     * Mock the configuration values used in the search logic.
     */
    private void mockConfig() {
        when(Config.<Object> GetValue(ConfigValues.SearchResultsLimit)).thenReturn(100);
        when(Config.<Object> GetValue(ConfigValues.DBSearchTemplate)).thenReturn("SELECT * FROM (SELECT *, ROW_NUMBER() OVER(%1$s) as RowNum FROM (%2$s)) as T1 ) as T2 %3$s");
    }

    @Test
    public void testGetAllMultiDiskImageSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Disks:", SearchType.DiskImage);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(diskImageResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllDiskImageSearch() throws Exception {
        // The query Should be used is : "SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY disk_name ASC ) as RowNum
        // FROM (SELECT * FROM vm_images_view WHERE ( image_guid IN (SELECT vm_images_view.image_guid FROM
        // vm_images_view ))) as T1 ) as T2"
        SearchParameters searchParam = new SearchParameters("Disk:", SearchType.DiskImage);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(diskImageResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllVMSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("VM:", SearchType.VM);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(vmResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllMultiVmSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("VMs:", SearchType.VM);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(vmResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllVdsSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Host:", SearchType.VDS);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(vdsResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllMultiVdsSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Hosts:", SearchType.VDS);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(vdsResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllClusterSearch() throws Exception {
        // The original query should be : SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY name ASC ) as RowNum FROM
        // (SELECT * FROM vds_groups WHERE ( vds_group_id IN (SELECT vds_groups_storage_domain.vds_group_id FROM
        // vds_groups_storage_domain ))) as T1 ) as T2
        SearchParameters searchParam = new SearchParameters("Cluster:", SearchType.Cluster);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(vdsGroupResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllMultiClusterSearch() throws Exception {
        // The original query should be : SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY name ASC ) as RowNum FROM
        // (SELECT * FROM vds_groups WHERE ( vds_group_id IN (SELECT vds_groups_storage_domain.vds_group_id FROM
        // vds_groups_storage_domain ))) as T1 ) as T2
        SearchParameters searchParam = new SearchParameters("Clusters:", SearchType.Cluster);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(vdsGroupResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllStoragePoolSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Datacenter:", SearchType.StoragePool);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(storagePoolResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    // TODO: Search using search text "Datacenters:" is not supported.
    @Ignore
    @Test
    public void testGetAllMultiStoragePoolSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Datacenters:", SearchType.StoragePool);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(storagePoolResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllGlusterVolumesSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Volumes:", SearchType.GlusterVolume);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(glusterVolumeList == searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllQuotaSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Quota:", SearchType.Quota);
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<SearchParameters>(searchParam));
        searchQuery.executeQueryCommand();
        assertTrue(quotaResultList == searchQuery.getQueryReturnValue().getReturnValue());
    }
}
