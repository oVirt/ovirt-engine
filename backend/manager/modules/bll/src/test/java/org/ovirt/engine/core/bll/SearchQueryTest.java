package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.utils.CommonConstants;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkViewDao;
import org.ovirt.engine.core.searchbackend.SearchObjectAutoCompleter;
import org.ovirt.engine.core.searchbackend.SearchObjects;

public class SearchQueryTest extends DbDependentTestBase {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.SearchResultsLimit, 100),
            mockConfig(ConfigValues.DBSearchTemplate,
                    "SELECT * FROM (SELECT *, ROW_NUMBER() OVER(%1$s) as RowNum FROM (%2$s)) as T1 ) as T2 %3$s"),
            mockConfig(ConfigValues.MinimumPercentageToUpdateQuotaCache, 60)
            );

    List<Disk> diskImageResultList = new ArrayList<>();
    List<Quota> quotaResultList = new ArrayList<>();
    List<VM> vmResultList = new ArrayList<>();
    List<VmTemplate> vmTemplateResultList = new ArrayList<>();
    List<VmTemplate> vmTemplateDaoResultList = new ArrayList<>();
    List<VDS> vdsResultList = new ArrayList<>();
    List<Cluster> clusterResultList = new ArrayList<>();
    List<StoragePool> storagePoolResultList = new ArrayList<>();
    List<GlusterVolumeEntity> glusterVolumeList = new ArrayList<>();
    List<NetworkView> networkResultList = new ArrayList<>();

    @Before
    public void setup() {
        DbFacade facadeMock = DbFacade.getInstance();
        final DiskDao diskDao = mock(DiskDao.class);
        final QuotaDao quotaDao = mock(QuotaDao.class);
        final VmDao vmDao = mock(VmDao.class);
        final VmTemplateDao vmTemplateDao = mock(VmTemplateDao.class);
        final VdsDao vdsDao = mock(VdsDao.class);
        final ClusterDao clusterDao = mock(ClusterDao.class);
        final StoragePoolDao storagePoolDao = mock(StoragePoolDao.class);
        final GlusterVolumeDao glusterVolumeDao = mock(GlusterVolumeDao.class);
        final NetworkViewDao networkViewDao = mock(NetworkViewDao.class);

        when(facadeMock.getDiskDao()).thenReturn(diskDao);
        when(facadeMock.getQuotaDao()).thenReturn(quotaDao);
        when(facadeMock.getVmDao()).thenReturn(vmDao);
        when(facadeMock.getVmTemplateDao()).thenReturn(vmTemplateDao);
        when(facadeMock.getVdsDao()).thenReturn(vdsDao);
        when(facadeMock.getClusterDao()).thenReturn(clusterDao);
        when(facadeMock.getStoragePoolDao()).thenReturn(storagePoolDao);
        when(facadeMock.getGlusterVolumeDao()).thenReturn(glusterVolumeDao);
        when(facadeMock.getNetworkViewDao()).thenReturn(networkViewDao);
        // mock Daos
        mockDiskDao(diskDao);
        mockQuotaDao(quotaDao);
        mockVMDao(vmDao);
        mockVMTemplateDao(vmTemplateDao);
        mockVdsDao(vdsDao);
        mockClusterDao(clusterDao);
        mockStoragePoolDao(storagePoolDao);
        mockGlusterVolumeDao(glusterVolumeDao);
        mockNetworkDao(networkViewDao);
    }

    /**
     * Mock disk Dao so that when getAllWithQuery will be called with the appropriate query string, a unique list will
     * be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     * @param diskDao
     *            - The dao to be used
     */
    private void mockDiskDao(final DiskDao diskDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(diskDao.getAllWithQuery(matches(getDiskImageRegexString(search))))
                .thenReturn(diskImageResultList);
    }

    private void mockQuotaDao(final QuotaDao quotaDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(quotaDao.getAllWithQuery(matches(getQuotaRegexString(search))))
                .thenReturn(quotaResultList);
    }

    private static String getQuotaRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();

        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.QUOTA_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.QUOTA_OBJ_NAME, false))
                .append(".* ");
        return query.toString();

    }

    /**
     * Mock vds group Dao so that when getAllWithQuery will be called with the appropriate query string, a unique list
     * will be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     * @param clusterDao
     *            - The dao to be used
     */
    private void mockClusterDao(final ClusterDao clusterDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(clusterDao.getAllWithQuery(matches(getClusterRegexString(search))))
                .thenReturn(clusterResultList);
    }

    /**
     * Mock storage pool Dao so that when getAllWithQuery will be called with the appropriate query string, a unique
     * list will be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     * @param storagePoolDao
     *            - The dao to be used
     */
    private void mockStoragePoolDao(final StoragePoolDao storagePoolDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(storagePoolDao.getAllWithQuery(matches(getStoragePoolRegexString(search))))
                .thenReturn(storagePoolResultList);
    }

    /**
     * Mock Gluster Volume Dao so that when getAllWithQuery will be called with the appropriate query string, a unique
     * list will be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     * @param glusterVolumeDao
     *            - The dao to be used
     */
    private void mockGlusterVolumeDao(final GlusterVolumeDao glusterVolumeDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(glusterVolumeDao.getAllWithQuery(matches(getGlusterVolumeRegexString(search))))
                .thenReturn(glusterVolumeList);
    }

    private void mockNetworkDao(final NetworkViewDao networkViewDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(networkViewDao.getAllWithQuery(matches(getNetworkRegexString(search))))
                .thenReturn(networkResultList);
    }

    /**
     * Regex string which contains all of the disk image properties.
     */
    private static String getDiskImageRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();

        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.DISK_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.DISK_OBJ_NAME, false))
                .append(".* ");
        return query.toString();
    }

    /**
     * Mock Vds Dao so that when getAllWithQuery will be called with the appropriate query string, a unique list will be
     * returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     * @param diskImageDao
     *            - The dao to be used
     */
    private void mockVdsDao(final VdsDao vdsDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(vdsDao.getAllWithQuery(matches(getVdsRegexString(search))))
                .thenReturn(vdsResultList);
        VDS vds = new VDS();
        vds.setCpuFlags("flag");
        vds.setClusterCompatibilityVersion(Version.getLast());
        vdsResultList.add(vds);
    }

    /**
     * Mock VM Dao so that when getAllWithQuery will be called with the appropriate query string, a unique list will be
     * returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     * @param vmDao
     *            - The dao to be used
     */
    private void mockVMDao(final VmDao vmDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(vmDao.getAllUsingQuery(matches(getVMRegexString(search))))
                .thenReturn(vmResultList);
    }

    /**
     * Mock VM Template Dao so that when getAllWithQuery will be called with the appropriate query string, a unique list
     * will be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     * @param vmTemplateDao
     *            - The dao to be used
     */
    private void mockVMTemplateDao(final VmTemplateDao vmTemplateDao) {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(vmTemplateDao.getAllWithQuery(matches(getVMTemplateRegexString(search))))
                .thenReturn(vmTemplateResultList);

        // A template returned by the DAO and by the SearchQuery
        VmTemplate goodTemplate = new VmTemplate();
        goodTemplate.setName("Good template");
        goodTemplate.setTemplateType(VmEntityType.TEMPLATE);
        vmTemplateDaoResultList.add(goodTemplate);
        vmTemplateResultList.add(goodTemplate);

        // A template returned by the DAO and removed by the SearchQuery
        VmTemplate badTemplate = new VmTemplate();
        badTemplate.setTemplateType(VmEntityType.INSTANCE_TYPE);
        badTemplate.setName("Bad template");
        vmTemplateDaoResultList.add(badTemplate);
    }

    /**
     * Regex string which contains all of the VM properties.
     */
    private static String getVMRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();
        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.VM_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.VM_OBJ_NAME, false))
                .append(".* ");
        return query.toString();
    }

    /**
     * Regex string which contains all of the VM Template properties.
     */
    private static String getVMTemplateRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();
        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.TEMPLATE_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.TEMPLATE_OBJ_NAME, false))
                .append(".* ");
        return query.toString();
    }

    /**
     * Regex string which contains all of the VDS properties.
     */
    private static String getVdsRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();

        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.VDS_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.VDS_OBJ_NAME, false))
                .append(".* ");
        return query.toString();
    }

    /**
     * Regex string which contains all of the Vds group properties.
     */
    private static String getClusterRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();

        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.VDC_CLUSTER_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.VDC_CLUSTER_OBJ_NAME, false))
                .append(".* ");
        return query.toString();
    }

    /**
     * Regex string which contains all of the storage pool properties.
     */
    private static String getStoragePoolRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();

        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME, false))
                .append(".* ");
        return query.toString();
    }

    /**
     * Regex string which contains all of the Gluster Volume properties.
     */
    private static String getGlusterVolumeRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();

        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.GLUSTER_VOLUME_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.GLUSTER_VOLUME_OBJ_NAME, false))
                .append(".* ");
        return query.toString();
    }

    /**
     * Regex string which contains all of the Network properties.
     */
    private static String getNetworkRegexString(SearchObjectAutoCompleter search) {
        StringBuilder query = new StringBuilder();
        query.append(".*")
                .append(search.getDefaultSort(SearchObjects.NETWORK_OBJ_NAME))
                .append(".*")
                .append(search.getRelatedTableName(SearchObjects.NETWORK_OBJ_NAME, false))
                .append(".* ");
        return query.toString();
    }

    private void mockInjections(SearchQuery<SearchParameters> searchQuery) {
        QuotaManager quotaManager = mock(QuotaManager.class);
        doNothing().when(quotaManager).updateUsage(anyListOf(Quota.class));
        when(searchQuery.getQuotaManager()).thenReturn(quotaManager);

        CpuFlagsManagerHandler cpuFlagsManagerHandler = mock(CpuFlagsManagerHandler.class);
        ServerCpu resultCpu = new ServerCpu();
        resultCpu.setCpuName("cpu");
        when(cpuFlagsManagerHandler.findMaxServerCpuByFlags("flag", Version.getLast())).thenReturn(resultCpu);
        when(searchQuery.getCpuFlagsManagerHandler()).thenReturn(cpuFlagsManagerHandler);
    }

    private SearchQuery<SearchParameters> spySearchQuery(SearchParameters searchParam) {
        SearchQuery<SearchParameters> searchQuery = spy(new SearchQuery<>(searchParam));
        mockInjections(searchQuery);
        return searchQuery;
    }

    @Test
    public void testGetAllMultiDiskImageSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Disks" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.Disk);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(diskImageResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllDiskImageSearch() throws Exception {
        // The query Should be used is : "SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY disk_name ASC ) as RowNum
        // FROM (SELECT * FROM vm_images_view WHERE ( image_guid IN (SELECT vm_images_view.image_guid FROM
        // vm_images_view ))) as T1 ) as T2"
        SearchParameters searchParam = new SearchParameters("Disk" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.Disk);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(diskImageResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllVMSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("VM" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.VM);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(vmResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllVMTemplatesSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Template" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.VmTemplate);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(vmTemplateResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }


    @Test
    public void testGetAllMultiVmSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("VMs" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.VM);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(vmResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllVdsSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Host" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.VDS);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(vdsResultList, searchQuery.getQueryReturnValue().getReturnValue());
        assertEquals(1, vdsResultList.size());
        assertEquals("cpu", vdsResultList.get(0).getCpuName().getCpuName());
    }

    @Test
    public void testGetAllMultiVdsSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Hosts" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.VDS);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(vdsResultList, searchQuery.getQueryReturnValue().getReturnValue());
        assertEquals(1, vdsResultList.size());
        assertEquals("cpu", vdsResultList.get(0).getCpuName().getCpuName());
    }

    @Test
    public void testGetAllClusterSearch() throws Exception {
        // The original query should be : SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY name ASC ) as RowNum FROM
        // (SELECT * FROM clusters WHERE ( cluster_id IN (SELECT cluster_storage_domain.cluster_id FROM
        // cluster_storage_domain ))) as T1 ) as T2
        SearchParameters searchParam = new SearchParameters("Cluster" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.Cluster);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(clusterResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllMultiClusterSearch() throws Exception {
        // The original query should be : SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY name ASC ) as RowNum FROM
        // (SELECT * FROM clusters WHERE ( cluster_id IN (SELECT cluster_storage_domain.cluster_id FROM
        // cluster_storage_domain ))) as T1 ) as T2
        SearchParameters searchParam = new SearchParameters("Clusters" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.Cluster);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(clusterResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllStoragePoolSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Datacenter" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.StoragePool);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(storagePoolResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    // TODO: Search using search text "Datacenters:" is not supported.
    @Ignore
    @Test
    public void testGetAllMultiStoragePoolSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Datacenters" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.StoragePool);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(storagePoolResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllGlusterVolumesSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Volumes" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.GlusterVolume);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(glusterVolumeList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllQuotaSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Quota" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.Quota);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(quotaResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllNetworkSearch() throws Exception {
        SearchParameters searchParam = new SearchParameters("Network" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR, SearchType.Network);
        SearchQuery<SearchParameters> searchQuery = spySearchQuery(searchParam);
        searchQuery.executeQueryCommand();
        assertEquals(networkResultList, searchQuery.getQueryReturnValue().getReturnValue());
    }
}
