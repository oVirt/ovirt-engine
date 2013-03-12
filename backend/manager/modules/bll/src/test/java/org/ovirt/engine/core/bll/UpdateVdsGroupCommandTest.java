package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class UpdateVdsGroupCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
    private static final Guid STORAGE_POOL_ID = Guid.NewGuid();

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
    public void invalidCpuSelection() {
        createCommandWithDefaultVdsGroup();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_CPU_NOT_FOUND);
    }

    @Test
    public void illegalCpuChange() {
        createCommandWithDefaultVdsGroup();
        cpuExists();
        cpuManufacturersDontMatch();
        vdsGroupHasVds();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_ILLEGAL);
    }

    @Test
    public void invalidVersion() {
        createCommandWithInvalidVersion();
        cpuExists();
        cpuManufacturersMatch();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_GIVEN_VERSION_NOT_SUPPORTED);
    }

    @Test
    public void versionDecrease() {
        createCommandWithOlderVersion();
        cpuExists();
        cpuManufacturersMatch();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void updateWithLowerVersionThanHosts() {
        createCommandWithDefaultVdsGroup();
        cpuExists();
        cpuManufacturersMatch();
        VdsExistWithHigherVersion();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS);
    }

    @Test
    public void updateWithCpuLowerThanHost() {
        createCommandWithDefaultVdsGroup();
        cpuExists();
        cpuManufacturersMatch();
        clusterHasVds();
        cpuFlagsMissing();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS);
    }

    @Test
    public void updateStoragePool() {
        createCommandWithDifferentPool();
        cpuExists();
        cpuManufacturersMatch();
        clusterHasVds();
        cpuFlagsNotMissing();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_CHANGE_STORAGE_POOL);
    }

    @Test
    public void clusterAlreadyInLocalFs() {
        createCommandWithDefaultVdsGroup();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        cpuExists();
        cpuManufacturersMatch();
        allQueriesForVms();
        storagePoolAlreadyHasCluster();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
    }

    @Test
    public void defaultClusterInLocalFs() {
        createCommandWithDefaultVdsGroup();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        cpuExists();
        cpuManufacturersMatch();
        allQueriesForVms();
        canDoActionFailedWithReason(VdcBllMessages.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
    }

    @Test
    public void selectionAlgoNotNone() {
        createCommandWithPowerSaveVdsGroup();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        cpuExists();
        cpuManufacturersMatch();
        allQueriesForVms();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_SELECTION_ALGORITHM_MUST_BE_SET_TO_NONE_ON_LOCAL_STORAGE);
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
    public void vdsGroupWithVirtGlusterNotSupported() {
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
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createVdsGroupWithNoCpuName());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createVdsGroupWithNoCpuName());
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

    private void createCommandWithNoCpuName() {
        createCommand(createVdsGroupWithNoCpuName());
    }

    private void createCommandWithPowerSaveVdsGroup() {
        createCommand(createVdsGroupWithPowerSave());
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

        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createDefaultVdsGroup());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createDefaultVdsGroup());
    }

    private void createCommandWithDifferentName() {
        createCommand(createVdsGroupWithDifferentName());
    }

    private static VDSGroup createVdsGroupWithDifferentName() {
        VDSGroup group = new VDSGroup();
        group.setname("BadName");
        return group;
    }

    private static VDSGroup createNewVdsGroup() {
        VDSGroup group = new VDSGroup();
        group.setcompatibility_version(VERSION_1_1);
        group.setname("Default");
        return group;
    }

    private static VDSGroup createDefaultVdsGroup() {
        VDSGroup group = new VDSGroup();
        group.setname("Default");
        group.setId(VDSGroup.DEFAULT_VDS_GROUP_ID);
        group.setcpu_name("Intel Conroe");
        group.setcompatibility_version(VERSION_1_1);
        group.setStoragePoolId(STORAGE_POOL_ID);
        return group;
    }

    private static VDSGroup createVdsGroupWithNoCpuName() {
        VDSGroup group = new VDSGroup();
        group.setname("Default");
        group.setId(VDSGroup.DEFAULT_VDS_GROUP_ID);
        group.setcompatibility_version(VERSION_1_1);
        group.setStoragePoolId(STORAGE_POOL_ID);
        return group;
    }

    private static VDSGroup createDetachedDefaultVdsGroup() {
        VDSGroup group = createDefaultVdsGroup();
        group.setStoragePoolId(null);
        return group;
    }

    private static VDSGroup createVdsGroupWithOlderVersion() {
        VDSGroup group = createNewVdsGroup();
        group.setcompatibility_version(VERSION_1_0);
        return group;
    }

    private static VDSGroup createVdsGroupWithBadVersion() {
        VDSGroup group = createNewVdsGroup();
        group.setcompatibility_version(new Version(5, 0));
        return group;
    }

    private static VDSGroup createVdsGroupWithDifferentPool() {
        VDSGroup group = createNewVdsGroup();
        group.setStoragePoolId(Guid.NewGuid());
        return group;
    }

    private static VDSGroup createVdsGroupWithPowerSave() {
        VDSGroup group = createDefaultVdsGroup();
        group.setselection_algorithm(VdsSelectionAlgorithm.PowerSave);
        return group;
    }

    private static VDSGroup createVdsGroupWith(boolean virtService, boolean glusterService) {
        VDSGroup group = createDefaultVdsGroup();
        group.setVirtService(virtService);
        group.setGlusterService(glusterService);
        group.setcompatibility_version(VERSION_1_1);
        return group;
    }

    private static storage_pool createStoragePoolLocalFS() {
        storage_pool pool = new storage_pool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        return pool;
    }

    private void storagePoolIsLocalFS() {
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(createStoragePoolLocalFS());
    }

    private void oldGroupIsDetachedDefault() {
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createDetachedDefaultVdsGroup());
    }

    private void storagePoolAlreadyHasCluster() {
        VDSGroup group = new VDSGroup();
        List<VDSGroup> groupList = new ArrayList<VDSGroup>();
        groupList.add(group);
        when(vdsGroupDAO.getAllForStoragePool(any(Guid.class))).thenReturn(groupList);
    }

    private void VdsExistWithHigherVersion() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.Up);
        List<VDS> vdsList = new ArrayList<VDS>();
        vdsList.add(vds);
        when(vdsDAO.getAllForVdsGroup(any(Guid.class))).thenReturn(vdsList);
    }

    private void allQueriesForVms() {
        when(vmDao.getAllForVdsGroup(any(Guid.class))).thenReturn(Collections.<VM> emptyList());
    }

    private void vdsGroupHasVds() {
        List<VDS> vdsList = new ArrayList<VDS>();
        vdsList.add(new VDS());
        when(vdsDAO.getAllForVdsGroup(any(Guid.class))).thenReturn(vdsList);
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
        vm.setVdsGroupId(VDSGroup.DEFAULT_VDS_GROUP_ID);
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
