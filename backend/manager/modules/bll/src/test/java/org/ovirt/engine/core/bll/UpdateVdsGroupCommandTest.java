package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.cluster.DefaultManagementNetworkFinder;
import org.ovirt.engine.core.bll.network.cluster.UpdateClusterNetworkClusterValidator;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

@RunWith(MockitoJUnitRunner.class)
public class UpdateVdsGroupCommandTest {


    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
    private static final Guid DC_ID1 = Guid.newGuid();
    private static final Guid DC_ID2 = Guid.newGuid();
    private static final Guid DEFAULT_VDS_GROUP_ID = new Guid("99408929-82CF-4DC7-A532-9D998063FA95");
    private static final Guid TEST_MANAGEMENT_NETWORK_ID = Guid.newGuid();

    private static final Map<String, String> migrationMap = new HashMap<>();

    static {
        migrationMap.put("undefined", "true");
        migrationMap.put("x86_64", "true");
        migrationMap.put("ppc64", "false");
    }

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.IsMigrationSupported, VERSION_1_0.getValue(), migrationMap),
            mockConfig(ConfigValues.IsMigrationSupported, VERSION_1_1.getValue(), migrationMap),
            mockConfig(ConfigValues.IsMigrationSupported, VERSION_1_2.getValue(), migrationMap)
            );

    @Mock
    private VdsGroupDAO vdsGroupDAO;
    @Mock
    private VdsDAO vdsDAO;
    @Mock
    private StoragePoolDAO storagePoolDAO;
    @Mock
    private GlusterVolumeDao glusterVolumeDao;
    @Mock
    private VmDAO vmDao;
    @Mock
    private DefaultManagementNetworkFinder defaultManagementNetworkFinder;
    @Mock
    private UpdateClusterNetworkClusterValidator networkClusterValidator;

    @Mock
    private Network mockManagementNetwork = createManagementNetwork();

    private Network createManagementNetwork() {
        final Network network = new Network();
        network.setId(TEST_MANAGEMENT_NETWORK_ID);
        return network;
    }

    private UpdateVdsGroupCommand<VdsGroupOperationParameters> cmd;

    @Test
    public void nameInUse() {
        createSimpleCommand();
        createCommandWithDifferentName();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_DO_ACTION_NAME_IN_USE);
    }

    @Test
    public void invalidVdsGroup() {
        createSimpleCommand();
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(null);
        canDoActionFailedWithReason(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
    }

    @Test
    public void legalArchitectureChange() {
        createCommandWithDefaultVdsGroup();
        cpuExists();
        architectureIsUpdatable();
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void illegalArchitectureChange() {
        createCommandWithDefaultVdsGroup();
        clusterHasVMs();
        cpuExists();
        architectureIsNotUpdatable();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_ARCHITECTURE_ILLEGAL);
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
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CPU_IS_NOT_UPDATABLE);
    }

    @Test
    public void legalCpuUpdate() {
        createCommandWithDifferentCpuName();
        cpuExists();
        architectureIsUpdatable();
        assertTrue(cmd.canDoAction());
    }

    private void cpuIsNotUpdatable() {
        doReturn(false).when(cmd).isCpuUpdatable(any(VDSGroup.class));
    }

    @Test
    public void invalidCpuSelection() {
        createCommandWithDefaultVdsGroup();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_CPU_NOT_FOUND);
    }

    @Test
    public void illegalCpuChange() {
        createCommandWithDefaultVdsGroup();
        cpuExists();
        cpuManufacturersDontMatch();
        clusterHasVds();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_ILLEGAL);
    }

    @Test
    public void invalidVersion() {
        createCommandWithInvalidVersion();
        setupCpu();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_GIVEN_VERSION_NOT_SUPPORTED);
    }

    @Test
    public void versionDecreaseWithHost() {
        createCommandWithOlderVersion();
        setupCpu();
        VdsExist();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void versionDecreaseNoHostsOrNetwork() {
        createCommandWithOlderVersion();
        setupCpu();
        StoragePoolDAO storagePoolDAO2 = Mockito.mock(StoragePoolDAO.class);
        when(storagePoolDAO2.get(any(Guid.class))).thenReturn(createStoragePoolLocalFS());
        doReturn(storagePoolDAO2).when(cmd).getStoragePoolDAO();
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void versionDecreaseLowerVersionThanDC() {
        createCommandWithOlderVersion();
        StoragePoolDAO storagePoolDAO2 = Mockito.mock(StoragePoolDAO.class);
        when(storagePoolDAO2.get(any(Guid.class))).thenReturn(createStoragePoolLocalFSOldVersion());
        doReturn(storagePoolDAO2).when(cmd).getStoragePoolDAO();
        setupCpu();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION_UNDER_DC);
    }

    @Test
    public void updateWithLowerVersionThanHosts() {
        createCommandWithDefaultVdsGroup();
        setupCpu();
        VdsExistWithHigherVersion();
        architectureIsUpdatable();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS);
    }

    @Test
    public void updateWithCpuLowerThanHost() {
        createCommandWithDefaultVdsGroup();
        setupCpu();
        clusterHasVds();
        cpuFlagsMissing();
        architectureIsUpdatable();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS);
    }

    @Test
    public void updateStoragePool() {
        createCommandWithDifferentPool();
        setupCpu();
        clusterHasVds();
        cpuFlagsNotMissing();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_CHANGE_STORAGE_POOL);
    }

    @Test
    public void clusterAlreadyInLocalFs() {
        prepareManagementNetworkMocks();

        createCommandWithDefaultVdsGroup();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        setupCpu();
        allQueriesForVms();
        storagePoolAlreadyHasCluster();
        architectureIsUpdatable();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
    }

    @Test
    public void clusterMovesToDcWithNoDefaultManagementNetwork() {
        noNewDefaultManagementNetworkFound();

        createCommandWithDefaultVdsGroup();
        oldGroupIsDetachedDefault();
        setupCpu();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_DEFAULT_MANAGEMENT_NETWORK_NOT_FOUND);
    }

    @Test
    public void invalidDefaultManagementNetworkAttachement() {
        newDefaultManagementNetworkFound();
        final VdcBllMessages expected = VdcBllMessages.Unassigned;
        when(networkClusterValidator.managementNetworkChange()).thenReturn(new ValidationResult(expected));

        createCommandWithDefaultVdsGroup();
        oldGroupIsDetachedDefault();
        setupCpu();
        canDoActionFailedWithReason(expected);
    }

    private void setupCpu() {
        cpuExists();
        cpuManufacturersMatch();
    }

    @Test
    public void defaultClusterInLocalFs() {
        prepareManagementNetworkMocks();

        mcr.mockConfigValue(ConfigValues.AutoRegistrationDefaultVdsGroupID, DEFAULT_VDS_GROUP_ID);
        createCommandWithDefaultVdsGroup();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        setupCpu();
        allQueriesForVms();
        architectureIsUpdatable();
        canDoActionFailedWithReason(VdcBllMessages.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
    }

    private void prepareManagementNetworkMocks() {
        newDefaultManagementNetworkFound();
        when(networkClusterValidator.managementNetworkChange()).thenReturn(ValidationResult.VALID);
    }

    private void newDefaultManagementNetworkFound() {
        when(defaultManagementNetworkFinder.findDefaultManagementNetwork(DC_ID1)).
                thenReturn(mockManagementNetwork);
    }

    private void noNewDefaultManagementNetworkFound() {
        when(defaultManagementNetworkFinder.findDefaultManagementNetwork(DC_ID1)).
                thenReturn(null);
    }

    @Test
    public void vdsGroupWithNoCpu() {
        createCommandWithNoCpuName();
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createVdsGroupWithNoCpuName());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createVdsGroupWithNoCpuName());
        when(glusterVolumeDao.getByClusterId(any(Guid.class))).thenReturn(new ArrayList<GlusterVolumeEntity>());
        allQueriesForVms();
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void vdsGroupWithNoServiceEnabled() {
        createCommandWithNoService();
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createVdsGroupWithNoCpuName());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createVdsGroupWithNoCpuName());
        cpuExists();
        allQueriesForVms();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED);
    }

    @Test
    public void vdsGroupWithVirtGlusterServicesNotAllowed() {
        createCommandWithVirtGlusterEnabled();
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createVdsGroupWithNoCpuName());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createVdsGroupWithNoCpuName());
        mcr.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, Boolean.FALSE);
        mcr.mockConfigValue(ConfigValues.GlusterSupport, VERSION_1_1, Boolean.TRUE);
        cpuExists();
        allQueriesForVms();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED);
    }

    @Test
    public void vdspysGroupWithVirtGlusterNotSupported() {
        createCommandWithGlusterEnabled();
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createVdsGroupWithNoCpuName());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createVdsGroupWithNoCpuName());
        mcr.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, Boolean.FALSE);
        mcr.mockConfigValue(ConfigValues.GlusterSupport, VERSION_1_1, Boolean.FALSE);
        cpuExists();
        allQueriesForVms();
        canDoActionFailedWithReason(VdcBllMessages.GLUSTER_NOT_SUPPORTED);
    }

    @Test
    public void disableVirtWhenVmsExist() {
        createCommandWithGlusterEnabled();
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createDefaultVdsGroup());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createDefaultVdsGroup());
        mcr.mockConfigValue(ConfigValues.GlusterSupport, VERSION_1_1, Boolean.TRUE);
        cpuExists();
        cpuFlagsNotMissing();
        clusterHasVds();
        clusterHasVMs();

        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_DISABLE_VIRT_WHEN_CLUSTER_CONTAINS_VMS);
    }

    @Test
    public void disableGlusterWhenVolumesExist() {
        createCommandWithVirtEnabled();
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createVdsGroupWithNoCpuName());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createVdsGroupWithNoCpuName());
        cpuExists();
        cpuFlagsNotMissing();
        allQueriesForVms();
        clusterHasGlusterVolumes();

        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_DISABLE_GLUSTER_WHEN_CLUSTER_CONTAINS_VOLUMES);
    }

    private void createSimpleCommand() {
        createCommand(createNewVdsGroup());
    }

    private void createCommandWithOlderVersion() {
        createCommand(createVdsGroupWithOlderVersion());

    }

    private void createCommandWithInvalidVersion() {
        createCommand(createVdsGroupWithBadVersion());
    }

    private void createCommandWithDifferentPool() {
        createCommand(createVdsGroupWithDifferentPool());
    }

    private void createCommandWithDefaultVdsGroup() {
        createCommand(createDefaultVdsGroup());
    }

    private void createCommandWithDifferentCpuName() {
        createCommand(createDefaultVdsGroupWithDifferentCpuName());
    }

    private void createCommandWithNoCpuName() {
        createCommand(createVdsGroupWithNoCpuName());
    }

    private void createCommandWithNoService() {
        createCommand(createVdsGroupWith(false, false));
    }

    private void createCommandWithVirtEnabled() {
        createCommand(createVdsGroupWith(true, false));
    }

    private void createCommandWithGlusterEnabled() {
        createCommand(createVdsGroupWith(false, true));
    }

    private void createCommandWithVirtGlusterEnabled() {
        createCommand(createVdsGroupWith(true, true));
    }

    private void createCommand(final VDSGroup group) {
        setValidCpuVersionMap();
        VdsGroupOperationParameters params = new VdsGroupOperationParameters(group);
        cmd = spy(new UpdateVdsGroupCommand<VdsGroupOperationParameters>(params));

        doReturn(0).when(cmd).compareCpuLevels(any(VDSGroup.class));

        doReturn(vdsGroupDAO).when(cmd).getVdsGroupDAO();
        doReturn(vdsDAO).when(cmd).getVdsDAO();
        doReturn(storagePoolDAO).when(cmd).getStoragePoolDAO();
        doReturn(glusterVolumeDao).when(cmd).getGlusterVolumeDao();
        doReturn(vmDao).when(cmd).getVmDAO();
        doReturn(defaultManagementNetworkFinder).when(cmd).getDefaultManagementNetworkFinder();
        doReturn(networkClusterValidator).when(cmd).createManagementNetworkClusterValidator();
        doReturn(true).when(cmd).validateClusterPolicy();

        if (StringUtils.isEmpty(group.getCpuName())) {
            doReturn(ArchitectureType.undefined).when(cmd).getArchitecture();
        } else {
            doReturn(ArchitectureType.x86_64).when(cmd).getArchitecture();
        }

        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createDefaultVdsGroup());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createDefaultVdsGroup());
        List<VDSGroup> vdsGroupList = new ArrayList<VDSGroup>();
        vdsGroupList.add(createDefaultVdsGroup());
        when(vdsGroupDAO.getByName(anyString(), anyBoolean())).thenReturn(vdsGroupList);
    }

    private void createCommandWithDifferentName() {
        createCommand(createVdsGroupWithDifferentName());
    }

    private static VDSGroup createVdsGroupWithDifferentName() {
        VDSGroup group = new VDSGroup();
        group.setName("BadName");
        group.setCompatibilityVersion(VERSION_1_1);
        return group;
    }

    private static VDSGroup createNewVdsGroup() {
        VDSGroup group = new VDSGroup();
        group.setCompatibilityVersion(VERSION_1_1);
        group.setName("Default");
        return group;
    }

    private static VDSGroup createDefaultVdsGroup() {
        VDSGroup group = new VDSGroup();
        group.setName("Default");
        group.setId(DEFAULT_VDS_GROUP_ID);
        group.setCpuName("Intel Conroe");
        group.setCompatibilityVersion(VERSION_1_1);
        group.setStoragePoolId(DC_ID1);
        group.setArchitecture(ArchitectureType.x86_64);
        return group;
    }

    private static VDSGroup createDefaultVdsGroupWithDifferentCpuName() {
        VDSGroup group = createDefaultVdsGroup();

        group.setCpuName("Another CPU name");

        return group;
    }

    private static VDSGroup createVdsGroupWithNoCpuName() {
        VDSGroup group = new VDSGroup();
        group.setName("Default");
        group.setId(DEFAULT_VDS_GROUP_ID);
        group.setCompatibilityVersion(VERSION_1_1);
        group.setStoragePoolId(DC_ID1);
        group.setArchitecture(ArchitectureType.undefined);
        return group;
    }

    private static VDSGroup createDetachedDefaultVdsGroup() {
        VDSGroup group = createDefaultVdsGroup();
        group.setStoragePoolId(null);
        return group;
    }

    private static VDSGroup createVdsGroupWithOlderVersion() {
        VDSGroup group = createNewVdsGroup();
        group.setCompatibilityVersion(VERSION_1_0);
        group.setStoragePoolId(DC_ID1);
        return group;
    }

    private static VDSGroup createVdsGroupWithBadVersion() {
        VDSGroup group = createNewVdsGroup();
        group.setCompatibilityVersion(new Version(5, 0));
        return group;
    }

    private static VDSGroup createVdsGroupWithDifferentPool() {
        VDSGroup group = createNewVdsGroup();
        group.setStoragePoolId(DC_ID2);
        return group;
    }

    private static VDSGroup createVdsGroupWith(boolean virtService, boolean glusterService) {
        VDSGroup group = createDefaultVdsGroup();
        group.setVirtService(virtService);
        group.setGlusterService(glusterService);
        group.setCompatibilityVersion(VERSION_1_1);
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
        when(storagePoolDAO.get(DC_ID1)).thenReturn(createStoragePoolLocalFS());
    }

    private void oldGroupIsDetachedDefault() {
        when(vdsGroupDAO.get(DEFAULT_VDS_GROUP_ID)).thenReturn(createDetachedDefaultVdsGroup());
    }

    private void storagePoolAlreadyHasCluster() {
        VDSGroup group = new VDSGroup();
        List<VDSGroup> groupList = new ArrayList<VDSGroup>();
        groupList.add(group);
        when(vdsGroupDAO.getAllForStoragePool(any(Guid.class))).thenReturn(groupList);
    }

    private void VdsExist() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        List<VDS> vdsList = new ArrayList<VDS>();
        vdsList.add(vds);
        when(vdsDAO.getAllForVdsGroup(any(Guid.class))).thenReturn(vdsList);
    }

    private void VdsExistWithHigherVersion() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        vds.setVdsGroupCompatibilityVersion(VERSION_1_2);
        List<VDS> vdsList = new ArrayList<VDS>();
        vdsList.add(vds);
        when(vdsDAO.getAllForVdsGroup(any(Guid.class))).thenReturn(vdsList);
    }

    private void allQueriesForVms() {
        when(vmDao.getAllForVdsGroup(any(Guid.class))).thenReturn(Collections.<VM> emptyList());
    }

    private void clusterHasVds() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        vds.setSupportedClusterLevels(VERSION_1_1.toString());
        List<VDS> vdsList = new ArrayList<VDS>();
        vdsList.add(vds);
        when(vdsDAO.getAllForVdsGroup(any(Guid.class))).thenReturn(vdsList);
    }

    private void clusterHasGlusterVolumes() {
        List<GlusterVolumeEntity> volumes = new ArrayList<GlusterVolumeEntity>();
        volumes.add(new GlusterVolumeEntity());
        when(glusterVolumeDao.getByClusterId(any(Guid.class))).thenReturn(volumes);
    }

    private void clusterHasVMs() {
        VM vm = new VM();
        vm.setVdsGroupId(DEFAULT_VDS_GROUP_ID);
        List<VM> vmList = new ArrayList<VM>();
        vmList.add(vm);

        when(vmDao.getAllForVdsGroup(any(Guid.class))).thenReturn(vmList);
    }

    private void cpuFlagsMissing() {
        List<String> strings = new ArrayList<String>();
        strings.add("foo");
        doReturn(strings).when(cmd).missingServerCpuFlags(any(VDS.class));
    }

    private void cpuFlagsNotMissing() {
        doReturn(null).when(cmd).missingServerCpuFlags(any(VDS.class));
    }

    private void cpuManufacturersDontMatch() {
        doReturn(false).when(cmd).checkIfCpusSameManufacture(any(VDSGroup.class));
    }

    private void cpuManufacturersMatch() {
        doReturn(true).when(cmd).checkIfCpusSameManufacture(any(VDSGroup.class));
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

    private static void setValidCpuVersionMap() {
        mcr.mockConfigValue(ConfigValues.SupportedClusterLevels, createVersionSet());
    }

    private static Set<Version> createVersionSet() {
        Set<Version> versions = new HashSet<Version>();
        versions.add(VERSION_1_0);
        versions.add(VERSION_1_1);
        versions.add(VERSION_1_2);
        return versions;
    }

    private void canDoActionFailedWithReason(final VdcBllMessages message) {
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(message.toString()));
    }
}
