package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkViewDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.searchbackend.SearchObjectAutoCompleter;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.lock.LockManager;

@MockitoSettings(strictness = Strictness.LENIENT)
public class SearchQueryTest extends AbstractQueryTest<SearchParameters, SearchQuery<SearchParameters>> {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.concat(AbstractQueryTest.mockConfiguration(),
                Stream.of(
                        MockConfigDescriptor.of(ConfigValues.UserSessionTimeOutInterval, 30),
                        MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels,
                                new HashSet<>(Collections.singletonList(new Version(3, 0)))),
                        MockConfigDescriptor.of(ConfigValues.DBEngine, null),
                        MockConfigDescriptor.of(ConfigValues.DBPagingType, null),
                        MockConfigDescriptor.of(ConfigValues.DBSearchTemplate,
                                "SELECT * FROM (SELECT *, ROW_NUMBER() OVER(%1$s) as RowNum FROM (%2$s)) as T1 ) as T2 %3$s")
                )
        );
    }

    @Mock
    private QuotaManager quotaManager;
    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Mock
    private DiskDao diskDao;
    @Mock
    private QuotaDao quotaDao;
    @Mock
    private VmDao vmDao;
    @Mock
    private VmTemplateDao vmTemplateDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private GlusterVolumeDao glusterVolumeDao;
    @Mock
    private NetworkViewDao networkViewDao;
    @Mock
    private LockManager lockManager;
    @Mock
    private HostLocking hostLocking;
    @Mock
    private VmHandler vmHandler;
    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

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

    /**
     * Mock disk Dao so that when getAllWithQuery will be called with the appropriate query string, a unique list will
     * be returned. <BR/>
     * This returned list will indicate, if the correct string has been passed as an argument to the getAllWithQuery
     * API.
     * @param diskDao
     *            - The dao to be used
     */
    @BeforeEach
    public void mockDiskDao() {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(diskDao.getAllWithQuery(matches(getDiskImageRegexString(search))))
                .thenReturn(diskImageResultList);
    }

    @BeforeEach
    public void mockQuotaDao() {
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
    @BeforeEach
    public void mockClusterDao() {
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
    @BeforeEach
    public void mockStoragePoolDao() {
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
    @BeforeEach
    public void mockGlusterVolumeDao() {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(glusterVolumeDao.getAllWithQuery(matches(getGlusterVolumeRegexString(search))))
                .thenReturn(glusterVolumeList);
    }

    @BeforeEach
    public void mockNetworkDao() {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(networkViewDao.getAllWithQuery(matches(getNetworkRegexString(search))))
                .thenReturn(networkResultList);
    }

    @BeforeEach
    void mockLockManager() {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(lockManager.isExclusiveLockPresent(any())).thenReturn(Boolean.TRUE);
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
    @BeforeEach
    public void mockVdsDao() {
        SearchObjectAutoCompleter search = new SearchObjectAutoCompleter();
        when(vdsDao.getAllWithQuery(matches(getVdsRegexString(search))))
                .thenReturn(vdsResultList);
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
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
    public void mockVMDao() {
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
    @BeforeEach
    public void mockVMTemplateDao() {
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

    @BeforeEach
    public void mockCpuFlagsManagerHandler() {
        ServerCpu resultCpu = new ServerCpu();
        resultCpu.setCpuName("cpu");
        when(cpuFlagsManagerHandler.findMaxServerCpuByFlags("flag", Version.getLast())).thenReturn(resultCpu);
        ServerCpu resultCpu2 = new ServerCpu();
        ServerCpu resultCpu3 = new ServerCpu();
        ServerCpu resultCpu4 = new ServerCpu();
        when(cpuFlagsManagerHandler.findServerCpusByFlags("flag", Version.getLast()))
                .thenReturn(java.util.Arrays.asList(resultCpu, resultCpu2, resultCpu3, resultCpu4));
    }

    @Test
    public void testGetAllMultiDiskImageSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Disks" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.Disk);
        getQuery().executeQueryCommand();
        assertEquals(diskImageResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllDiskImageSearch() {
        // The query Should be used is : "SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY disk_name ASC ) as RowNum
        // FROM (SELECT * FROM vm_images_view WHERE ( image_guid IN (SELECT vm_images_view.image_guid FROM
        // vm_images_view ))) as T1 ) as T2"
        when(getQueryParameters().getSearchPattern()).thenReturn("Disk" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.Disk);
        getQuery().executeQueryCommand();
        assertEquals(diskImageResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllVMSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("VM" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.VM);
        when(vmNetworkInterfaceDao.getAllWithVnicOutOfSync(any())).thenReturn(Collections.emptyList());
        getQuery().executeQueryCommand();
        assertEquals(vmResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllVMTemplatesSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Template" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.VmTemplate);
        getQuery().executeQueryCommand();
        assertEquals(vmTemplateResultList, getQuery().getQueryReturnValue().getReturnValue());
    }


    @Test
    public void testGetAllMultiVmSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("VMs" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.VmTemplate);
        getQuery().executeQueryCommand();
        assertEquals(vmResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllVdsSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Host" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.VDS);
        getQuery().executeQueryCommand();
        assertEquals(vdsResultList, getQuery().getQueryReturnValue().getReturnValue());
        assertEquals(1, vdsResultList.size());
        assertEquals("cpu", vdsResultList.get(0).getCpuName().getCpuName());
        assertEquals(true, vdsResultList.get(0).isNetworkOperationInProgress());
    }

    @Test
    public void testGetAllMultiVdsSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Hosts" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.VDS);
        getQuery().executeQueryCommand();
        assertEquals(vdsResultList, getQuery().getQueryReturnValue().getReturnValue());
        assertEquals(1, vdsResultList.size());
        assertEquals("cpu", vdsResultList.get(0).getCpuName().getCpuName());
        assertEquals(true, vdsResultList.get(0).isNetworkOperationInProgress());
    }

    @Test
    public void testGetAllClusterSearch() {
        // The original query should be : SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY name ASC ) as RowNum FROM
        // (SELECT * FROM clusters WHERE ( cluster_id IN (SELECT cluster_storage_domain.cluster_id FROM
        // cluster_storage_domain ))) as T1 ) as T2
        when(getQueryParameters().getSearchPattern()).thenReturn("Cluster" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.Cluster);
        getQuery().executeQueryCommand();
        assertEquals(clusterResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllMultiClusterSearch() {
        // The original query should be : SELECT * FROM (SELECT *, ROW_NUMBER() OVER( ORDER BY name ASC ) as RowNum FROM
        // (SELECT * FROM clusters WHERE ( cluster_id IN (SELECT cluster_storage_domain.cluster_id FROM
        // cluster_storage_domain ))) as T1 ) as T2
        when(getQueryParameters().getSearchPattern()).thenReturn("Clusters" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.Cluster);
        getQuery().executeQueryCommand();
        assertEquals(clusterResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllStoragePoolSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Datacenter" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.StoragePool);
        getQuery().executeQueryCommand();
        assertEquals(storagePoolResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    // TODO: Search using search text "Datacenters:" is not supported.
    @Disabled
    @Test
    public void testGetAllMultiStoragePoolSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Datacenters" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.StoragePool);
        getQuery().executeQueryCommand();
        assertEquals(storagePoolResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllGlusterVolumesSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Volumes" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.GlusterVolume);
        getQuery().executeQueryCommand();
        assertEquals(glusterVolumeList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllQuotaSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Quota" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.Quota);
        getQuery().executeQueryCommand();
        assertEquals(quotaResultList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testGetAllNetworkSearch() {
        when(getQueryParameters().getSearchPattern()).thenReturn("Network" + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR);
        when(getQueryParameters().getSearchTypeValue()).thenReturn(SearchType.Network);
        getQuery().executeQueryCommand();
        assertEquals(networkResultList, getQuery().getQueryReturnValue().getReturnValue());
    }
}
